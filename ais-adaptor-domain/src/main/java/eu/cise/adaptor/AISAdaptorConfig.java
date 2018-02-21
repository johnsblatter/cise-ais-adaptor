package eu.cise.adaptor;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:ais-adaptor.properties")
public interface AISAdaptorConfig extends Config {

    @Key("gateway.address")
    String getGatewayAddress();

    @Key("service.id")
    String getServiceId();

    @Key("service.data-freshness-type")
    String getDataFreshnessType();

    @Key("service.sea-basin-type")
    String getSeaBasinType();

    @Key("service.participant.url")
    String getEndpointUrl();

    @Key("service.operation")
    String getServiceOperation();

    @Key("message.priority")
    String getMessagePriority();

    @Key("message.security-level")
    String getSecurityLevel();

    @Key("message.sensitivity")
    String getSensitivity();

    @Key("message.purpose")
    String getPurpose();
}
