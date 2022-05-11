package lox.stmt
import lox.Token
import lox.expr.Expr

abstract class Stmt {
    abstract fun <R> accept(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitStmt(stmt: Block): R
        fun visitStmt(stmt: Expression): R
        fun visitStmt(stmt: Function): R
        fun visitStmt(stmt: If): R
        fun visitStmt(stmt: Print): R
        fun visitStmt(stmt: Return): R
        fun visitStmt(stmt: Var): R
        fun visitStmt(stmt: While): R
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

class Function(
    val name: Token,
    val params: List<Token>,
    val body: List<Stmt?>,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

class If(
    val condition: Expr,
    val thenBranch: Stmt,
    val elseBranch: Stmt?,
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

class Return(
    val keyword: Token,
    val value: Expr?,
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

class While(
    val condition: Expr,
    val body: Stmt,
) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitStmt(this)
    }
}

