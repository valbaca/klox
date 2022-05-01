package lox

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

class Lox {
}

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> {
            println("Usage: klox [script]")
            exitProcess(ExitCodes.USAGE.value)
        }
    }
}

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
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val expression = parser.parse()
    if (hadError || expression == null) return
    println(AstPrinter().print(expression))
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
    println("[line $line Error $where: $message")
    hadError = true
}

var hadError: Boolean = false
