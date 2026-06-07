package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GatewayRoutePipelineConfigurerFailureContextTest {

    @Test
    void failureContextPrefersExchangeProperties() throws Exception {
        GatewayRoutePipelineConfigurer builder = builder();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        Service service = new Service();
        service.setCode("card-inquiry");
        exchange.setProperty(Message.CHANNEL_CODE, "mb");
        exchange.setProperty(Message.SERVICE, service);

        assertThat(invoke(builder, "failureChannelCode", exchange)).isEqualTo("mb");
        assertThat(invoke(builder, "failureServiceCode", exchange)).isEqualTo("card-inquiry");
    }

    @Test
    void failureContextFallsBackToRuntimeServicePlan() throws Exception {
        GatewayRoutePipelineConfigurer builder = builder();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(Message.RUNTIME_SERVICE_PLAN, servicePlan("ib", "balance-inquiry"));

        assertThat(invoke(builder, "failureChannelCode", exchange)).isEqualTo("ib");
        assertThat(invoke(builder, "failureServiceCode", exchange)).isEqualTo("balance-inquiry");
    }

    @Test
    void failureContextAllowsMissingValues() throws Exception {
        GatewayRoutePipelineConfigurer builder = builder();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());

        assertThat(invoke(builder, "failureChannelCode", exchange)).isNull();
        assertThat(invoke(builder, "failureServiceCode", exchange)).isNull();
    }

    private GatewayRoutePipelineConfigurer builder() {
        return new GatewayRoutePipelineConfigurer(
                mock(ClientContractResolver.class),
                Map.<String, RequestContractDecoder>of(),
                mock(ServiceRouteUriResolver.class),
                new ScmExchangeMdc(),
                mock(IncomingChannelCodeResolver.class));
    }

    private RuntimeServicePlan servicePlan(String channelCode, String serviceCode) {
        Channel channel = new Channel();
        channel.setCode(channelCode);
        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setChannel(channel);
        Service service = new Service();
        service.setCode(serviceCode);
        return new RuntimeServicePlan(new GatewayChannel(), access, service, List.of());
    }

    private String invoke(GatewayRoutePipelineConfigurer builder, String methodName, Exchange exchange) throws Exception {
        Method method = GatewayRoutePipelineConfigurer.class.getDeclaredMethod(methodName, Exchange.class);
        method.setAccessible(true);
        return (String) method.invoke(builder, exchange);
    }
}
