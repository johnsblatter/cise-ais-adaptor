package eu.cise.adaptor;

import com.greghaskins.spectrum.Spectrum;
import eu.cise.adaptor.translate.AISTranslator;
import eu.cise.adaptor.translate.DefaultAISTranslator;
import eu.cise.datamodel.v1.entity.location.Geometry;
import eu.cise.datamodel.v1.entity.object.Objet;
import eu.cise.datamodel.v1.entity.vessel.Vessel;
import eu.cise.servicemodel.v1.message.Push;
import eu.cise.servicemodel.v1.message.XmlEntityPayload;
import org.aeonbits.owner.ConfigFactory;
import org.junit.runner.RunWith;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.greghaskins.spectrum.Spectrum.describe;
import static com.greghaskins.spectrum.Spectrum.it;
import static eu.cise.adaptor.helpers.Utils.extractPayload;
import static eu.cise.adaptor.helpers.Utils.extractVessel;
import static eu.cise.adaptor.normalize.NavigationStatus.UnderwayUsingEngine;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Spectrum.class)
public class AISMessageTranslatorSpec {
    {
        describe("an AIS to CISE message translator", () -> {

            AISAdaptorConfig config = ConfigFactory.create(AISAdaptorConfig.class);
            AISTranslator translator = new DefaultAISTranslator(config);

            describe("when a message type is not supported", () -> {
                asList(4, 6, 7, 8, 9, 10, 11).forEach((n) ->
                        it("returns an empty optional / " + n, () -> {
                            assertThat(translator.translate(new AISMsg.Builder(8).build()), is(Optional.empty()));
                        })
                );
            });

            final AISMsg m = new AISMsg.Builder(1)
                    .withLatitude(47.443634F)
                    .withLongitude(-6.9895167F)
                    .withPositionAccuracy(1)
                    .withCOG(2119.0F)
                    .withTrueHeading(210)
                    .withTimestamp(Instant.parse("2018-02-19T14:43:16.550Z"))
                    .withSOG(138.0F)
                    .withMMSI(538005989)
                    .withNavigationStatus(UnderwayUsingEngine)
                    .build();

            describe("when a message type is 1,2,3 or 5", () -> {
                final Vessel v = extractVessel(translator.translate(m));

                asList(1, 2, 3, 5).forEach((n) ->
                        it("returns an optional with a push message / " + n, () ->
                                assertThat(translator.translate(new AISMsg.Builder(n).build()),
                                        is(not(Optional.empty()))))
                );

                it("returns an Optional<Push> with a vessel", () -> {

                    XmlEntityPayload payload = extractPayload(translator.translate(m));
                    assertThat("The XmlEntityPayload has not been created",
                            payload, is(notNullValue()));

                    List<Object> vessels = payload.getAnies();
                    assertThat("There must be at least one vessel element i the payload",
                            vessels, is(not(empty())));

                    assertThat("The element in the payload must be a Vessel",
                            vessels.get(0), instanceOf(Vessel.class));
                });

            });

        });
    }

}

