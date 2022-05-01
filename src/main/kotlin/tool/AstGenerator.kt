package tool

import java.io.PrintWriter

class AstGenerator {
}

fun main(args: Array<String>) {
    if ((args.size != 1)) {
        error("Usage: generate_ast <output directory>")
    }
    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf<String>(
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")
    with (writer) {
        println("package lox")
        println()
        println("abstract class $baseName")
        for (type in types) {
            val className = type.split(":")[0].trim()
            val fields = type.split(":")[1].trim()
            defineType(writer, baseName, className, fields)
        }
        close()
    }
}

fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    with(writer) {
        println("class $className(")
        for (field in fieldList.split(", ")) {
            val (type, name) = field.split(" ")
            println("    val $name: $type,")
        }
        println("): $baseName() {")
        println("}")
    }
}