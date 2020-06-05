
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert._;
class ArticleTests {

   
    @Test
    def restockTest(){
        var article= new Article(-1,"Hersteller","Test","Name",20.0f,12)
        article.restock(12);
        assertEquals(25,article.getStock())
    }

}