package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import java.util.Map;

public class ChargingBehaviourScoringEvent extends Event implements HasPersonId {
    
    public enum ScoreTrigger{
        ACTIVITYSTART, 
        ACTIVITYEND
    }

    public static final String EVENT_TYPE = "scoring";

    private Id<Person> personId;
    private Double soc;
    private Double walkingDistance;
    private String activityType;
    private Double startSoc;
    private Double chargingDuration;
    private Double pluggedDuration;
    private ScoreTrigger scoreTrigger;

    public ChargingBehaviourScoringEvent(
        double time,
        Id<Person> personId,
        String activityType,
        Double soc,
        Double startSoc,
        Double walkingDistance,
        Double chargingDuration,
        Double pluggedDuration, 
        ScoreTrigger scoreTrigger        
        ) 
    {
        super(time);
        this.personId = personId;
        this.activityType = activityType;
        this.soc = soc;
        this.startSoc = startSoc;
        this.walkingDistance = walkingDistance;
        this.chargingDuration = chargingDuration;
        this.pluggedDuration = pluggedDuration;
        this.scoreTrigger = scoreTrigger;
    }

    @Override
    public String getEventType() { return EVENT_TYPE; }

    @Override
    public Id<Person> getPersonId() { return personId; }

    public String getActivityType() { return activityType; }

    public Double getSoc() { return soc; }

    public Double getStartSoc() { return startSoc; }

    public Double getWalkingDistance() { return walkingDistance; }

    public Double getChargingDuration() { return chargingDuration; }
    
    public Double getPluggedDuration() { return pluggedDuration; }

    public ScoreTrigger getScoreTrigger() { return scoreTrigger; }

    @Override
    public Map<String, String> getAttributes() {
        
        Map<String, String> attributes = super.getAttributes();
        
        attributes.put("activityType", getActivityType());
        attributes.put("soc", getSoc().toString());
        attributes.put("startSoc", getStartSoc().toString());
        attributes.put("walkingDistance", getWalkingDistance().toString());
        attributes.put("chargingDuration", getChargingDuration().toString());
        attributes.put("pluggedDuration", getPluggedDuration().toString());
        attributes.put("scoreTrigger", getScoreTrigger().name());

        return attributes;
    }

}
