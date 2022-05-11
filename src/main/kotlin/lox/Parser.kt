package lox

import lox.TokenType.*
import lox.expr.*
import lox.stmt.*
import lox.stmt.Function
import lox.stmt.Return

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
        return try {
            when {
                match(FUN) -> function("function")
                match(VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun function(kind: String): Stmt? {
        // kind will allow us to support methods later
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= MAX_ARG_SIZE) {
                    error(peek(), "Can't have more than $MAX_ARG_SIZE parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name"))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")
        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Function(name, parameters, body)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Var(name, initializer)
    }


    private fun statement(): Stmt {
        return when {
            match(FOR) -> forStatement()
            match(IF) -> ifStatement()
            match(WHILE) -> whileStatement()
            match(PRINT) -> printStatement()
            match(RETURN) -> returnStatement()
            match(LEFT_BRACE) -> Block(block())
            else -> expressionStatement()
        }
    }


    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        // for (initializer; condition; increment) { body; }


        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }
        val condition = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment = if (!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        /*
        for (initializer; condition; increment) { body; }
        expands to:
         {
             initializer;
             while (condition) {
                 body;
                 increment;
             }
         }
         */

        var body = statement()
        // append increment (if any)
        if (increment != null) body = Block(listOf(body, Expression(increment)))
        // wrap body+increment in while loop, with normalized condition
        body = While(condition ?: Literal(true), body)
        // prepend initializer
        if (initializer != null) body = Block(listOf(initializer, body))
        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenStatement = statement()
        val elseStatement = if (match(ELSE)) statement() else null
        return If(condition, thenStatement, elseStatement)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return While(condition, body)
    }


    private fun block(): List<Stmt?> {
        val statements = mutableListOf<Stmt?>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
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

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after return value.")
        return Return(keyword, value)
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = or()
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

    private fun or(): Expr {
        var expr = and()
        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()
        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Logical(expr, operator, right)
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
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= MAX_ARG_SIZE) {
                    error(peek(), "Can't have more than $MAX_ARG_SIZE arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Call(callee, paren, arguments)
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

    companion object {
        const val MAX_ARG_SIZE = 255
    }
}