package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ClientContractVersionResolver {
    public static final String DEFAULT_VERSION = "v1";

    private static final Pattern VERSION_PATTERN = Pattern.compile("v[1-9][0-9]*");
    private static final Pattern PATH_VERSION_PATTERN = Pattern.compile("^/?(v[1-9][0-9]*)(?:/.*)?$");

    private final ObjectMapper objectMapper;

    public String resolve(ChannelServiceDefinition routeDefinition) {
        return resolve(routeDefinition, null, null);
    }

    public String resolve(ChannelServiceDefinition routeDefinition,
                          ChannelServiceDefinition parentDefinition,
                          String contextPath) {
        String explicitVersion = explicitVersion(routeDefinition);
        if (explicitVersion != null) {
            return explicitVersion;
        }
        explicitVersion = explicitVersion(parentDefinition);
        if (explicitVersion != null) {
            return explicitVersion;
        }
        return inferFromPath(effectivePath(contextPath, routePath(routeDefinition)));
    }

    public String validate(String version) {
        String normalized = StringUtils.trimToNull(version);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if (!VERSION_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Client contract version must match vN: " + version);
        }
        return normalized;
    }

    private String explicitVersion(ChannelServiceDefinition definition) {
        JsonNode root = details(definition);
        if (root == null || !root.hasNonNull("version")) {
            return null;
        }
        return validate(root.path("version").asText(null));
    }

    private String inferFromPath(String path) {
        String normalizedPath = StringUtils.trimToEmpty(path);
        Matcher matcher = PATH_VERSION_PATTERN.matcher(normalizedPath);
        if (matcher.matches()) {
            return matcher.group(1).toLowerCase(Locale.ROOT);
        }
        return DEFAULT_VERSION;
    }

    private String effectivePath(String contextPath, String routePath) {
        String normalizedContextPath = stripPath(contextPath);
        String normalizedRoutePath = stripPath(routePath);
        if (normalizedContextPath == null) {
            return normalizedRoutePath;
        }
        if (normalizedRoutePath == null) {
            return normalizedContextPath;
        }
        return normalizedContextPath + "/" + normalizedRoutePath;
    }

    private String stripPath(String path) {
        String normalizedPath = StringUtils.trimToNull(path);
        if (normalizedPath == null) {
            return null;
        }
        normalizedPath = StringUtils.strip(normalizedPath, "/");
        return StringUtils.trimToNull(normalizedPath);
    }

    private String routePath(ChannelServiceDefinition definition) {
        if (definition instanceof InboundChannelServiceDefinition restDefinition) {
            return restDefinition.getPath();
        }
        JsonNode root = details(definition);
        return root != null ? StringUtils.trimToNull(root.path("path").asText(null)) : null;
    }

    private JsonNode details(ChannelServiceDefinition definition) {
        if (definition == null
                || definition.getDefinition() == null
                || StringUtils.isBlank(definition.getDefinition().getDetails())) {
            return null;
        }
        try {
            return objectMapper.readTree(definition.getDefinition().getDetails());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid route definition details for client contract version "
                    + definition.getId(), e);
        }
    }
}
