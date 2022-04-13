package com.nickjiang.webfluxwebclient;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication
public class WebFluxWebclientApplication {
    private static Logger logger = LoggerFactory.getLogger(WebFluxWebclientApplication.class);

    public static void main(String[] args) {
        setSpringBootNonWebEnvironment(args);

        logger.info("Spring Boot 2 Application is running in non web environment.");

//		SpringApplication.run(WebfluxWebclientApplication.class, args);
    }

    @Bean
    public CommandLineRunner myCommandLineRunner() {
        return args -> {
            var webFluxWebclientApplication = new WebFluxWebclientApplication();

            var ret = webFluxWebclientApplication.call_rest_api();
            logger.info("Got response: " + ret.toString());

            ret = webFluxWebclientApplication.call_rest_api_using_builder();
            logger.info("Got response: " + ret.toString());
        };
    }

    private static void setSpringBootNonWebEnvironment(String[] args) {
        var app = new SpringApplication(WebFluxWebclientApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    private Object call_rest_api() {
        var jscodeInfo = new JscodeInfo();
        jscodeInfo.wxcode = "12312";

        return WebClient.create("https://jsonplaceholder.typicode.com")
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/posts")
                        .queryParam("appid", "test")
                        .queryParam("secret", "test")
                        .queryParam("js_code", "test")
                        .build())
                .bodyValue(jscodeInfo)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    /**
     * Call Restful API using Jetty HttpClient
     * @return
     */
    private Object call_rest_api_using_builder() throws Exception {
        org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient(new SslContextFactory(true));
        ClientHttpConnector connector = new JettyClientHttpConnector(httpClient);

        var jscodeInfo = new JscodeInfo();
        jscodeInfo.wxcode = "12312";

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .build();

        var ret = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("jsonplaceholder.typicode.com")
                        .path("/posts")
                        .queryParam("appid", "test")
                        .queryParam("secret", "test")
                        .queryParam("js_code", "test")
                        .build())
                .bodyValue(jscodeInfo)
                .retrieve()
                .bodyToMono(Object.class)
                .block();

        // Destroy reactorClientResourceFactory
        httpClient.stop();

        return ret;
    }
}
