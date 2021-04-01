import com.google.common.primitives.UnsignedLong;

public class Test {

    @org.junit.jupiter.api.Test
    public void myTest(){

        var temp = System.getenv("DATASOURCE_USERNAME");
        System.out.println(temp);
    }
}
