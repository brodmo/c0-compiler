package edu.kit.kastel.vads.compiler

import edu.kit.kastel.vads.compiler.backend.InstructionSelector
import edu.kit.kastel.vads.compiler.backend.RegisterAllocator
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.SsaTranslation
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering
import edu.kit.kastel.vads.compiler.ir.util.YCompPrinter
import edu.kit.kastel.vads.compiler.lexer.Lexer
import edu.kit.kastel.vads.compiler.parser.ParseException
import edu.kit.kastel.vads.compiler.parser.Parser
import edu.kit.kastel.vads.compiler.parser.TokenSource
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis
import edu.kit.kastel.vads.compiler.semantic.SemanticException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.moveTo

const val PREAMBLE =
    """.global main
.global _main
.text

main:
call _main
movq %rax, %rdi
movq $0x3C, %rax
syscall

_main:
"""

val onArm = System.getProperty("os.arch") in listOf("arm64", "aarch64")
val commandPrefix = if (onArm) "./x86_run.sh" else null

fun wrapCommand(command: String): String {
    return if (onArm) {
        """
            #!/bin/bash
            docker run --platform linux/amd64 --rm -v "$(pwd)":/work -w /work gcc:latest "$command $@"
        """.trimIndent()
    } else {
        command
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    if (args.size != 2) {
        System.err.println("Invalid arguments: Expected one input file and one output file")
        System.exit(3)
    }
    val input = Path.of(args[0])
    val output = Path.of(args[1])
    val program = lexAndParse(input)
    try {
        SemanticAnalysis(program).analyze()
    } catch (e: SemanticException) {
        e.printStackTrace()
        System.exit(7)
        return
    }
    val graphs: MutableList<IrGraph> = ArrayList<IrGraph>()
    for (function in program.topLevelTrees) {
        val translation = SsaTranslation(function, LocalValueNumbering())
        graphs.add(translation.translate())
    }

    val dumpGraphs = false
    if (dumpGraphs) {
        val tmp = output.toAbsolutePath().resolveSibling("graphs")
        for (graph in graphs) {
            dumpGraph(graph, tmp, "before-codegen")
        }
    }

    // TODO: generate assembly and invoke gcc instead of generating abstract assembly
    val instructionSelector = InstructionSelector()
    val registerAllocator = RegisterAllocator()
    val (_, instructions) = graphs[0].endBlock.accept(instructionSelector)
    val mainLines = registerAllocator.allocate(instructions).map { it.emit() } + listOf("")
    val tempFile = output.resolveSibling("temp.s")
    Files.writeString(tempFile, PREAMBLE + mainLines.joinToString("\n"))
    val dir = output.parent.toAbsolutePath()
    val compile = wrapCommand(dir, "gcc",  "./${tempFile.fileName}", "-o", "./${output.fileName}")
    val process = ProcessBuilder(compile).redirectErrorStream(true).start()
    val stdout = process.inputStream.bufferedReader().use { it.readText() }
    val exitCode = process.waitFor()
    print(stdout)
    if (exitCode != 0) { error("Failed to compile $compile exited with code $exitCode") }
    if (onArm) {
        val armOutput = output.resolveSibling("${output.fileName}-arm")
        output.moveTo(armOutput, overwrite = true)
        Files.writeString(
            output, """
            #!/bin/sh
            ${wrapCommand(dir,"./${armOutput.fileName}", "\"$@\"").joinToString(" ")}
        """.trimIndent()
        )
        ProcessBuilder("chmod", "+x", armOutput.toString()).start().waitFor()
    }
    ProcessBuilder("chmod", "+x", output.toString()).start().waitFor()
}

private fun wrapCommand(dir: Path, vararg args: String): List<String> = listOf(
        "docker",
        "run",
        "--platform", "linux/amd64",
        "--rm",
        "-v", "$dir:/work",
        "-w", "/work",
        "gcc:latest",
        *args
    )

@Throws(IOException::class)
private fun lexAndParse(input: Path): ProgramTree {
    try {
        val lexer: Lexer = Lexer.Companion.forString(Files.readString(input))
        val tokenSource = TokenSource(lexer)
        val parser = Parser(tokenSource)
        return parser.parseProgram()
    } catch (e: ParseException) {
        e.printStackTrace()
        System.exit(42)
        throw AssertionError("unreachable")
    }
}

@Throws(IOException::class)
private fun dumpGraph(graph: IrGraph, path: Path, key: String) {
    Files.writeString(
        path.resolve(graph.name + "-" + key + ".vcg"),
        YCompPrinter.Companion.print(graph)
    )
}
