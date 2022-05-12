package lox

import lox.stmt.Function

/* Originally called LoxFunction ("Fn" borrowed from Clojure) */
class Fn(
    private val declaration: Function,
    private val closure: Environment
) : Callable {
    override val arity = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val env = Environment(closure)
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