package ir.daneshrefah.scm.common.model.gateway;

public enum RoutingStrategy {
    FIRST,
    FAIL_OVER,
    CHAIN_ON_APPROVE,
    ACTION_DISPATCH,
    TASK_WORKFLOW
}
