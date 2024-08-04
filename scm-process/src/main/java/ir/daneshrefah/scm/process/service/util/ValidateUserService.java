package ir.daneshrefah.scm.process.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class ValidateUserService {

    private final ObjectMapper objectMapper;

    public boolean isUserAuthorized(Map<String, Object> variables, String userName, List<String> authorizedUsers) {
        if (Objects.nonNull(authorizedUsers) && !authorizedUsers.isEmpty()) {
            for (String authorizedUser : authorizedUsers) {
                if (authorizedUser.contains(".")) {
                    String variableName = authorizedUser.substring(0, authorizedUser.indexOf("."));
                    Object variableValue = variables.get(variableName);
                    if (variableValue != null) {
                        JsonNode jsonNode = objectMapper.valueToTree(variableValue);
                        String key = authorizedUser.substring(authorizedUser.indexOf(".") + 1);
                        if (validateAuthorizedUser(jsonNode, key.split("\\."), 0, userName)) {
                            return true;
                        }
                    }
                } else if (variables.containsKey(authorizedUser)) {
                    if (variables.get(authorizedUser).equals(userName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isUserInAuthorizedRoles(List<String> roles) {
        return Objects.requireNonNull(AuthenticationUtils.getLoggedInUserAuthentication()).getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> roles.contains(grantedAuthority.getAuthority()));
    }

    private boolean validateAuthorizedUser(JsonNode node, String[] keys, int index, String username) {
        if (index >= keys.length) {
            return false;
        }
        String key = keys[index];
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                if (validateAuthorizedUser(element, keys, index, username)) {
                    return true;
                }
            }
        }
        if (!node.has(key)) {
            return false;
        }
        node = node.get(key);
        if (index == keys.length - 1) {
            return node.asText().equals(username);
        }
        return validateAuthorizedUser(node, keys, index + 1, username);
    }
}
