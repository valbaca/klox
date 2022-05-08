package lox

abstract class Expr {
    abstract fun <R> accept(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitExpr(expr: Assign): R
        fun visitExpr(expr: Binary): R
        fun visitExpr(expr: Grouping): R
        fun visitExpr(expr: Literal): R
        fun visitExpr(expr: Unary): R
        fun visitExpr(expr: Variable): R
    }
}

class Assign(
    val name: Token,
    val value: Expr,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

class Binary(
    val left: Expr,
    val operator: Token,
    val right: Expr,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

class Grouping(
    val expression: Expr,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

class Literal(
    val value: Any?,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

class Unary(
    val operator: Token,
    val right: Expr,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

class Variable(
    val name: Token,
) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpr(this)
    }
}

