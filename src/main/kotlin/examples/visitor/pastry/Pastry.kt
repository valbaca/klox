package examples.visitor.pastry

// Shows the Visitor pattern Pastry example from 5.3.2
// https://craftinginterpreters.com/representing-code.html#the-visitor-pattern

abstract class Pastry {
    abstract fun accept(visitor: PastryVisitor)
}

class Beignet : Pastry() {
    override fun accept(visitor: PastryVisitor) {
        println("Visiting beignet")
        visitor.visit(this)
    }

}

class Cruller : Pastry() {
    override fun accept(visitor: PastryVisitor) {
        println("Visiting cruller")
        visitor.visit(this)
    }
}


interface PastryVisitor {
    /* Kotlin supports overloading */
//    fun visitBeignet(beignet: Beignet)
//    fun visitCruller(cruller: Cruller)
    fun visit(beignet: Beignet)
    fun visit(cruller: Cruller)
}

class NewOrleansBaker : PastryVisitor {
    override fun visit(beignet: Beignet) {
        println("Piled high with powdered sugar and comes with a cup of café au lait")
    }

    override fun visit(cruller: Cruller) {
        println("Sorry, all out!")
    }
}

fun main() {
    val beignet = Beignet()
    val cruller = Cruller()

    val baker = NewOrleansBaker()

    val pastries = listOf<Pastry>(beignet, cruller)
    for (pastry in pastries) {
        pastry.accept(baker)
    }
}