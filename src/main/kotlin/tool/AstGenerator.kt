package tool

import java.io.PrintWriter

class AstGenerator {
}

fun main(args: Array<String>) {
    if ((args.size != 1)) {
        error("Usage: generate_ast <output directory>")
    }
    val outputDir = args[0]
    defineAst(
        outputDir, "Expr", listOf(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Any? value", // "Any?" was "Object", made ? to handle nil
            "Unary    : Token operator, Expr right"
        )
    )
    defineAst(
        outputDir, "Stmt", listOf(
            "Expression : Expr expression",
            "Print : Expr expression"
        )
    )
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")
    with(writer) {
        println("package lox")
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