package lox

abstract class Stmt {
    abstract fun <R> accept(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitStmt(stmt: Block): R
        fun visitStmt(stmt: Expression): R
        fun visitStmt(stmt: Print): R
        fun visitStmt(stmt: Var): R
    }
}

class Block(
    val statements: List<Stmt?>,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

class Expression(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

class Print(
    val expression: Expr,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

class Var(
    val name: Token,
    val initializer: Expr?,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

