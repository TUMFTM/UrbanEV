package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import com.google.inject.Inject;

import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent.ScoreTrigger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.stats.ChargingBehaviorScoresCollector;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PersonUtils;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.scoring.SumScoringFunction;

public class ChargingBehaviourScoring implements SumScoringFunction.ArbitraryEventScoring {

    public enum ScoreComponents {
        RANGE_ANXIETY,
        EMPTY_BATTERY,
        WALKING_DISTANCE,
        HOME_CHARGING,
        ENERGY_BALANCE, 
        OPPORTUNITY_CHARGING, 
        STATION_HOGGING, 
        BATTERY_HEALTH
    }

    private double score;

    private static final String CRITICAL_SOC_IDENTIFIER = "criticalSOC";
    private static final double logn_residual_utility_walking = Math.log(0.001);
    private static final double logn_residual_utility_rangeAnxiety = Math.log(0.00001);

    private ChargingBehaviorScoresCollector chargingBehaviorScoresCollector = ChargingBehaviorScoresCollector.getInstance();
    
    private final Id<Person> personId;
    private final boolean opportunityCharging;
    private final boolean hasChargerAtHome;
    private final boolean hasChargerAtWork;
    

    final ChargingBehaviourScoringParameters params;
    Person person;

    @Inject
    public ChargingBehaviourScoring(final ChargingBehaviourScoringParameters params, Person person) {
        
        // scoring params
        this.params = params;
        
        // person 
        this.personId =  person.getId();
        this.person = person;

        this.hasChargerAtHome = PersonUtils.hasHomeCharger(person);
        this.hasChargerAtWork = PersonUtils.hasWorkCharger(person);
        this.opportunityCharging = PersonUtils.isOpportunityCharging(person);
        
    }

    @Override
    public void handleEvent(Event event) {

        if(event instanceof ChargingBehaviourScoringEvent){
        
            // parse event
            ChargingBehaviourScoringEvent chargingBehaviourScoringEvent = (ChargingBehaviourScoringEvent) event;
            double time = chargingBehaviourScoringEvent.getTime();
            String activityType = chargingBehaviourScoringEvent.getActivityType();
            boolean isCharging = PlanUtils.isCharging(activityType);
            boolean isIniAct = PlanUtils.isIniAct(activityType);
            boolean isEndAct = PlanUtils.isEndAct(activityType);

            // make sure this is not called on unspecified/ini activities
            if(!isIniAct)
            {
                ScoreTrigger scoreTrigger = chargingBehaviourScoringEvent.getScoreTrigger();
                double soc = chargingBehaviourScoringEvent.getSoc();
                boolean isActStart = scoreTrigger==ScoreTrigger.ACTIVITYSTART;
                boolean isActEnd = scoreTrigger==ScoreTrigger.ACTIVITYEND;
                boolean isHome = PlanUtils.isHome(activityType);
                boolean isWork = PlanUtils.isWork(activityType);

                // punish empty battery at any chance
                if(soc==0 && (isActStart || isActEnd))
                {
                    score += scoreEmptyBattery(soc,time);
                }

                // punish battery health stress after any charging activity
                if(soc>params.optimalSOC && isActEnd && isCharging)
                {
                    score += scoreBatteryHealth(soc,time);
                }

                // punish range anxiety after any activity
                if(soc>0.0 && soc<= params.optimalSOC && isActEnd)
                {
                    score += scoreRangeAnxiety(soc,time);
                }

                // scoring of location choices
                if(isActStart && isCharging)
                {
                    double walkingDistance = chargingBehaviourScoringEvent.getWalkingDistance();
                    
                    // punish walking distance if
                    // a person charges and has to walk longer than the assumed baseline distance 
                    // and is not a person that charges publicly at home due to not having a private charger at home
                    if (walkingDistance>params.referenceParkingDistance) { // Todo: Re-evaluate -> && !(!hasChargerAtHome && activityType.contains("home"))
                        score += scoreWalking(walkingDistance,time);                
                    }

                    // reward charging at home
                    if (hasChargerAtHome&&isHome) {
                        score += scoreHomeCharging(time);
                    }

                }
                
                if(isActEnd && isCharging)
                {
                    // Scoring of charging hogging                    
                    if(
                        chargingBehaviourScoringEvent.isHogging() && // If the vehicle is plugged for an excessive duration
                        !((isHome && hasChargerAtHome) || (isWork && hasChargerAtWork)) // and charging was performed publicly
                        ) 
                    {
                        score += scoreStationHogging(time);
                    }
                        
                }
                
                // Scoring on last activity
                if (isEndAct)
                {
                    score += scoreEnergyBalance(soc, chargingBehaviourScoringEvent.getStartSoc(),time);

                    if(opportunityCharging && !successfulOpportunityCharging(person)){
                        score += scoreOpportunityCharging(time);
                    }

                }
            }
            
        }
        
    }

    private void collectScores(Id<Person> personId, double time, ScoreComponents scoreComponent, double value)
    {
        ChargingBehaviorScoresCollector.getInstance().collect(
            personId,
            time,
            scoreComponent,
            value);
    }

    private double scoreWalking(double walkingDistance, double time)
    {

        // inverted utility based on Geurs, van Wee 2004 Equation (1)
        double additionalWalkingThroughCharging = walkingDistance-params.referenceParkingDistance;
        double additionalWalkingMaxCase = params.parkingSearchRadius-params.referenceParkingDistance;
        
        double delta_score = params.marginalUtilityOfWalking_m * (1 - Math.exp(logn_residual_utility_walking * (additionalWalkingThroughCharging/additionalWalkingMaxCase)));
        
        collectScores(personId, time, ScoreComponents.WALKING_DISTANCE, delta_score);
        
        return delta_score;
    }

    private double scoreHomeCharging(double time)
    {
        // reward charging at home
        double delta_score = params.utilityOfHomeCharging;
        collectScores(personId, time, ScoreComponents.HOME_CHARGING, delta_score);

        return delta_score;
    }

    private double scoreStationHogging(double time)
    {
        double delta_score = params.marginalUtilityOfStationHogging;
        collectScores(personId, time, ScoreComponents.STATION_HOGGING, delta_score);

        return delta_score;
    }

    private double scoreEnergyBalance(double soc, double startSoc, double time)
    {
        // Calculate SOC difference
        double soc_diff =  Math.abs(soc - startSoc);
        double delta_score = params.marginalUtilityOfSocDifference * Math.abs(soc_diff);
        
        collectScores(personId, time, ScoreComponents.ENERGY_BALANCE, delta_score);
        
        return delta_score;
    }

    private double scoreBatteryHealth(double soc, double time)
    {

        // battery health (soc>params.optimalSOC)
        double delta_score = ((soc-params.optimalSOC)/(1.0-params.optimalSOC))*params.batteryHealthStressUtility;
        collectScores(personId, time, ScoreComponents.BATTERY_HEALTH, delta_score);
        
        return delta_score;

    }

    private double scoreRangeAnxiety(double soc, double time)
    {

        // range anxiety
        double delta_score = params.marginalUtilityOfRangeAnxiety_soc * Math.exp(logn_residual_utility_rangeAnxiety*(soc/params.optimalSOC));
        collectScores(personId, time, ScoreComponents.RANGE_ANXIETY, delta_score);
        
        if(soc<=params.criticalSOCThreshold)
        {
            // Add all critical agents to the criticalSOC subpopulation such that they get replanned
            person.getAttributes().putAttribute("subpopulation", CRITICAL_SOC_IDENTIFIER);
        }
        
        return delta_score; 
    }

    private double scoreEmptyBattery(double soc, double time)
    {
        // empty battery
        double delta_score = params.utilityOfEmptyBattery;
        collectScores(personId, time, ScoreComponents.EMPTY_BATTERY, delta_score);
            
        // Add all critical agents to the criticalSOC subpopulation such that they get replanned
        person.getAttributes().putAttribute("subpopulation", CRITICAL_SOC_IDENTIFIER);

        return delta_score;
    }

    private double scoreOpportunityCharging(double time)
    {
        // agent failed to opportunity charge even though it should have
        double delta_score = params.failedOpportunityChargingUtility;   
        collectScores(personId, time, ScoreComponents.OPPORTUNITY_CHARGING, delta_score);
        
        return delta_score;
    }

    @Override public void finish() {}

    @Override
    public double getScore() {
        return score;
    }

    private boolean successfulOpportunityCharging(Person person){
        
        boolean planContainsSuccessfulOpportunityCharging = false;

        for(PlanElement pe:person.getSelectedPlan().getPlanElements()){

            if (pe instanceof Activity) {

                Activity act = (Activity) pe;
                String actType = act.getType();
                
                boolean isSuccessfulCharging = PlanUtils.isCharging(actType) && !actType.contains("failed");

                if(isSuccessfulCharging)
                {
                    boolean isPrivateHomeCharging = actType.contains("home") && hasChargerAtHome;
                    boolean isWorkCharging = actType.contains("work") && !actType.contains("work_related");
                    boolean isPrivateWorkCharging = isWorkCharging && hasChargerAtWork;

                    if(!isPrivateHomeCharging && !isPrivateWorkCharging)
                    {
                        // plan contains a successful opportunity charging activity in case there is a charging activity that
                        // is non-home, non-work, did not fail and is not the last activity
                        planContainsSuccessfulOpportunityCharging = true;
                        break;
                    }
                }
                
            }

        }

        return planContainsSuccessfulOpportunityCharging;

    }

}
