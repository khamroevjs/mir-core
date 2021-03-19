package mir.services;

import antlr.ParseTree;
import mir.models.ParsedMessage;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class MessageServiceTest {

    private final String URI = "https://mir-platform.herokuapp.com/messages";

    /*
    @Test
    void MyTest(){

        RestTemplate restTemplate = new RestTemplate();

        var responseEntity = restTemplate.exchange(
                URI,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ParsedMessage>>() {}
        );

        var list = responseEntity.getBody();

        if (list != null) {
            for (var item: list)
                System.out.println(item.getHex());
        }
    }

     */
}
