package lox

import lox.TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"
        if (value is Double) {
            return value.toString().removeSuffix(".0") // Love that Kotlin had removeSuffix as part of String exts
        }
        return value.toString()
    }

    override fun visitExpr(expr: Binary): Any {
        val left: Any? = evaluate(expr.left)
        val right: Any? = evaluate(expr.right)
        return when (val type = expr.operator.type) {
            BANG_EQUAL -> left != right // don't need special 'isEqual' function with Kotlin
            EQUAL_EQUAL -> left == right
            PLUS -> {
                if (left is Double && right is Double) {
                    return left + right
                }
                if (left is String && right is String) {
                    return left + right
                }
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
            }
            else -> {
                checkNumberOperands(expr.operator, left, right)
                val ld = left as Double
                val rd = right as Double
                return when (type) {
                    MINUS -> ld - rd
                    SLASH -> ld / rd
                    STAR -> ld * rd
                    GREATER -> ld > rd
                    GREATER_EQUAL -> ld >= rd
                    LESS -> ld < rd
                    LESS_EQUAL -> ld <= rd
                    else -> throw RuntimeError(expr.operator, "Unexpected")
                }
            }
        }
    }

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.any { it !is Double }) throw RuntimeError(operator, "Operands must be numbers")
    }

    override fun visitExpr(expr: Grouping): Any? {
        return evaluate(expr.expression)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    override fun visitExpr(expr: Literal): Any? {
        return expr.value
    }

    override fun visitExpr(expr: Unary): Any {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperands(expr.operator, right)
                -1 * (right as Double)
            }
            else -> throw RuntimeError(expr.operator, "Unexpected")
        }
    }

    private fun isTruthy(any: Any?): Boolean = when (any) {
        null -> false
        is Boolean -> any
        else -> true
    }

    override fun visitStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitStmt(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

}