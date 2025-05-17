package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.web.App;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(classes = App.class)
public class OperationRouteBuilderTest {
    public static final String body = "{\n" +
            "  \"parameters\": [\n" +
            "    {\n" +
            "      \"name\": \"P_CUSTOMERID\",\n" +
            "      \"value\": \"5607479\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_NATIONALID\",\n" +
            "      \"value\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_SUBORGAN\",\n" +
            "      \"value\": \"0\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_SIGNER\",\n" +
            "      \"value\": \"-1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_ACCOUNTSTATUS\",\n" +
            "      \"value\": \"-1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_STARTROW\",\n" +
            "      \"value\": \"1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"P_ENDROW\",\n" +
            "      \"value\": \"11\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"callType\": \"Reader\",\n" +
            "  \"encoding\": \"ASCII\",\n" +
            "  \"requestID\": \"RequestID\"\n" +
            "}";
    @Autowired
    private ProducerTemplate template;

    @Test
    public void test_SCMREAD_GETCUSTOMERACCOUNTS_route() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_METHOD, "POST");
        headers.put(Exchange.CONTENT_TYPE, "application/json");

        String response = template.requestBodyAndHeaders(
                "direct:SCMREAD_GETCUSTOMERACCOUNTS",
                body,
                headers,
                String.class);
        assertFalse(response.isEmpty());
        System.out.println(response);
    }
}
