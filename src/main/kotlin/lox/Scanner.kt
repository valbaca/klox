package lox

import lox.ScannerConstants.ALPHAS
import lox.ScannerConstants.DIGITS
import lox.ScannerConstants.KEYWORDS
import lox.ScannerConstants.NUL
import lox.TokenType.*

class Scanner(val source: String) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length


    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    // ignore line-comments
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(SLASH)
                }
            }
            ' ', '\r', '\t' -> {} // ignored whitespace
            '\n' -> line++
            '"' -> string()
            // 'else' is Kotlin equivalent of default:
            else -> when {
                isDigit(c) -> number()
                isAlpha(c) -> identifier()
                else -> error(line, "Unexpected character.")
            }
        }
    }


    private fun advance(): Char = source[current++]
    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    // Like advance, but doesn't consume the character
    private fun peek(): Char {
        if (isAtEnd()) return NUL
        return source[current]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }
        // the closing "
        advance()
        // Trim the surrounding quotes
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun isDigit(c: Char): Boolean = c in DIGITS
    private fun number() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return NUL
        return source[current + 1]
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = KEYWORDS[text]?: IDENTIFIER
        addToken(type)
    }
    private fun isAlpha(c: Char): Boolean = c in ALPHAS
    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)
}

object ScannerConstants {
    const val NUL = '\u0000' // Weirdly Kotlin doesn't support '\0' as a char o.O
    val DIGITS = '0'..'9'
    val ALPHAS = ('a'..'z').union('A'..'Z').plus('_')
    val KEYWORDS = mapOf<String, TokenType>(
        "and" to AND,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE

    )
}