package sanchez.carlos.gourmetco.ui.create

data class Ingredient(
    val name: String = "",
    val qty: String = "",
    val unit: String = "Pz"
) {
    constructor() : this(
        "",
        "",
        "Pz"
    )
}
