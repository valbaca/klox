package lox

import lox.TokenType.*

/**
 * Consumes a flat input sequence, like the scanner, but reads tokens instead of characters
 */
class Parser(val tokens: List<Token>) {
    private var current = 0

    private class ParseError : RuntimeException()

    fun parse(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun varDeclaration(): Stmt? {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        if (initializer == null) throw ParseError() // TODO: null is viral :(
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Var(name, initializer)
    }


    private fun statement(): Stmt {
        if (match(PRINT)) return printStatement()
        return expressionStatement()
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Expression(expr)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Print(value)
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = equality()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Variable) {
                val name = expr.name
                return Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    /**
     * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
     */
    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun isAtEnd() = (peek().type == EOF)
    private fun peek() = tokens[current]
    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun previous() = tokens[current - 1]


    private fun comparison(): Expr = binary(::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

    private fun term(): Expr = binary(::factor, MINUS, PLUS)

    private fun factor(): Expr = binary(::unary, SLASH, STAR)

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expr {
        return when {
            match(FALSE) -> Literal(false)
            match(TRUE) -> Literal(true)
            match(NIL) -> Literal(null)
            match(NUMBER, STRING) -> Literal(previous().literal)
            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression")
                return Grouping(expr)
            }
            match(IDENTIFIER) -> Variable(previous())
            else -> throw error(peek(), "Expect expression.")
        }
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    @Suppress("unused") // until we support statements
    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }

    /**
     * Helper method to reduce repetitive code for comparison, term, and factor
     */
    private fun binary(exprFn: () -> Expr, vararg tokenTypes: TokenType): Expr {
        var expr = exprFn()
        while (match(*tokenTypes)) {
            val operator = previous()
            val right = exprFn()
            expr = Binary(expr, operator, right)
        }
        return expr
    }
}