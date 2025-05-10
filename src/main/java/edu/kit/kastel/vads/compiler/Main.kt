package edu.kit.kastel.vads.compiler

import edu.kit.kastel.vads.compiler.backend.aasm.CodeGenerator
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

    if ("vcg" == System.getenv("DUMP_GRAPHS") || "vcg" == System.getProperty("dumpGraphs")) {
        val tmp = output.toAbsolutePath().resolveSibling("graphs")
        Files.createDirectory(tmp)
        for (graph in graphs) {
            dumpGraph(graph, tmp, "before-codegen")
        }
    }

    // TODO: generate assembly and invoke gcc instead of generating abstract assembly
    val s = CodeGenerator().generateCode(graphs)
    Files.writeString(output, s)
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
        path.resolve(graph.name() + "-" + key + ".vcg"),
        YCompPrinter.Companion.print(graph)
    )
}
