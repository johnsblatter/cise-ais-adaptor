package eu.cise.adaptor;

import eu.cise.datamodel.v1.entity.location.Geometry;
import eu.cise.datamodel.v1.entity.location.Location;
import eu.cise.datamodel.v1.entity.object.Objet;
import eu.cise.datamodel.v1.entity.object.SensorType;
import eu.cise.datamodel.v1.entity.object.SourceType;
import eu.cise.datamodel.v1.entity.vessel.NavigationalStatusType;
import eu.cise.datamodel.v1.entity.vessel.Vessel;
import eu.cise.servicemodel.v1.authority.SeaBasinType;
import eu.cise.servicemodel.v1.message.InformationSecurityLevelType;
import eu.cise.servicemodel.v1.message.InformationSensitivityType;
import eu.cise.servicemodel.v1.message.PurposeType;
import eu.cise.servicemodel.v1.message.Push;
import eu.cise.servicemodel.v1.service.DataFreshnessType;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static eu.cise.adaptor.NavigationStatus.UnderwayUsingEngine;
import static eu.cise.servicemodel.v1.message.PriorityType.LOW;
import static eu.cise.servicemodel.v1.service.ServiceOperationType.PUSH;
import static eu.eucise.helpers.ParticipantBuilder.newParticipant;
import static eu.eucise.helpers.PushBuilder.newPush;
import static eu.eucise.helpers.ServiceBuilder.newService;

/**
 * This is the translator from the internal AISMsg object to a CISE Push message
 * <p>
 * TODO There is a difference in latitude and longitude between the AIS and the
 * CISE calculation. Here for simplicity it hasn't been taken into account.
 * <p>
 * Please refer to:
 * https://webgate.ec.europa.eu/CITnet/confluence/display/MAREX/AIS+Message+1%2C2%2C3
 */
public class Translator {
    public Optional<Push> translate(AISMsg aisMsg) {
        if (isTypeSupported(aisMsg)) {
            return Optional.empty();
        }

        return Optional.of(newPush()
                .id(UUID.randomUUID().toString())
                .contextId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .creationDateTime(new Date())
                .sender(newService()
                        .id("123")
                        .dataFreshness(DataFreshnessType.HISTORIC)
                        .seaBasin(SeaBasinType.ARCTIC_OCEAN)
                        .operation(PUSH)
                        .participant(newParticipant())
                        .build())
                .priority(LOW)
                .isRequiresAck(false)
                .informationSecurityLevel(InformationSecurityLevelType.NON_CLASSIFIED)
                .informationSensitivity(InformationSensitivityType.NON_SPECIFIED)
                .isPersonalData(false)
                .purpose(PurposeType.BORDER_MONITORING)
                .addEntity(toVessel(
                        latitude(aisMsg),
                        longitude(aisMsg),
                        fromCourseOverGround(aisMsg.getCOG()),  // casting float to double
                        fromTrueHeading(aisMsg.getTrueHeading()),
                        f2d(aisMsg.getSOG()),  // casting float to double
                        Long.valueOf(aisMsg.getMMSI()),
                        fromNavigationStatus(aisMsg.getNavigationStatus())
                        )
                )

                .build());
    }

    private Vessel toVessel(String latitude,
                            String longitude,
                            Double cog,
                            Double heading,
                            Double sog,
                            Long mmsi,
                            NavigationalStatusType nst) {

        Vessel vessel = new Vessel();
        vessel.setMMSI(mmsi);
        vessel.getLocationRels().add(getLocationRel(latitude, longitude, cog, heading, sog));
        vessel.setNavigationalStatus(nst);
        return vessel;
    }

    /**
     * TODO Implement the mapping of all the Navigation Status cases
     *
     * @param ns
     * @return
     */
    private NavigationalStatusType fromNavigationStatus(NavigationStatus ns) {
        if (ns == null || !ns.equals(UnderwayUsingEngine))
            return NavigationalStatusType.UNDEFINED_DEFAULT;
        else
            return NavigationalStatusType.UNDER_WAY_USING_ENGINE;
    }

    private Objet.LocationRel getLocationRel(String latitude,
                                             String longitude,
                                             Double cog,
                                             Double heading,
                                             Double sog) {

        Objet.LocationRel locationRel = new Objet.LocationRel();
        locationRel.setLocation(toLocation(latitude, longitude));
        locationRel.setCOG(cog);
        locationRel.setHeading(heading);
        locationRel.setSourceType(SourceType.DECLARATION);
        locationRel.setSensorType(SensorType.AUTOMATIC_IDENTIFICATION_SYSTEM);
        locationRel.setSOG(sog);

        return locationRel;
    }

    private String longitude(AISMsg aisMsg) {
        return Float.toString(aisMsg.getLongitude());
    }

    private String latitude(AISMsg aisMsg) {
        return Float.toString(aisMsg.getLatitude());
    }

    private Location toLocation(String latitude, String longitude) {
        Location location = new Location();
        Geometry geometry = new Geometry();
        geometry.setLatitude(latitude);
        geometry.setLongitude(longitude);
        location.getGeometries().add(geometry);
        return location;
    }

    private boolean isTypeSupported(AISMsg aisMessage) {
        return aisMessage.getMessageType() != 1 &&
                aisMessage.getMessageType() != 2 &&
                aisMessage.getMessageType() != 3 &&
                aisMessage.getMessageType() != 5;
    }

    private Double f2d(Float fValue) {
        return Double.valueOf(fValue.toString());
    }

    private Double fromCourseOverGround(Float cog) {
        return cog == 3600 ? null : f2d(cog);
    }

    private Double fromTrueHeading(int th) {
        return th == 511 ? null : Double.valueOf(th);
    }

}

