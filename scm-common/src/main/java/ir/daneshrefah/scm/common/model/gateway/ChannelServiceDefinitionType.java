package ir.daneshrefah.scm.common.model.gateway;

public enum ChannelServiceDefinitionType {
    INBOUND_ROUTE,
    INBOUND_ROUTE_GROUP,
    API_DOCUMENTATION,
    SERVICE_DOMAIN_MEMBER,

    @Deprecated(forRemoval = false)
    REST,
    @Deprecated(forRemoval = false)
    REST_MULTIPLE,
    @Deprecated(forRemoval = false)
    SWAGGER,
}
