package lox

import lox.stmt.Function

/* Called LoxFunction */
class Fn(private val declaration: Function) : Callable {
    override val arity = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val env = Environment(interpreter.globals)
        declaration.params.zip(arguments).forEach { (param, arg) ->
            env.define(param.lexeme, arg) // bindings
        }
        try {
            interpreter.executeBlock(declaration.body, env)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}