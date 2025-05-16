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
val commandPrefix = if (onArm) listOf("arch", "-x86_64") else emptyList()

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
    val mainLines =  registerAllocator.allocate(instructions).map { it.emit() } + listOf("")
    val tempFile = output.resolveSibling("temp.s")
    Files.writeString(tempFile, PREAMBLE + mainLines.joinToString("\n"))
    val command = commandPrefix + listOf("gcc", tempFile.toString(), "-o", output.toString())
    val process = ProcessBuilder(command).redirectErrorStream(true).start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        System.err.println("Compilation failed with exit code $exitCode")
        System.err.println("Output: ${process.inputStream.bufferedReader().readText()}")
        System.exit(1)
    }
    if (onArm) {
        val bin = output.resolveSibling("bin")
        output.moveTo(bin, overwrite = true)
        Files.writeString(output, """
            #!/bin/bash
            exec ${commandPrefix.joinToString(" ")} ${bin.toAbsolutePath()}
        """.trimIndent())
        output.toFile().setExecutable(true)
    }
}

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
