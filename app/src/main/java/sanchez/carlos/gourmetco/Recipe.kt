package sanchez.carlos.gourmetco

data class Recipe(val title: String,
                  val image: Int,
                  val calories: String,
                  val time: String,
                  val author: String,
                  val categories: List<String>)

