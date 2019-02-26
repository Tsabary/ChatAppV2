package co.getdere.chatapp.Model

class Channel(val name: String, val Description: String, val id: String) {

    override fun toString(): String {
        return ("#$name")
    }

}