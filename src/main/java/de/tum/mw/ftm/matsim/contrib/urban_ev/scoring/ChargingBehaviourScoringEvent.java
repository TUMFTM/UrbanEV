package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import java.util.Map;

public class ChargingBehaviourScoringEvent extends Event implements HasPersonId {
    public static final String EVENT_TYPE = "scoring";

    private Id<Person> personId;
    private Double soc;
    private Double walkingDistance;
    private String activityType;
    private Double startSoc;

    public ChargingBehaviourScoringEvent(double time, Id<Person> personId, Double soc, Double walkingDistance,
                                         String activityType, double startSoc) {
        super(time);
        this.personId = personId;
        this.soc = soc;
        this.walkingDistance = walkingDistance;
        this.activityType = activityType;
        this.startSoc = startSoc;
    }

    @Override
    public Id<Person> getPersonId() {
        return personId;
    }

    public Double getSoc() {
        return soc;
    }

    public Double getWalkingDistance() {
        return walkingDistance;
    }

    public String getActivityType() { return activityType; }

    public Double getStartSoc() { return startSoc; }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }


    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = super.getAttributes();
        attributes.put("soc", getSoc().toString());
        attributes.put("walkingDistance", getWalkingDistance().toString());
        attributes.put("activityType", getActivityType());
        attributes.put("startSoc", getStartSoc().toString());
        return attributes;
    }

}
