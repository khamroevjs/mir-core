package mir.routing;

//import mir.routing.exception.UnsupportedMessageTypeIDException;

import mir.controllers.MessageController;
import mir.routing.constants.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

@SpringBootApplication
@EntityScan(basePackages = "mir.models")
@ComponentScan(basePackages = "mir")
@EnableJpaRepositories(basePackages = "mir.repositories")
@EnableSwagger2
public class Application {
//    private static final int MTI_LENGTH = 4;

    static public void start() {

    }

    static public String sendMessage(String message) {
        String answer = null;
        try {
            Socket sendSocket = new Socket("localhost", Constants.Ports.ACQUIRER_MODULE);
            ServerSocket serverSocket = new ServerSocket(Constants.Ports.ROUTING_MODULE, 1);

            DataOutputStream out = new DataOutputStream(sendSocket.getOutputStream());

            out.writeUTF(message);
            out.flush();

            Socket acquirerAnswerSocket = serverSocket.accept();
            DataInputStream in = new DataInputStream(acquirerAnswerSocket.getInputStream());

            answer = in.readUTF();

            return answer;
        } catch (IOException ex) {
            // TODO: как-то отреагировать.
        }

        return answer;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // TODO: сделать запуск модулей эквайера, платформы и эмитента параллельным, вызовом одной функции.
        // TODO: На данный момент каждый модуль надо запускать самому.
        // run();

        System.out.println(sendMessage("asdasd)"));
    }

    @Bean
    Docket Docs(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }
    @Bean
    public ApiInfo apiInfo() {
        final ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("Mir-Core").version("0.0.1-demo").license("(C) Copyright 2020")
                .description("List of all the APIs of Mir-Core through Swagger UI");
        return builder.build();
    }
}
