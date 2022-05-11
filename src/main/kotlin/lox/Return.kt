package lox

/**
 * Exception used to simulate 'return'
 * (Note: this is blatantly abusing the Exception system)
 */
class Return(val value: Any?) : RuntimeException(null, null, false, false)
