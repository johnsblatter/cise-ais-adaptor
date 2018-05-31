package eu.cise.adaptor;

import eu.cise.servicemodel.v1.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

/**
 * Main class in the domain module. The run() method start the process of
 * reading from the selected AisSource, translate the ais info into cise
 * messages and dispatch the cise messages through a rest protocol to the
 * gateway.
 * <p></p>
 * Three phases are composing the transformation:
 * - toFluxString: open the source and feed the flux stream of ais messages in
 * string format
 * - toCiseMessages: transform the string in {NMEAMessage}, then in {AISMessage}
 * and finally in {AisMsg}
 * - dispatchMessages: using multi threading dispatches the cise messages
 * to the gateway absorbing peaks
 */
public class AisApp implements Runnable {

    private final AdaptorConfig config;
    private final AisSource aisSource;

    private final Dispatcher dispatcher;
    private final StreamProcessor streamProcessor;

    public AisApp(AisSource aisSource,
                  StreamProcessor streamProcessor,
                  Dispatcher dispatcher,
                  AdaptorConfig config) {
        this.aisSource = aisSource;
        this.dispatcher = dispatcher;
        this.streamProcessor = streamProcessor;
        this.config = config;
    }

    @Override
    public void run() {
        dispatchMessages(toCiseMessages(toFluxString(openAisSource())));
    }

    /**
     * The publishOn allows to be flexible.
     *
     * @param messageStream
     */
    private void dispatchMessages(Flux<Message> messageStream) {
        messageStream
                .publishOn(Schedulers.elastic())
                .doOnNext(msg -> dispatcher.send(msg, config.getGatewayAddress()))
                .doOnError(e -> e.printStackTrace())
                .blockLast();
    }

    private Flux<Message> toCiseMessages(Flux<String> aisStringFlux) {
        return streamProcessor.process(aisStringFlux);
    }

    private Stream<String> openAisSource() {
        return aisSource.open();
    }

    private Flux<String> toFluxString(Stream<String> source) {
        return Flux.fromStream(source);
    }

}