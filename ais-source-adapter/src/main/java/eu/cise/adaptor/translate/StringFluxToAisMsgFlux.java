package eu.cise.adaptor.translate;

import eu.cise.adaptor.AisMsg;
import eu.cise.adaptor.StringToAisMsg;
import reactor.core.publisher.Flux;

import java.util.Optional;

@SuppressWarnings("ununsed")
public class StringFluxToAisMsgFlux implements StringToAisMsg {

    private final StringToNmea stringToNmea;
    private final NmeaToAISMessage nmeaToAISMessage;
    private final AisMessageToAisMsg aisMessageToAisMsg;

    public StringFluxToAisMsgFlux() {
        this.stringToNmea = new StringToNmea();
        this.nmeaToAISMessage = new NmeaToAISMessage("SRC");
        this.aisMessageToAisMsg = new AisMessageToAisMsg();
    }

    @SuppressWarnings("ununsed")
    public StringFluxToAisMsgFlux(StringToNmea stringToNmea,
                                  NmeaToAISMessage nmeaToAISMessage,
                                  AisMessageToAisMsg aisMessageToAISMsg) {
        this.stringToNmea = stringToNmea;
        this.nmeaToAISMessage = nmeaToAISMessage;
        this.aisMessageToAisMsg = aisMessageToAISMsg;
    }

    @Override
    public Flux<AisMsg> translate(Flux<String> stringFlux) {
        return stringFlux
                .map(stringToNmea::translate)
                .map(nmeaToAISMessage::translate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(aisMessageToAisMsg::translate);
    }
}
