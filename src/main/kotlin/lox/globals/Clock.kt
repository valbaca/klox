package lox.globals

import lox.Callable
import lox.Interpreter

class Clock : Callable {
    override val arity: Int
        get() = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return System.currentTimeMillis().toDouble() / 1000.0
    }

    override fun toString(): String {
        return "<native fn>"
    }
}