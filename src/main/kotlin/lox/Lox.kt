package lox

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args.size) {
        0 -> Lox.runPrompt()
        1 -> Lox.runFile(args[0])
        else -> {
            println("Usage: klox [script]")
            exitProcess(ExitCodes.USAGE.value)
        }
    }
}

object Lox {
    var hadError = false
    var hadRuntimeError = false
    val interpreter = Interpreter()

    /**
     * Starts a REPL using stdin. Ctrl-D to exit
     */
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            println("> ")
            val line = reader.readLine() // null if Ctrl-D
            if (line == null) {
                break
            } else {
                run(line)
                hadError = false
            }
        }
    }

    fun runFile(path: String) {
        run(File(path).readText())
        if (hadError) exitProcess(ExitCodes.DATAERR.value)
        if (hadRuntimeError) exitProcess(ExitCodes.SOFTWARE.value)
    }


    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expressions = parser.parse()
        if (hadError || expressions.any { it == null }) return
        interpreter.interpret(expressions.filterNotNull())
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end ", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    fun report(line: Int, where: String, message: String) {
        println("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println(error.message + "\n[line${error.token.line}]")
        hadRuntimeError = true
    }


}