package lox

class Environment(val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        // Kotlin's `in` replaces contains/indexOf/etc
        if (name.lexeme in values) {
            return values[name.lexeme]
        }
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (name.lexeme in values) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}