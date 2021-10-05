package com.oboapp.oboapp;

import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloWorldController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldController.class);

    @Autowired
    private WebClient webClient;

    @Value("${webapp.registered-endpoints.webapi}")
    public String webApiEndpoint;

    @GetMapping("/hello")
    public JSONObject hello() {
        JSONObject jo = new JSONObject();
        jo.put("username", "toto");
        return jo;
    }

    @GetMapping("/api")
    public String api(@RegisteredOAuth2AuthorizedClient("myregistrationid")OAuth2AuthorizedClient authorizedClient) {
        String result = this.callWebAPI(authorizedClient);

        return result;
    }

    private String callWebAPI(OAuth2AuthorizedClient authorizedClient) {
        if (null != authorizedClient) {
            String body = webClient
                    .get()
                    .uri(webApiEndpoint)
                    .attributes(oauth2AuthorizedClient(authorizedClient))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            LOGGER.info("Response from WebAPI: {}", body);
            return null != body ? body : "Failed to get data from Web API.";
        } else {
            return "Failed to get data from Web API.";
        }
    }
}
