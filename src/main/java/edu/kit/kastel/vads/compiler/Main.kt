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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.moveTo
import kotlin.system.exitProcess

const val DUMP_GRAPHS = false

val PREAMBLE = """
    .intel_syntax noprefix
    .global main
    .global _main
    .text
    
    main:
    call _main
    mov rdi, rax
    mov rax, 0x3C
    syscall
    
    _main:
""".trimIndent()

val ON_ARM = System.getProperty("os.arch") in listOf("arm64", "aarch64")

fun main(args: Array<String>) {
    val (source, executable) = parseArgs(args)
    val program = parseSourceFile(source)
    analyzeSemantics(program)
    val graphs = generateIrGraphs(program)
    if (DUMP_GRAPHS) {
        dumpGraphsToFiles(graphs, executable, "before-codegen")
    }
    val assembly = executable.resolveSibling("asm.s")
    generateAssembly(graphs, assembly)
    compileAssembly(assembly, executable)
}

fun parseArgs(args: Array<String>): Pair<Path, Path> {
    if (args.size != 2) {
        System.err.println("Invalid arguments: Expected one source file and one executable file")
        exitProcess(3)
    }
    return Path.of(args[0]) to Path.of(args[1])
}

private fun parseSourceFile(source: Path): ProgramTree {
    try {
        val lexer = Lexer.forString(Files.readString(source))
        val tokenSource = TokenSource(lexer)
        val parser = Parser(tokenSource)
        return parser.parseProgram()
    } catch (e: ParseException) {
        e.printStackTrace()
        exitProcess(42)
    }
}

private fun analyzeSemantics(program: ProgramTree) {
    try {
        SemanticAnalysis(program).analyze()
    } catch (e: SemanticException) {
        e.printStackTrace()
        exitProcess(7)
    }
}

private fun generateIrGraphs(program: ProgramTree): List<IrGraph> {
    return program.topLevelTrees.map { function ->
        SsaTranslation(function, LocalValueNumbering()).translate()
    }
}

private fun dumpGraphsToFiles(graphs: List<IrGraph>, executable: Path, key: String) {
    val graphsDir = executable.toAbsolutePath().resolveSibling("graphs")
    graphs.forEach { graph ->
        Files.writeString(
            graphsDir.resolve("${graph.name}-$key.vcg"),
            YCompPrinter.print(graph)
        )
    }
}

private fun generateAssembly(graphs: List<IrGraph>, assembly: Path) {
    val instructionSelector = InstructionSelector()
    val registerAllocator = RegisterAllocator()
    val (_, instructions) = graphs[0].endBlock.accept(instructionSelector)
    val mainLines = registerAllocator.allocate(instructions).map { it.emit() } + listOf("")
    Files.writeString(assembly, "$PREAMBLE\n${mainLines.joinToString("\n")}")
}

private fun compileAssembly(assembly: Path, executable: Path) {
    val commandPrefix = if (ON_ARM) listOf("arch", "-x86_64") else emptyList()
    val command = listOf("gcc", assembly.toString(), "-o", executable.toString())
    runCommand(commandPrefix + command)
    if (ON_ARM) {
        armWrapAssembly(executable)
    }
}

private fun runCommand(command: List<String>) {
    val process = ProcessBuilder(command)
        .redirectErrorStream(true)
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        val stdout = process.inputStream.bufferedReader().readText()
        System.err.println("Command failed with exit code $exitCode")
        System.err.println("Output: $stdout")
        exitProcess(exitCode)
    }
}

private fun armWrapAssembly(executable: Path) {
    val x86binary = executable.resolveSibling("x86bin")
    executable.moveTo(x86binary, overwrite = true)
    val wrapperScript = """
        #!/bin/bash
        exec arch -x86_64 ${x86binary.toAbsolutePath()}
    """.trimIndent()
    Files.writeString(executable, wrapperScript)
    executable.toFile().setExecutable(true)
}
