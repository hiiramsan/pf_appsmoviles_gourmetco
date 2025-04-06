package sanchez.carlos.gourmetco.ui.create

data class Ingredient(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "Pz"
) {
    constructor() : this("", "", "Pz")
}