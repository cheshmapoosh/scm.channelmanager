package ir.daneshrefah.scm.core.integration.observability.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.gateway.InboundRouteDefinition;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.observation.starter.ElasticFieldType;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.utils.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.StreamCache;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayObservationEnrichmentSupport {
    public static final String DEFINITION_PROPERTY = "scm.observation.gateway.definition";
    public static final String ATTRIBUTES_PROPERTY = "scm.observation.gateway.attributes";

    private static final int SUPPORTED_VERSION = 1;
    private static final String REQUEST_SNAPSHOT_PROPERTY = "scm.observation.gateway.request.snapshot";
    private static final String RESPONSE_SNAPSHOT_PROPERTY = "scm.observation.gateway.response.snapshot";
    private static final Set<String> RESERVED_OBSERVATION_ATTRIBUTES = Set.of(
            "trace.id",
            "correlation.id",
            "service.name",
            "span.start_time",
            "span.end_time",
            "span.duration_ms",
            "span.events",
            "scm.service.code",
            "scm.service.name",
            "scm.service.version",
            "scm.service.duration_ms",
            "scm.status.duration_ms"
    );
    private static final Set<String> UNSAFE_AUTHENTICATED_ATTRIBUTE_NAMES = Set.of(
            "access_token",
            "access_tokens",
            "refresh_token",
            "refresh_tokens",
            "bearer_token",
            "bearer_tokens",
            "token",
            "tokens",
            "authorization",
            "authorization_value",
            "password",
            "password_hash",
            "secret",
            "client_secret",
            "credential",
            "credentials",
            "cookie",
            "otp",
            "pin",
            "cvv",
            "api_key",
            "apikey"
    );
    private static final Map<String, String> REQUEST_HEADER_EVENT_ATTRIBUTES = Map.ofEntries(
            Map.entry("user-agent", CommonTraceAttributes.HTTP_REQUEST_HEADER_USER_AGENT.name()),
            Map.entry("accept", CommonTraceAttributes.HTTP_REQUEST_HEADER_ACCEPT.name()),
            Map.entry("accept-language", CommonTraceAttributes.HTTP_REQUEST_HEADER_ACCEPT_LANGUAGE.name()),
            Map.entry("origin", CommonTraceAttributes.HTTP_REQUEST_HEADER_ORIGIN.name()),
            Map.entry("referer", CommonTraceAttributes.HTTP_REQUEST_HEADER_REFERER.name()),
            Map.entry("content-type", CommonTraceAttributes.HTTP_REQUEST_HEADER_CONTENT_TYPE.name()),
            Map.entry("content-length", CommonTraceAttributes.HTTP_REQUEST_HEADER_CONTENT_LENGTH.name()),
            Map.entry("x-scm-client-correlation-id", CommonTraceAttributes.HTTP_REQUEST_HEADER_CLIENT_CORRELATION_ID.name())
    );

    private final ObjectMapper objectMapper;
    private final ObservationAttributeRegistry attributeRegistry;

    public GatewayObservationDefinition definitionFor(
            RuntimeRoutePlan routePlan,
            RuntimeServicePlan servicePlan,
            InboundRouteDefinition inboundRoute
    ) {
        DefinitionRegistrationContext context = registrationContext(routePlan, servicePlan, inboundRoute, null);
        List<ChannelServiceDefinition> definitions = observationDefinitions(servicePlan);
        if (definitions.isEmpty()) {
            return GatewayObservationDefinition.empty();
        }
        if (definitions.size() > 1) {
            invalid(context.withDefinition(duplicateDefinitionIds(definitions)), "duplicateDefinitions");
        }
        ChannelServiceDefinition definition = definitions.getFirst();
        return compile(definition, context.withDefinition(definition.getId()));
    }

    public void captureRequest(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        try {
            Object body = immutableBodyCopy(exchange.getMessage().getBody());
            String rawQuery = rawQuery(exchange);
            GatewayRequestSnapshot snapshot = new GatewayRequestSnapshot(
                    body,
                    bodySize(body),
                    safeHeaders(exchange.getMessage().getHeaders()),
                    queryParameters(rawQuery),
                    pathParameters(exchange),
                    httpMethod(exchange),
                    requestPath(exchange),
                    sanitizeQuery(rawQuery)
            );
            exchange.setProperty(REQUEST_SNAPSHOT_PROPERTY, snapshot);
            exchange.setProperty(ATTRIBUTES_PROPERTY, new LinkedHashMap<String, Object>());
        } catch (RuntimeException exception) {
            log.warn("TRACE enrichment failed spanName={} phase={} failureType={}",
                    "gateway.receive", "capture.request", exception.getClass().getSimpleName());
        }
    }

    public void recordGatewayRequestReceived(Exchange exchange) {
        if (exchange == null || exchange.getProperty(Message.GATEWAY_CHANNEL_PROTOCOL) != ProtocolType.REST) {
            return;
        }
        ObservationScope gatewayScope = exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_SCOPE_PROPERTY,
                ObservationScope.class
        );
        GatewayRequestSnapshot snapshot = exchange.getProperty(REQUEST_SNAPSHOT_PROPERTY, GatewayRequestSnapshot.class);
        if (gatewayScope == null || snapshot == null) {
            return;
        }
        try {
            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put(CommonTraceAttributes.EVENT_OUTCOME.name(), "success");
            put(attributes, CommonTraceAttributes.HTTP_METHOD.name(), snapshot.method());
            put(attributes, CommonTraceAttributes.URL_PATH.name(), snapshot.path());
            put(attributes, CommonTraceAttributes.URL_QUERY.name(), snapshot.query());
            put(attributes, CommonTraceAttributes.HTTP_REQUEST_BODY_SIZE.name(), snapshot.bodySize());
            REQUEST_HEADER_EVENT_ATTRIBUTES.forEach((header, attribute) -> put(attributes, attribute, snapshot.header(header)));
            gatewayScope.event("gateway.request.received", attributes);
        } catch (RuntimeException exception) {
            log.warn("TRACE enrichment failed spanName={} phase={} failureType={}",
                    "gateway.receive", "event.gateway.request.received", exception.getClass().getSimpleName());
        }
    }

    public void captureResponseAndExtract(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        try {
            Object body = immutableBodyCopy(exchange.getMessage().getBody());
            GatewayResponseSnapshot snapshot = new GatewayResponseSnapshot(
                    body,
                    bodySize(body),
                    safeHeaders(exchange.getMessage().getHeaders()),
                    exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class),
                    responseContentType(exchange)
            );
            exchange.setProperty(RESPONSE_SNAPSHOT_PROPERTY, snapshot);
            executeTraceRules(exchange, snapshot);
        } catch (RuntimeException exception) {
            log.warn("TRACE enrichment failed spanName={} phase={} failureType={}",
                    "gateway.receive", "capture.response", exception.getClass().getSimpleName());
        }
    }

    public void applyGatewaySpanAttributes(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        ObservationScope gatewayScope = exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_SCOPE_PROPERTY,
                ObservationScope.class
        );
        if (gatewayScope == null) {
            return;
        }
        Map<String, Object> attributes = observationAttributes(exchange);
        if (attributes.isEmpty()) {
            return;
        }
        try {
            gatewayScope.attributes(attributes);
        } catch (RuntimeException exception) {
            log.warn("TRACE enrichment failed spanName={} phase={} failureType={}",
                    "gateway.receive", "apply.attributes", exception.getClass().getSimpleName());
        }
    }

    public Set<String> authenticatedAttributeNames(Exchange exchange) {
        GatewayObservationDefinition definition = exchange == null
                ? null
                : exchange.getProperty(DEFINITION_PROPERTY, GatewayObservationDefinition.class);
        if (definition == null || definition.trace().isEmpty()) {
            return Set.of();
        }
        Set<String> names = new LinkedHashSet<>();
        for (GatewayObservationRule rule : definition.trace()) {
            if (rule.source() == ObservationSource.AUTH_CLAIM) {
                names.add(rule.path());
            }
        }
        return names.isEmpty() ? Set.of() : Collections.unmodifiableSet(names);
    }

    private List<ChannelServiceDefinition> observationDefinitions(RuntimeServicePlan servicePlan) {
        if (servicePlan == null || servicePlan.routeDefinitions() == null) {
            return List.of();
        }
        return servicePlan.routeDefinitions()
                .stream()
                .filter(definition -> definition != null
                        && definition.getType() == ChannelServiceDefinitionType.OBSERVATION)
                .toList();
    }

    private GatewayObservationDefinition compile(
            ChannelServiceDefinition channelServiceDefinition,
            DefinitionRegistrationContext context
    ) {
        Definition definition = channelServiceDefinition == null ? null : channelServiceDefinition.getDefinition();
        String payload = definition == null ? null : definition.getDetails();
        if (payload == null || payload.isBlank()) {
            invalid(context, "missingPayload");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (Exception exception) {
            invalid(context, "invalidJson");
            return GatewayObservationDefinition.empty();
        }
        if (root == null || !root.isObject()) {
            invalid(context, "definitionNotObject");
        }
        JsonNode versionNode = root.get("version");
        if (versionNode == null || !versionNode.isInt()) {
            invalid(context, "missingOrInvalidVersion");
        }
        int version = versionNode.asInt();
        if (version != SUPPORTED_VERSION) {
            invalid(context, "unsupportedVersion");
        }
        JsonNode trace = root.get("trace");
        if (trace == null || !trace.isArray()) {
            invalid(context, "traceNotArray");
        }
        List<GatewayObservationRule> rules = new ArrayList<>();
        Set<String> seenAttributes = new java.util.LinkedHashSet<>();
        int index = 0;
        for (JsonNode item : trace) {
            GatewayObservationRule rule = compileRule(item, context.withRuleIndex(index), seenAttributes);
            rules.add(rule);
            index++;
        }
        return new GatewayObservationDefinition(version, List.copyOf(rules));
    }

    private GatewayObservationRule compileRule(
            JsonNode item,
            DefinitionRegistrationContext context,
            Set<String> seenAttributes
    ) {
        if (item == null || !item.isObject()) {
            invalid(context, "invalidRuleStructure");
        }
        String attribute = stringField(item, "attribute", context, "missingAttribute");
        DefinitionRegistrationContext attributeContext = context.withAttribute(attribute);
        ObservationAttributeKey<?> attributeKey = attributeRegistry.findByName(ObservationStream.TRACE, attribute)
                .orElseThrow(() -> invalid(attributeContext, "unregisteredAttribute"));
        validateObservationAttribute(attributeKey, attributeContext);

        String from = stringField(item, "from", attributeContext, "missingFrom");
        DefinitionRegistrationContext sourceContext = attributeContext.withFrom(from);
        ObservationSource source = ObservationSource.from(from);
        if (source == null) {
            invalid(sourceContext, "unsupportedFrom");
        }
        if (source == ObservationSource.AUTH_CLAIM && !ObservationSource.AUTH_CLAIM.externalName.equals(
                item.get("from").asText())) {
            invalid(sourceContext, "unsupportedFrom");
        }

        boolean required = booleanField(item, "required", sourceContext, false);
        boolean overwrite = booleanField(item, "overwrite", sourceContext, false);
        String path = source == ObservationSource.AUTH_CLAIM
                ? opaquePathField(item, "path", sourceContext)
                : optionalStringField(item, "path", sourceContext, null);
        JsonNode valueNode = item.get("value");
        DefinitionRegistrationContext ruleContext = sourceContext.withPath(path);
        if (source.requiresPath() && path == null) {
            invalid(ruleContext, "missingPath");
        }
        if (source == ObservationSource.AUTH_CLAIM && unsafeAuthenticatedAttributeName(path)) {
            invalid(ruleContext, "unsafeAuthClaimPath");
        }
        if (source == ObservationSource.CONSTANT && missingNode(valueNode)) {
            invalid(ruleContext, "missingValue");
        }
        String assertedType = optionalStringField(item, "type", ruleContext, null);
        if (assertedType != null) {
            validateTypeAssertion(assertedType, attributeKey, ruleContext);
        }
        Object constantValue = source == ObservationSource.CONSTANT
                ? convertRegisteredValue(nodeValue(valueNode), attributeKey, ruleContext, "constantTypeMismatch")
                : null;
        Object defaultValue = missingNode(item.get("default"))
                ? null
                : convertRegisteredValue(nodeValue(item.get("default")), attributeKey, ruleContext, "defaultTypeMismatch");
        if (seenAttributes.contains(attribute) && !overwrite) {
            invalid(ruleContext, "duplicateAttribute");
        }
        seenAttributes.add(attribute);
        return new GatewayObservationRule(
                attribute,
                attributeKey,
                source,
                path,
                assertedType,
                required,
                defaultValue,
                overwrite,
                constantValue
        );
    }

    private void validateObservationAttribute(
            ObservationAttributeKey<?> attributeKey,
            DefinitionRegistrationContext context
    ) {
        String attribute = attributeKey.name();
        String normalized = attribute.toLowerCase(Locale.ROOT);
        if (RESERVED_OBSERVATION_ATTRIBUTES.contains(normalized)
                || !(normalized.startsWith("scm.service.") || normalized.startsWith("scm.status."))) {
            invalid(context, "reservedOrUnsupportedAttribute");
        }
        if (attributeKey.type().elasticType() == ElasticFieldType.OBJECT
                || List.class.isAssignableFrom(attributeKey.type().javaType())) {
            invalid(context, "nonScalarRegisteredAttribute");
        }
        String compact = compact(normalized);
        if (compact.contains("token")
                || compact.contains("authorization")
                || compact.contains("password")
                || compact.contains("secret")
                || compact.contains("apikey")
                || compact.contains("cookie")
                || compact.contains("credential")
                || hasSegment(normalized, "otp")
                || hasSegment(normalized, "pin")
                || hasSegment(normalized, "cvv")) {
            invalid(context, "unsafeAttribute");
        }
    }

    private void validateTypeAssertion(
            String assertedType,
            ObservationAttributeKey<?> attributeKey,
            DefinitionRegistrationContext context
    ) {
        RuleType ruleType = RuleType.from(assertedType);
        if (ruleType == null) {
            invalid(context, "unsupportedType");
        }
        if (!ruleType.compatibleWith(attributeKey.type().elasticType())) {
            invalid(context, "typeIncompatibleWithRegisteredAttribute");
        }
    }

    private void executeTraceRules(Exchange exchange, GatewayResponseSnapshot responseSnapshot) {
        GatewayObservationDefinition definition = exchange.getProperty(
                DEFINITION_PROPERTY,
                GatewayObservationDefinition.class
        );
        if (definition == null || definition.trace().isEmpty()) {
            return;
        }
        GatewayRequestSnapshot requestSnapshot = exchange.getProperty(REQUEST_SNAPSHOT_PROPERTY, GatewayRequestSnapshot.class);
        Map<String, Object> attributes = observationAttributes(exchange);
        for (GatewayObservationRule rule : definition.trace()) {
            try {
                Object value = extractValue(exchange, requestSnapshot, responseSnapshot, rule);
                if (value == null) {
                    value = rule.defaultValue();
                }
                if (value == null) {
                    if (rule.required()) {
                        warnRuleFailed(rule, "rule.required", "missingValue");
                    }
                    continue;
                }
                Object converted = convertRegisteredValue(value, rule.attributeKey(), null, "runtimeTypeMismatch");
                if (converted == null) {
                    continue;
                }
                if (attributes.containsKey(rule.attribute()) && !rule.overwrite()) {
                    continue;
                }
                attributes.put(rule.attribute(), converted);
            } catch (RuntimeException exception) {
                warnRuleFailed(rule, "rule.extract", exception.getClass().getSimpleName());
            }
        }
    }

    private Object extractValue(
            Exchange exchange,
            GatewayRequestSnapshot requestSnapshot,
            GatewayResponseSnapshot responseSnapshot,
            GatewayObservationRule rule
    ) {
        return switch (rule.source()) {
            case REQUEST_BODY -> jsonPointerValue(requestSnapshot == null ? null : requestSnapshot.body(), rule.path());
            case REQUEST_HEADER -> requestSnapshot == null ? null : requestSnapshot.header(ruleKey(rule.path()));
            case REQUEST_QUERY -> requestSnapshot == null ? null : requestSnapshot.queryParameter(ruleKey(rule.path()));
            case REQUEST_PATH -> requestSnapshot == null ? null : requestSnapshot.pathParameter(ruleKey(rule.path()));
            case RESPONSE_BODY -> jsonPointerValue(responseSnapshot == null ? null : responseSnapshot.body(), rule.path());
            case RESPONSE_HEADER -> responseSnapshot == null ? null : responseSnapshot.header(ruleKey(rule.path()));
            case EXCHANGE_PROPERTY -> exchange.getProperty(propertyKey(rule.path()));
            case AUTH_CLAIM -> ExchangeAuthenticationContext.authenticatedAttributes(exchange).value(rule.path());
            case CONSTANT -> rule.constantValue();
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> observationAttributes(Exchange exchange) {
        Map<String, Object> attributes = exchange.getProperty(ATTRIBUTES_PROPERTY, Map.class);
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
            exchange.setProperty(ATTRIBUTES_PROPERTY, attributes);
        }
        return attributes;
    }

    private Object jsonPointerValue(Object body, String pointer) {
        if (body == null || pointer == null || pointer.isBlank()) {
            return null;
        }
        JsonNode node = bodyNode(body);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String normalizedPointer = pointer.startsWith("/") ? pointer : "/" + pointer;
        return nodeValue(node.at(normalizedPointer));
    }

    private JsonNode bodyNode(Object body) {
        try {
            if (body == null) {
                return MissingNode.getInstance();
            }
            if (body instanceof JsonNode jsonNode) {
                return jsonNode;
            }
            if (body instanceof byte[] bytes) {
                return objectMapper.readTree(bytes);
            }
            if (body instanceof CharSequence text) {
                String candidate = text.toString();
                if (candidate.isBlank()) {
                    return MissingNode.getInstance();
                }
                return objectMapper.readTree(candidate);
            }
            return objectMapper.valueToTree(body);
        } catch (RuntimeException | java.io.IOException ignored) {
            return MissingNode.getInstance();
        }
    }

    private Object nodeValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return textOrNull(node.asText());
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.doubleValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        return objectMapper.convertValue(node, Object.class);
    }

    private Object convertRegisteredValue(
            Object value,
            ObservationAttributeKey<?> attributeKey,
            DefinitionRegistrationContext context,
            String failureReason
    ) {
        if (value == null) {
            return null;
        }
        Object normalized = value instanceof JsonNode jsonNode ? nodeValue(jsonNode) : value;
        if (isObjectOrArray(normalized)) {
            if (context != null) {
                invalid(context, "nonScalarValue");
            }
            throw new IllegalArgumentException("Non-scalar value for " + attributeKey.name());
        }
        try {
            return switch (attributeKey.type().elasticType()) {
                case KEYWORD, TEXT, DATE -> stringValue(normalized);
                case LONG -> longValue(normalized);
                case INTEGER -> integerValue(normalized);
                case DOUBLE -> doubleValue(normalized);
                case BOOLEAN -> booleanValue(normalized);
                case OBJECT -> {
                    if (context != null) {
                        invalid(context, "objectAttributeNotSupported");
                    }
                    throw new IllegalArgumentException("Object attribute is not supported");
                }
            };
        } catch (RuntimeException exception) {
            if (context != null) {
                invalid(context, failureReason);
            }
            throw exception;
        }
    }

    private boolean isObjectOrArray(Object value) {
        return value instanceof Map<?, ?>
                || value instanceof Collection<?>
                || value != null && value.getClass().isArray();
    }

    private String stringValue(Object value) {
        return textOrNull(String.valueOf(value));
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = textOrNull(String.valueOf(value));
        return text == null ? null : Long.valueOf(text);
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = textOrNull(String.valueOf(value));
        return text == null ? null : Integer.valueOf(text);
    }

    private Double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = textOrNull(String.valueOf(value));
        return text == null ? null : Double.valueOf(text);
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = textOrNull(String.valueOf(value));
        return text == null ? null : Boolean.valueOf(text);
    }

    private Object immutableBodyCopy(Object body) {
        if (body == null
                || body instanceof String
                || body instanceof Number
                || body instanceof Boolean
                || body instanceof BigDecimal
                || body instanceof TemporalAccessor) {
            return body;
        }
        if (body instanceof JsonNode jsonNode) {
            return jsonNode.deepCopy();
        }
        if (body instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (body instanceof ByteBuffer buffer) {
            ByteBuffer duplicate = buffer.asReadOnlyBuffer();
            byte[] bytes = new byte[duplicate.remaining()];
            duplicate.get(bytes);
            return bytes;
        }
        if (body instanceof StreamCache streamCache) {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                streamCache.writeTo(output);
                streamCache.reset();
                return output.toByteArray();
            } catch (Exception exception) {
                log.warn("TRACE enrichment failed spanName={} phase={} failureType={}",
                        "gateway.receive", "body.copy", exception.getClass().getSimpleName());
                return null;
            }
        }
        if (body instanceof InputStream || body instanceof Reader) {
            return null;
        }
        if (body instanceof Map<?, ?> || body instanceof Collection<?> || body.getClass().isArray()) {
            return objectMapper.valueToTree(body);
        }
        return objectMapper.valueToTree(body);
    }

    private long bodySize(Object body) {
        if (body == null) {
            return 0L;
        }
        try {
            if (body instanceof byte[] bytes) {
                return bytes.length;
            }
            if (body instanceof ByteBuffer buffer) {
                return buffer.asReadOnlyBuffer().remaining();
            }
            if (body instanceof CharSequence text) {
                return text.toString().getBytes(StandardCharsets.UTF_8).length;
            }
            if (body instanceof JsonNode jsonNode) {
                return objectMapper.writeValueAsBytes(jsonNode).length;
            }
            if (body instanceof Map<?, ?> || body instanceof Collection<?>) {
                return objectMapper.writeValueAsBytes(body).length;
            }
            return String.valueOf(body).getBytes(StandardCharsets.UTF_8).length;
        } catch (RuntimeException | java.io.IOException ignored) {
            return 0L;
        }
    }

    private Map<String, Object> safeHeaders(Map<String, Object> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> safe = new LinkedHashMap<>();
        headers.forEach((name, value) -> {
            String normalized = normalizeHeaderName(name);
            if (normalized != null && isSafeHeaderName(normalized)) {
                put(safe, normalized, headerValue(value));
            }
        });
        return Map.copyOf(safe);
    }

    private Object headerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            List<String> values = collection.stream()
                    .map(item -> textOrNull(item == null ? null : String.valueOf(item)))
                    .filter(item -> item != null)
                    .toList();
            return values.isEmpty() ? null : values;
        }
        return textOrNull(String.valueOf(value));
    }

    private boolean isSafeHeaderName(String normalized) {
        String compact = compact(normalized);
        return !compact.contains("authorization")
                && !compact.contains("cookie")
                && !compact.contains("password")
                && !compact.contains("credential")
                && !compact.contains("token")
                && !compact.contains("apikey")
                && !compact.contains("secret")
                && !compact.contains("otp")
                && !hasSegment(normalized, "pin")
                && !hasSegment(normalized, "cvv");
    }

    private String normalizeHeaderName(String name) {
        return name == null || name.isBlank() ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    private Map<String, Object> queryParameters(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (String pair : rawQuery.split("&", -1)) {
            if (pair.isBlank()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String name = normalizeMapKey(decodeQueryComponent(separator >= 0 ? pair.substring(0, separator) : pair));
            String value = separator >= 0 ? decodeQueryComponent(pair.substring(separator + 1)) : "";
            if (name == null) {
                continue;
            }
            Object existing = values.get(name);
            if (existing instanceof List<?> list) {
                List<Object> appended = new ArrayList<>(list);
                appended.add(value == null ? "" : value);
                values.put(name, List.copyOf(appended));
            } else if (existing != null) {
                values.put(name, List.of(existing, value == null ? "" : value));
            } else {
                values.put(name, value == null ? "" : value);
            }
        }
        return Map.copyOf(values);
    }

    private String sanitizeQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }
        List<String> pairs = new ArrayList<>();
        for (String pair : rawQuery.split("&", -1)) {
            if (pair.isBlank()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String rawName = separator >= 0 ? pair.substring(0, separator) : pair;
            String decodedName = decodeQueryComponent(rawName);
            if (isSensitiveParameterName(decodedName)) {
                pairs.add(rawName + "=[REDACTED]");
            } else {
                pairs.add(pair);
            }
        }
        return pairs.isEmpty() ? null : String.join("&", pairs);
    }

    private boolean isSensitiveParameterName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String compact = compact(normalized);
        return compact.contains("token")
                || compact.contains("authorization")
                || compact.contains("password")
                || compact.contains("secret")
                || compact.contains("apikey")
                || compact.contains("otp")
                || compact.contains("cookie")
                || compact.contains("credential")
                || hasSegment(normalized, "pin")
                || hasSegment(normalized, "cvv");
    }

    private String decodeQueryComponent(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    private Map<String, Object> pathParameters(Exchange exchange) {
        Object value = exchange.getProperty(Message.INBOUND_PARAMETERS);
        if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> path = new LinkedHashMap<>();
        map.forEach((name, item) -> put(path, normalizeMapKey(String.valueOf(name)), item));
        return Map.copyOf(path);
    }

    private String rawQuery(Exchange exchange) {
        return firstText(
                exchange.getMessage().getHeader(Exchange.HTTP_QUERY, String.class),
                queryFromUri(exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class)),
                queryFromUri(exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class))
        );
    }

    private String queryFromUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }
        try {
            return URI.create(uri).getQuery();
        } catch (IllegalArgumentException ignored) {
            int index = uri.indexOf('?');
            return index < 0 || index + 1 >= uri.length() ? null : uri.substring(index + 1);
        }
    }

    private String httpMethod(Exchange exchange) {
        return firstText(
                exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class),
                exchange.getMessage().getHeader(Exchange.HTTP_METHOD, String.class)
        );
    }

    private String requestPath(Exchange exchange) {
        return safePath(firstText(
                exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class),
                exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class),
                exchange.getMessage().getHeader(Exchange.HTTP_PATH, String.class)
        ));
    }

    private String safePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return URI.create(value).getPath();
        } catch (IllegalArgumentException ignored) {
            return value.contains("?") ? value.substring(0, value.indexOf('?')) : value;
        }
    }

    private String responseContentType(Exchange exchange) {
        return firstText(
                exchange.getMessage().getHeader(Exchange.CONTENT_TYPE, String.class),
                exchange.getMessage().getHeader("Content-Type", String.class)
        );
    }

    private String stringField(JsonNode item, String fieldName, DefinitionRegistrationContext context, String reason) {
        String value = optionalStringField(item, fieldName, context, reason);
        if (value == null) {
            invalid(context, reason);
        }
        return value;
    }

    private String optionalStringField(JsonNode item, String fieldName, DefinitionRegistrationContext context, String reason) {
        JsonNode node = item.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            invalid(context, reason);
        }
        return textOrNull(node.asText());
    }

    private String opaquePathField(JsonNode item, String fieldName, DefinitionRegistrationContext context) {
        JsonNode node = item.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            invalid(context, "invalidPath");
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private boolean booleanField(
            JsonNode item,
            String fieldName,
            DefinitionRegistrationContext context,
            boolean defaultValue
    ) {
        JsonNode node = item.get(fieldName);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (!node.isBoolean()) {
            invalid(context, "invalid" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1));
        }
        return node.asBoolean(defaultValue);
    }

    private boolean missingNode(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull();
    }

    private String ruleKey(String path) {
        String value = textOrNull(path);
        if (value == null) {
            return null;
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value.isBlank() ? null : value.toLowerCase(Locale.ROOT);
    }

    private String propertyKey(String path) {
        String value = textOrNull(path);
        if (value == null) {
            return null;
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value.isBlank() ? null : value;
    }

    private String normalizeMapKey(String value) {
        String text = textOrNull(value);
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }

    private boolean unsafeAuthenticatedAttributeName(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = normalizeAuthenticatedAttributeName(path);
        if (UNSAFE_AUTHENTICATED_ATTRIBUTE_NAMES.contains(normalized)) {
            return true;
        }
        int separator = Math.max(path.lastIndexOf('.'), path.lastIndexOf('/'));
        return separator >= 0 && UNSAFE_AUTHENTICATED_ATTRIBUTE_NAMES.contains(
                normalizeAuthenticatedAttributeName(path.substring(separator + 1))
        );
    }

    private String normalizeAuthenticatedAttributeName(String value) {
        return value == null
                ? ""
                : value.trim()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s./-]+", "_");
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String text = textOrNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (attributes != null && name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
    }

    private boolean hasSegment(String normalized, String segment) {
        String[] parts = normalized.split("[._\\-\\s/]+");
        for (String part : parts) {
            if (segment.equals(part)) {
                return true;
            }
        }
        return false;
    }

    private String compact(String value) {
        return value == null
                ? ""
                : value.replace(".", "")
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .toLowerCase(Locale.ROOT);
    }

    private String duplicateDefinitionIds(List<ChannelServiceDefinition> definitions) {
        return definitions.stream()
                .map(ChannelServiceDefinition::getId)
                .map(value -> value == null ? "<null>" : value)
                .toList()
                .toString();
    }

    private DefinitionRegistrationContext registrationContext(
            RuntimeRoutePlan routePlan,
            RuntimeServicePlan servicePlan,
            InboundRouteDefinition inboundRoute,
            String definitionId
    ) {
        GatewayChannel gateway = servicePlan == null ? null : servicePlan.gatewayChannel();
        Service service = servicePlan == null ? null : servicePlan.service();
        return new DefinitionRegistrationContext(
                inboundRoute == null || inboundRoute.route() == null ? null : inboundRoute.route().getRouteId(),
                gateway == null ? null : gateway.getName(),
                gatewayChannelCode(gateway, servicePlan),
                service == null ? null : service.getCode(),
                inboundRoute == null ? null : inboundRoute.serviceVersion(),
                gateway == null || gateway.getProtocolType() == null ? null : gateway.getProtocolType().name(),
                definitionId,
                null,
                null,
                null,
                null
        );
    }

    private String gatewayChannelCode(GatewayChannel gateway, RuntimeServicePlan servicePlan) {
        if (gateway != null && gateway.getChannel() != null && gateway.getChannel().getCode() != null) {
            return gateway.getChannel().getCode();
        }
        if (servicePlan != null
                && servicePlan.channelServiceAccess() != null
                && servicePlan.channelServiceAccess().getChannel() != null) {
            return servicePlan.channelServiceAccess().getChannel().getCode();
        }
        return null;
    }

    private IllegalStateException invalid(DefinitionRegistrationContext context, String reason) {
        log.error("event=gateway.observation.definition.invalid routeId={} gatewayName={} channelCode={} serviceCode={} serviceVersion={} protocol={} definitionId={} ruleIndex={} attribute={} from={} path={} reason={}",
                context.routeId(),
                context.gatewayName(),
                context.channelCode(),
                context.serviceCode(),
                context.serviceVersion(),
                context.protocol(),
                context.definitionId(),
                context.ruleIndex(),
                context.attribute(),
                context.from(),
                context.path(),
                reason);
        throw new IllegalStateException("Invalid gateway OBSERVATION definition: reason=" + reason
                + ", routeId=" + context.routeId()
                + ", gatewayName=" + context.gatewayName()
                + ", channelCode=" + context.channelCode()
                + ", serviceCode=" + context.serviceCode()
                + ", serviceVersion=" + context.serviceVersion()
                + ", protocol=" + context.protocol()
                + ", definitionId=" + context.definitionId()
                + ", ruleIndex=" + context.ruleIndex()
                + ", attribute=" + context.attribute()
                + ", from=" + context.from()
                + ", path=" + context.path());
    }

    private void warnRuleFailed(GatewayObservationRule rule, String phase, String failureType) {
        log.warn("TRACE enrichment rule skipped spanName={} phase={} attribute={} source={} failureType={}",
                "gateway.receive", phase, rule.attribute(), rule.source().externalName(), failureType);
    }

    public record GatewayObservationDefinition(int version, List<GatewayObservationRule> trace) {
        private static final GatewayObservationDefinition EMPTY = new GatewayObservationDefinition(SUPPORTED_VERSION, List.of());

        public GatewayObservationDefinition {
            trace = trace == null || trace.isEmpty() ? List.of() : List.copyOf(trace);
        }

        static GatewayObservationDefinition empty() {
            return EMPTY;
        }

        public boolean requiresBodyExtraction() {
            return trace.stream().anyMatch(rule -> rule.source().isBody());
        }
    }

    public record GatewayObservationRule(
            String attribute,
            ObservationAttributeKey<?> attributeKey,
            ObservationSource source,
            String path,
            String assertedType,
            boolean required,
            Object defaultValue,
            boolean overwrite,
            Object constantValue
    ) {
    }

    private record GatewayRequestSnapshot(
            Object body,
            long bodySize,
            Map<String, Object> headers,
            Map<String, Object> queryParameters,
            Map<String, Object> pathParameters,
            String method,
            String path,
            String query
    ) {
        Object header(String name) {
            return name == null ? null : headers.get(name.toLowerCase(Locale.ROOT));
        }

        Object queryParameter(String name) {
            return name == null ? null : queryParameters.get(name);
        }

        Object pathParameter(String name) {
            return name == null ? null : pathParameters.get(name);
        }
    }

    private record GatewayResponseSnapshot(
            Object body,
            long bodySize,
            Map<String, Object> headers,
            Integer statusCode,
            String contentType
    ) {
        Object header(String name) {
            return name == null ? null : headers.get(name.toLowerCase(Locale.ROOT));
        }
    }

    private record DefinitionRegistrationContext(
            String routeId,
            String gatewayName,
            String channelCode,
            String serviceCode,
            String serviceVersion,
            String protocol,
            String definitionId,
            Integer ruleIndex,
            String attribute,
            String from,
            String path
    ) {
        DefinitionRegistrationContext withDefinition(String definitionId) {
            return new DefinitionRegistrationContext(
                    routeId, gatewayName, channelCode, serviceCode, serviceVersion, protocol,
                    definitionId, ruleIndex, attribute, from, path);
        }

        DefinitionRegistrationContext withRuleIndex(int ruleIndex) {
            return new DefinitionRegistrationContext(
                    routeId, gatewayName, channelCode, serviceCode, serviceVersion, protocol,
                    definitionId, ruleIndex, attribute, from, path);
        }

        DefinitionRegistrationContext withAttribute(String attribute) {
            return new DefinitionRegistrationContext(
                    routeId, gatewayName, channelCode, serviceCode, serviceVersion, protocol,
                    definitionId, ruleIndex, attribute, from, path);
        }

        DefinitionRegistrationContext withFrom(String from) {
            return new DefinitionRegistrationContext(
                    routeId, gatewayName, channelCode, serviceCode, serviceVersion, protocol,
                    definitionId, ruleIndex, attribute, from, path);
        }

        DefinitionRegistrationContext withPath(String path) {
            return new DefinitionRegistrationContext(
                    routeId, gatewayName, channelCode, serviceCode, serviceVersion, protocol,
                    definitionId, ruleIndex, attribute, from, path);
        }
    }

    public enum ObservationSource {
        REQUEST_BODY("request.body", true),
        REQUEST_HEADER("request.header", false),
        REQUEST_QUERY("request.query", false),
        REQUEST_PATH("request.path", false),
        RESPONSE_BODY("response.body", true),
        RESPONSE_HEADER("response.header", false),
        EXCHANGE_PROPERTY("exchange.property", false),
        AUTH_CLAIM("auth.claim", false),
        CONSTANT("constant", false);

        private final String externalName;
        private final boolean body;

        ObservationSource(String externalName, boolean body) {
            this.externalName = externalName;
            this.body = body;
        }

        String externalName() {
            return externalName;
        }

        boolean isBody() {
            return body;
        }

        boolean requiresPath() {
            return this != CONSTANT;
        }

        static ObservationSource from(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            if (AUTH_CLAIM.externalName.equals(value)) {
                return AUTH_CLAIM;
            }
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            for (ObservationSource source : values()) {
                if (source == AUTH_CLAIM) {
                    continue;
                }
                if (source.externalName.equals(normalized)) {
                    return source;
                }
            }
            return null;
        }
    }

    private enum RuleType {
        STRING,
        LONG,
        INTEGER,
        DOUBLE,
        BOOLEAN;

        static RuleType from(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "string", "text", "keyword", "date" -> STRING;
                case "long" -> LONG;
                case "int", "integer" -> INTEGER;
                case "double", "decimal", "number" -> DOUBLE;
                case "boolean", "bool" -> BOOLEAN;
                default -> null;
            };
        }

        boolean compatibleWith(ElasticFieldType elasticFieldType) {
            return switch (this) {
                case STRING -> elasticFieldType == ElasticFieldType.KEYWORD
                        || elasticFieldType == ElasticFieldType.TEXT
                        || elasticFieldType == ElasticFieldType.DATE;
                case LONG -> elasticFieldType == ElasticFieldType.LONG;
                case INTEGER -> elasticFieldType == ElasticFieldType.INTEGER;
                case DOUBLE -> elasticFieldType == ElasticFieldType.DOUBLE;
                case BOOLEAN -> elasticFieldType == ElasticFieldType.BOOLEAN;
            };
        }
    }
}
