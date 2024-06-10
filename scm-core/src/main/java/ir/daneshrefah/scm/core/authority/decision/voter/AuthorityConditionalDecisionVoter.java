package ir.daneshrefah.scm.core.authority.decision.voter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.condition.Condition;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.util.SimpleMethodInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
public class AuthorityConditionalDecisionVoter extends BaseSingularConditionalDecisionVoter {

    private static final MethodInvocation METHOD_INVOCATION;

    static {
        try {
            METHOD_INVOCATION = new SimpleMethodInvocation("test", String.class.getMethod("toString"));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthorityConditionalDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected int checkCondition(Message message, Condition condition) {
        Authentication authentication = (Authentication) message.getHeader().getAuthentication();
        Expression expression = new SpelExpressionParser().parseExpression(condition.getValue());
        EvaluationContext context = new DefaultMethodSecurityExpressionHandler().createEvaluationContext(authentication, METHOD_INVOCATION);
        boolean isGranted = ExpressionUtils.evaluateAsBoolean(expression, context);
        if (!isGranted) {
            return ACCESS_DENIED;
        }
        return ACCESS_ABSTAIN;
    }

    @Override
    protected ConditionType getConditionType() {
        return ConditionType.AUTHORITY;
    }

    public List<JsonNode> parseExpression(String expression) {
        List<JsonNode> nodes = new ArrayList<>();
        Pattern functionPattern = Pattern.compile("\\w+\\(([^)]*)\\)");
        Pattern operatorPattern = Pattern.compile("(and|or)");

        Matcher matcher = functionPattern.matcher(expression);
        int lastMatchEnd = 0; // Track last matched ending position
        while (matcher.find()) {
            String fullMatch = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            if (start > lastMatchEnd) { // Handle operators between functions
                String operator = expression.substring(lastMatchEnd, start);
                if (operatorPattern.matcher(operator).matches()) {
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.put("operator", operator);
                    node.put("type", "operator");
                    nodes.add(node);
                } else {
                    throw new RuntimeException("Unexpected token between functions: " + operator);
                }
            }

            String functionName = fullMatch.substring(0, fullMatch.indexOf("("));
            String arguments = matcher.group(1); // Extract arguments

            // ... Handle escaped characters and function details as before
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("functionName", functionName);
            node.put("type", "functionName");
            node.put("arguments", arguments);

            nodes.add(node);
            lastMatchEnd = end;
        }

        // Check for remaining operators at the end
        String remainingExpression = expression.substring(lastMatchEnd);
        if (operatorPattern.matcher(remainingExpression).matches()) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("operator", remainingExpression);
            node.put("type", "operator");
            nodes.add(node);
        } else if (!remainingExpression.isEmpty()) {
            throw new RuntimeException("Unexpected token at the end: " + remainingExpression);
        }

        return nodes;
    }

}
