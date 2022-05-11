# Notes

*These are my own rough notes on what I learned or re-learned as a result of going through Crafting Interpreters. A mix of language-related notes and Kotlin-specific notes.*

# 10 Functions

FFI: Foreign function interface. Allows users to provide their own native functions.

Lisp-1: functions and variables are in "one" namespace. Ex: Scheme, Clojure, Lox

Lisp-2: functions and variables are partitioned into "two" namespaces. Ex: Common Lisp

Kotlin anonymous class (actually "anonymous object" syntax):

```kotlin
interface Shouter { fun shout() }

object : Shouter {
    override fun shout() {
        println("HI!")
    }
}
```

[Doc: Object Expressions](https://kotlinlang.org/docs/object-declarations.html#object-expressions)

