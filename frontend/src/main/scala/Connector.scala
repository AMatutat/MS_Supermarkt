import org.scalajs.dom
import dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Connector() {
//API GET CALL EXAMPLE
  def getAllArticle: Future[String] = Future {
    Ajax.get("http://localhost:9000/allArticle").toString()
  }
}
