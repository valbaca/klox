package lox

import lox.expr.*

class AstPrinter : Expr.Visitor<String> {

    fun print(expr: Expr): String = expr.accept(this)

    override fun visitExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitExpr(expr: Call): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitExpr(expr: Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitExpr(expr: Logical): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitExpr(expr: Variable): String {
        TODO("Not yet implemented")
    }

    override fun visitExpr(expr: Assign): String {
        TODO("Not yet implemented")
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        // Kotlin idiom: use buildString over StringBuilder
        return buildString {
            append("(")
            append(name)
            for (expr in exprs) {
                append(" ")
                // here need to qualify `this` with a tag b/c we're within a buildString
                append(expr.accept(this@AstPrinter))
            }
            append(")")
        }
    }
}

fun main() {
    val expression = Binary(
        Unary(
            Token(TokenType.MINUS, "-", null, 1), Literal(123)
        ), Token(TokenType.STAR, "*", null, 1), Grouping(
            Literal(45.67)
        )
    )
    println(AstPrinter().print(expression)) // (* (- 123) (group 45.67))
}