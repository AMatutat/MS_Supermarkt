class Bewertung(
    val text: String,
    val rating: Int,
    val author: User,
    val toArticle: Article
) {

  def getText(): String = this.text
  def getRating(): Int = this.rating
  def getUser(): User = this.author
  def getArticle(): Article = this.toArticle
  def push():Unit = 0 //api call Put 
}
