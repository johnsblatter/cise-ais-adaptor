package eu.cise.adaptor;

import dk.tbsalling.aismessages.ais.messages.AISMessage;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import eu.cise.adaptor.translate.AisMessageToAisMsg;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * The two following ais messages are unmarshalled with the following values
 * <pre>
 * !ABVDM,2,1,2,A,5DSFVl02=s8qK8E3H00h4pLDpE=<000000000017ApB>;=qA0J11EmSP0000,0*36
 * !ABVDM,2,2,2,A,00000000000,2*2D
 * {
 *  toStern: 18,
 *  metadata: {
 *      source: 'SRC',
 *      received: '2018-02-15T09:52:24.986Z'
 *  },
 *  destination: 'DEWVN',
 *  imo.IMO: 9301134,
 *  toPort: 14,
 *  dataTerminalReady: false,
 *  nmeaMessages: [{
 *      shipName: 'LANGENESS',
 *      sourceMmsi.MMSI: 305506000,
 *      positionFixingDevice: 'CombinedGpsGlonass',
 *      valid: true,
 *      eta: '18-07 17: 00',
 *      draught: 10.4,
 *      messageType: 'ShipAndVoyageRelatedData',
 *      toStarboard: 11,
 *      callsign: 'V2EP6',
 *      shipType: 'CargoHazardousA',
 *      toBow: 143,
 *      repeatIndicator: 1,
 *      transponderClass: 'A'
 *  }]
 * }
 * </pre>
 * for all month, day, hours, minutes.
 */
public class AisNormalizerMsg5Test {

    private AisMessageToAisMsg n;

    AISMessage voyageMsg() {
        return AISMessage.create(
                NMEAMessage.fromString(
                        "!ABVDM,2,1,2,A,5DSFVl02=s8qK8E3H00h4pLDpE=<000000000017ApB>;=qA0J11EmSP0000,0*36"),
                NMEAMessage.fromString(
                        "!ABVDM,2,2,2,A,00000000000,2*2D")
        );
    }

    AISMessage voyageMsgNoMonthDay() {
        return AISMessage.create(
                NMEAMessage.fromString(
                        "!AIVDM,2,1,8,B,56?cVR42>RBT5HAJ220<N3;3:22222222222220n5Hk7540Ht02CQ2@H8888,0*66"),
                NMEAMessage.fromString(
                        "!AIVDM,2,2,8,B,88888888880,2*2F")
        );
    }

    @Before
    public void before() {
        n = new AisMessageToAisMsg();
    }

    @Test
    public void it_maps_voyage_message_type() {
        assertThat(n.translate(voyageMsg()).getMessageType(), is(5));
    }

    @Test
    public void it_maps_voyage_message_destination() {
        assertThat(n.translate(voyageMsg()).getDestination(), is("DEWVN"));
    }


    // ETA:
    //    Estimated time of arrival; MMDDHHMM UTC
    //    Bits 19-16: month; 1-12; 0 = not available = default
    //    Bits 15-11: day; 1-31; 0 = not available = default
    //    Bits 10-6: hour; 0-23; 24 = not available = default
    //    Bits 5-0: minute; 0-59; 60 = not available = default
    @Test
    // eta=18-07 17:00
    public void it_maps_voyage_message_ETA_on_the_next_year() {
        Clock beforeJuly2018 = Clock.fixed(Instant.parse("2018-05-18T17:00:00.00Z"), ZoneId.systemDefault());

        n = new AisMessageToAisMsg(beforeJuly2018);

        assertThat(n.translate(voyageMsg()).getEta(), is(Instant.parse("2018-07-18T17:00:00.00Z")));
    }

    @Test
    // eta=18-07 17:00
    public void it_maps_voyage_message_ETA_on_the_current_year() {
        Clock afterJuly2018 = Clock.fixed(Instant.parse("2018-10-18T17:00:00.00Z"), ZoneId.systemDefault());

        n = new AisMessageToAisMsg(afterJuly2018);

        assertThat(n.translate(voyageMsg()).getEta(), is(Instant.parse("2019-07-18T17:00:00.00Z")));
    }

    //00-00 24:60
    @Test
    public void it_maps_voyage_message_ETA_on_1970_when_month_and_day_are_not_available() {
        assertThat(n.translate(voyageMsgNoMonthDay()).getEta(), is(Instant.parse("1970-01-01T00:00:00.00Z")));
    }

    @Test
    public void it_maps_voyage_message_imo_number() {
        assertThat(n.translate(voyageMsg()).getImoNumber(), is(9301134));
    }

    @Test
    public void it_maps_voyage_message_call_sign() {
        assertThat(n.translate(voyageMsg()).getCallSign(), is("V2EP6"));
    }

    @Test
    public void it_maps_voyage_message_draught() {
        assertThat(n.translate(voyageMsg()).getDraught(), is(10.4F));
    }

    @Test
    public void it_maps_voyage_message_dimension_C() {
        assertThat(n.translate(voyageMsg()).getDimensionC(), is(14));
    }

    @Test
    public void it_maps_voyage_message_dimension_D() {
        assertThat(n.translate(voyageMsg()).getDimensionD(), is(11));
    }

    @Test
    public void it_maps_voyage_message_dimension_A() {
        assertThat(n.translate(voyageMsg()).getDimensionA(), is(143));
    }

    @Test
    public void it_maps_voyage_message_dimension_B() {
        assertThat(n.translate(voyageMsg()).getDimensionB(), is(18));
    }

    @Test
    //CargoHazardousA(71)
    public void it_maps_voyage_message_ship_type() {
        assertThat(n.translate(voyageMsg()).getShipType(), is(71));
    }

    @Test
    public void it_maps_voyage_message_ship_name() {
        assertThat(n.translate(voyageMsg()).getShipName(), is("LANGENESS"));
    }
}