package com.oboapp.oboapp;

import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
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

    @Autowired
    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

    @Value("${webapp.registered-endpoints.webapi}")
    public String webApiEndpoint;

    @GetMapping("/hello")
    public JSONObject hello() {
        JSONObject jo = new JSONObject();
        jo.put("username", "toto");
        return jo;
    }

    @GetMapping("/api")
    public JSONObject api(HttpServletRequest request) {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        final OAuth2AuthorizedClient authorizedClient = this.oAuth2AuthorizedClientRepository.loadAuthorizedClient("myregistrationid", principal, request);

        String result = callWebAPI(authorizedClient);

        JSONObject jo = new JSONObject();
        jo.put("data", result);

        return jo;
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
