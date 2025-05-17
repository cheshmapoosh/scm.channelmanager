package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.core.integration.template.ConditionalServiceMetadataTemplate;
import ir.daneshrefah.scm.core.integration.template.ServiceMetadataTemplate;
import ir.daneshrefah.scm.core.integration.template.ServiceMetadataTemplateCompiler;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestResponseTemplate;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import ir.daneshrefah.scm.plugin.camel.component.webclient.WebClientComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ir.daneshrefah.scm.common.model.message.Message.*;
import static org.apache.camel.builder.Builder.simple;

//@Component
public class RestExternalServiceExecutor extends ServiceExecutor {
    private final ParameterDataProvider parameterDataProvider;

    public RestExternalServiceExecutor(ParameterDataProvider parameterDataProvider) {
        this.parameterDataProvider = parameterDataProvider;
    }

    @Override
    protected void defineServiceRoute(Service service, ProcessorDefinition<?> processorDefinition) {
        if (!ClassUtils.isAssignable(RestExternalService.class, service.getClass())) {
            //TODO SCMNEW-5: throw scm exception
            throw new IllegalArgumentException("Service implementation type is not RestExternalService");
        }
        RestExternalService restExternalService = (RestExternalService) service;
        String serviceCode = restExternalService.getCode();
        String engine = restExternalService.getServiceMetadata().getEngine();

        String pathTemplate = restExternalService.getServiceMetadata().getPathTemplate();
        List<Parameter> pathParameters = restExternalService.getParameters(ParameterActionType.REQUEST_PATH_VARIABLE);
        ServiceMetadataTemplate pathServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                .name(serviceCode + "_path")
                .engin(engine)
                .template(pathTemplate)
                .parameters(pathParameters)
                .provider(parameterDataProvider)
                .compile();

        String headersTemplate = restExternalService.getServiceMetadata().getHeadersTemplate();
        List<Parameter> headersParameters = restExternalService.getParameters(ParameterActionType.REQUEST_HEADER);
        ServiceMetadataTemplate headersServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                .name(serviceCode + "_headers")
                .engin(engine)
                .template(headersTemplate)
                .parameters(headersParameters)
                .provider(parameterDataProvider)
                .compile();

        String requestTemplate = restExternalService.getServiceMetadata().getRequestTemplate();
        List<Parameter> requestParameters = restExternalService.getParameters(ParameterActionType.REQUEST_BODY);
        ServiceMetadataTemplate requestServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                .name(serviceCode + "_request")
                .engin(engine)
                .template(requestTemplate)
                .parameters(requestParameters)
                .provider(parameterDataProvider)
                .compile();

        List<RestResponseTemplate> responseTemplates = restExternalService.getServiceMetadata().getResponseTemplates();
        List<Parameter> responseParameters = restExternalService.getParameters(ParameterActionType.RESPONSE_BODY);
        List<ConditionalServiceMetadataTemplate> responseServiceMetadataTemplates = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(responseTemplates)) {
            final AtomicInteger index = new AtomicInteger(0);
            responseTemplates.forEach(template -> {
                int currentIndex = index.getAndIncrement();
                ServiceMetadataTemplate conditionTemplate = ServiceMetadataTemplateCompiler.compiler()
                        .name(serviceCode + "_condition_" + currentIndex)
                        .engin(engine)
                        .template(template.getConditionTemplate())
                        .parameters(responseParameters)
                        .provider(parameterDataProvider)
                        .compile();

                ServiceMetadataTemplate responseTemplate = ServiceMetadataTemplateCompiler.compiler()
                        .name(serviceCode + "_response_" + currentIndex)
                        .engin(engine)
                        .template(template.getResponseTemplate())
                        .parameters(responseParameters)
                        .provider(parameterDataProvider)
                        .compile();
                ConditionalServiceMetadataTemplate conditionalServiceMetadataTemplate =
                        new ConditionalServiceMetadataTemplate(conditionTemplate, responseTemplate);
                responseServiceMetadataTemplates.add(conditionalServiceMetadataTemplate);
            });

        }

        processorDefinition
                .setProperty(ORIGINAL_BODY, simple("${body}"))
                .setProperty(ORIGINAL_HEADERS, simple("${headers}"))
                .process(exchange -> {
                    Message message = exchange.getMessage().getBody(Message.class);
                    Map<String, Object> headers = headersServiceMetadataTemplate.renderAsMap(message);
                    exchange.getIn().setHeaders(headers);
                    exchange.getIn().setBody(requestServiceMetadataTemplate.render(message));
                    exchange.setProperty(Message.HTTP_PATH, pathServiceMetadataTemplate.renderAsMap(message));
                    exchange.setProperty(Message.HTTP_METHOD, restExternalService.getServiceMetadata().getMethod());
                }).toD("webclient:${exchangeProperty.scmHttpPath}" +
                        "?method=${exchangeProperty.scmHttpMethod}" +
                        "&responseTimeout=1000" +
                        "&connectTimeout=50" +
                        "&writeTimeout=100" +
                        "&retryEnabled=true" +
                        "&maxAttempts=3" +
                        "&minBackoff=1000" +
                        "&wiretap=true")
                .process(exchange -> {
                    //TODO SCMNEW-4: support another content type. e.g. xml, String and etc
                    JsonNode responseBody = exchange.getIn().getBody(JsonNode.class);
                    Message responseMessage = builder().payload(responseBody).build();
                    if (CollectionUtils.isNotEmpty(responseServiceMetadataTemplates)) {
                        ServiceMetadataTemplate responseServiceMetadataTemplate = responseServiceMetadataTemplates
                                .stream()
                                .filter(template -> template.matches(responseMessage))
                                .findFirst().orElseThrow(() -> new IllegalStateException("No matching template found"));
                        JsonNode paylod = responseServiceMetadataTemplate.renderAsJson(responseMessage);
                        exchange.getMessage().setBody(Message.builder().payload(paylod).build());

                    }
                });
    }

    private boolean evaluateCondition(ServiceMetadataTemplate conditionTemplate, Message message) throws Exception {
        String result = conditionTemplate.render(message);
        return Boolean.parseBoolean(result.trim());
    }

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        WebClientComponent component = new WebClientComponent();
        context.addComponent("webclient", component);
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                String serviceCode = "GETCUSTOMERACCOUNTS";
                String engine = ServiceMetadataTemplate.FREEMARKER;
                String pathTemplate = "https://${core_base_url}/Service/SCMREAD.GETCUSTOMERACCOUNTS";
                String headersTemplate = """
                        {"accept":"text/plain",
                        "Content-Type":"application/json"}
                        """;
                String requestTemplate = """
                        {
                          "parameters": [
                            {
                              "name": "P_CUSTOMERID",
                              "value": "${customer_no}"
                            },
                            {
                              "name": "P_NATIONALID",
                              "value": ""
                            },
                            {
                              "name": "P_SUBORGAN",
                              "value": "0"
                            },
                            {
                              "name": "P_SIGNER",
                              "value": "-1"
                            },
                            {
                              "name": "P_ACCOUNTSTATUS",
                              "value": "-1"
                            },
                            {
                              "name": "P_STARTROW",
                              "value": "1"
                            },
                            {
                              "name": "P_ENDROW",
                              "value": "11"
                            }
                          ],
                          "callType": "Reader",
                          "encoding": "ASCII",
                          "requestID": "RequestID"
                        }
                        """;
                Parameter path = new Parameter();
                path.setName("core_base_url");
                path.setDefaultValue("scm-core.daneshrefah.ir");
                ServiceMetadataTemplate pathServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                        .name(serviceCode + "_path")
                        .engin(engine)
                        .template(pathTemplate)
                        .parameters(List.of(path))
                        .compile();
                ServiceMetadataTemplate headersServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                        .name(serviceCode + "_headers")
                        .engin(engine)
                        .template(headersTemplate)
                        .compile();
                Parameter request = new Parameter();
                request.setName("customer_no");
                request.setDefaultValue("5607479");
                ServiceMetadataTemplate requestServiceMetadataTemplate = ServiceMetadataTemplateCompiler.compiler()
                        .name(serviceCode + "_request")
                        .engin(engine)
                        .template(requestTemplate)
                        .parameters(List.of(request))
                        .compile();

                from("direct:start")
                        .setProperty(ORIGINAL_BODY, simple("${body}"))
                        .process(exchange -> {
                            Message message = exchange.getMessage().getBody(Message.class);
                            Map<String, Object> headers = headersServiceMetadataTemplate.renderAsMap(message);
                            exchange.getIn().setHeaders(headers);
                            exchange.getIn().setBody(requestServiceMetadataTemplate.render(message));
                            exchange.setProperty(Message.HTTP_PATH, pathServiceMetadataTemplate.render(message));
                            exchange.setProperty(Message.HTTP_METHOD, "POST");
                        }).toD("webclient:${exchangeProperty.scmHttpPath}" +
                                "?method=${exchangeProperty.scmHttpMethod}" +
                                "&responseTimeout=1000" +
                                "&connectTimeout=50" +
                                "&writeTimeout=100" +
                                "&retryEnabled=true" +
                                "&maxAttempts=3" +
                                "&minBackoff=1000" +
                                "&wiretap=true")
                        .log("Api Repo response: ${body}")
                        .setBody().simple("${exchangeProperty.scmOriginalBody}")
                        .log("original body recovered: ${body.getPayloadValue('name')}");

            }
        });

        context.start();
        ProducerTemplate template = context.createProducerTemplate();

        Message message = Message.builder().build();
        message.setPayloadValue("name", "activeAccount");

        String result = template.requestBody("direct:start", message, String.class);

        System.out.println("Response from route: " + result);
    }
}
