class Review(
    val text: String,
    val rating: Int,
    val author: User,
    val toArticle: Int
) {

  def getText: String = this.text
  def getRating: Int = this.rating
  def getUser: User = this.author
  def getArticle: Int = this.toArticle
  def push: Unit = {}

}
