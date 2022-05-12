package lox

import lox.TokenType.*
import lox.expr.*
import lox.globals.Clock
import lox.stmt.*
import lox.expr.Grouping as LoxExprGrouping

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = Environment()
    private var environment = globals

    init {
        // Kotlin's anonymous object syntax is clunky and ugly, so just used a class
        globals.define("clock", Clock())
    }

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

    override fun visitExpr(expr: Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }
        if (callee !is Callable) {
            throw RuntimeError(expr.paren, "Can only call functions and classses.")
        }
        if (arguments.size != callee.arity) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity} arguments but got ${arguments.size}.")
        }
        return callee.call(this, arguments)
    }

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.any { it !is Double }) throw RuntimeError(operator, "Operands must be numbers")
    }

    override fun visitExpr(expr: LoxExprGrouping): Any? {
        return evaluate(expr.expression)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    override fun visitExpr(expr: Literal): Any? {
        return expr.value
    }

    override fun visitExpr(expr: Logical): Any? {
        // evaluate left operand first and see if we can short-circuit
        // notice we return the actual values, no boolean casting
        val left = evaluate(expr.left)
        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
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

    override fun visitStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment) {
        val prev = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                checkNotNull(statement)
                execute(statement)
            }
        } finally {
            this.environment = prev
        }
    }

    override fun visitStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitStmt(stmt: lox.stmt.Function) {
        // the env when the function is *declared*, not when it's called.
        val function = Fn(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitStmt(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitStmt(stmt: lox.stmt.Return) {
        val value = if (stmt.value != null) evaluate(stmt.value) else null
        throw Return(value)
    }

    override fun visitStmt(stmt: Var) {
        val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitExpr(expr: Variable) = environment.get(expr.name)

    override fun visitExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }
}