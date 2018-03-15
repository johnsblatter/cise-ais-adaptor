package eu.cise.adaptor;

import dk.tbsalling.aismessages.ais.messages.AISMessage;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import eu.cise.adaptor.tbs.TbsAISNormalizer;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// !ABVDM,2,1,2,A,5DSFVl02=s8qK8E3H00h4pLDpE=<000000000017ApB>;=qA0J11EmSP0000,0*36
// !ABVDM,2,2,2,A,00000000000,2*2D
//
// {toStern=18, metadata=Metadata{source='SRC', received=2018-02-15T09:52:24.986Z}, destination=DEWVN, imo.IMO=9301134, toPort=14, dataTerminalReady=false, nmeaMessages=[Ldk.tbsalling.aismessages.nmea.messages.NMEAMessage;@6e5df971, shipName=LANGENESS, sourceMmsi.MMSI=305506000, positionFixingDevice=CombinedGpsGlonass, valid=true, eta=18-07 17:00, draught=10.4, messageType=ShipAndVoyageRelatedData, toStarboard=11, callsign=V2EP6, shipType=CargoHazardousA, toBow=143, repeatIndicator=1, transponderClass=A}

public class AISNormalizerMsg5Test {

    private TbsAISNormalizer n;

    AISMessage voyageMsg() {
        return AISMessage.create(
                NMEAMessage.fromString(
                        "!ABVDM,2,1,2,A,5DSFVl02=s8qK8E3H00h4pLDpE=<000000000017ApB>;=qA0J11EmSP0000,0*36"),
                NMEAMessage.fromString(
                        "!ABVDM,2,2,2,A,00000000000,2*2D")
        );
    }

    @Before
    public void before() {
        n = new TbsAISNormalizer();
    }

    @Test
    public void it_maps_voyage_message_type() {
        assertThat(n.normalize(voyageMsg()).getMessageType(), is(5));

    }

    @Test
    public void it_maps_voyage_message() {
        assertThat(n.normalize(voyageMsg()).getDestination(), is("DEWVN"));
    }





}
