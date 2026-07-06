package ir.daneshrefah.scm.core.integration.gateway.inbound;

public record InboundRouteActionConfig(
        boolean configured,
        String inboundAction
) {
    public static InboundRouteActionConfig absent() {
        return new InboundRouteActionConfig(false, null);
    }
}
