# klox: a lox compiler written in Kotlin

At the same time [Crafting Interpreters](https://craftinginterpreters.com/) came up next on my reading list, I was also learning Kotlin. To knock two birds with one stone, this repo is an implementation of the `lox` compiler written in Kotlin.

The Java complier is called `jlox` and the one written in C is called `clox`. Naturally, the Kotlin version would be called `klox`

# Why?

To learn and become familiar with Kotlin by using a reference written in Java

Kotlin has nice features (compared to Java):

1. Null-safety by default, unlike Java where NPEs abound
2. Kotlin is more `fun`ctional: functions are first-class (rather than just public static methods)
3. `when` statements are more expressive/flexible than switch/case/break
4. `val` is better than `final` on every line making my eyes bleed
5. Properties over getters/setters
6. String templates
7. TODO() is literally built into Kotlin
8. Simple `in` keyword for collections and ranges
9. `with` saves repetition (like with `writer.println`)