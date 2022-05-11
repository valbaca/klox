package tool

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    if ((args.size != 1)) {
        error("Usage: generate_ast <output directory>")
    }
    val outputDir = args[0]
    GenerateAst.defineAst(
        outputDir, "Expr", listOf(
            "Assign   : Token name, Expr value",
            "Binary   : Expr left, Token operator, Expr right",
            "Call     : Expr callee, Token paren, List<Expr> arguments",
            "Grouping : Expr expression",
            "Literal  : Any? value", // "Any?" was "Object", made ? to handle nil
            "Logical  : Expr left, Token operator, Expr right",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        )
    )
    GenerateAst.defineAst(
        outputDir, "Stmt", listOf(
            "Block      : List<Stmt?> statements",
            "Expression : Expr expression",
            "Function   : Token name, List<Token> params, List<Stmt?> body",
            "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
            "Print      : Expr expression",
            "Return     : Token keyword, Expr? value",
            "Var        : Token name, Expr? initializer",
            "While      : Expr condition, Stmt body"
        )
    )
}

object GenerateAst {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val basePath = "$outputDir/lox/${baseName.lowercase()}"
        Files.createDirectories(Paths.get(basePath))
        val path = "$outputDir/lox/${baseName.lowercase()}/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")
        with(writer) {
            println("package lox.${baseName.lowercase()}")
            println("import lox.Token")
            if (baseName != "Expr") println("import lox.expr.Expr")
            println()
            println("abstract class $baseName {")
            // The base accept() method
            println("    abstract fun <R> accept(visitor: Visitor<R>): R")
            defineVisitor(writer, baseName, types)
            println("}")
            println()
            for (type in types) {
                val className = type.split(":")[0].trim()
                val fields = type.split(":")[1].trim()
                defineType(writer, baseName, className, fields)
            }
            close()
        }
    }

    // Kotlin supports method overloading, so we can name them visit() instead of visitName()
    fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        with(writer) {
            println("    interface Visitor<R> {")
            for (type in types) {
                val typeName = type.split(":")[0].trim()
                println("        fun visit$baseName(${baseName.lowercase()}: $typeName): R")
            }
            println("    }")
        }
    }

    fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        with(writer) {
            println("class $className(")
            for (field in fieldList.split(", ")) {
                val (type, name) = field.split(" ")
                println("    val $name: $type,")
            }
            println(") : $baseName() {")
            // Visitor pattern
            println("    override fun <R> accept(visitor: Visitor<R>): R {")
            println("        return visitor.visit$baseName(this)")
            println("    }")
            println("}")
            println()
        }
    }
}
