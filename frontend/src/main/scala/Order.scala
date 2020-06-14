
class Order(
    val id: Int,
    val customer: User,
    var state: String,
    val date: String,
    val article: List[Article]
) {
  def getUser(): User = this.customer
  def getState(): String = this.state
  def getID(): Int = this.id
  def getDate():String = this.date
  def getArticle:List[Article ] =this.article
  def setStatus(state: String): Unit = this.state=state //api call ALTER

}
