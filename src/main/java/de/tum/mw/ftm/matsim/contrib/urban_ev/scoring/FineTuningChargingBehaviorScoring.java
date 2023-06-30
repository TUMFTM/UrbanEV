package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import com.google.inject.Inject;

import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent.ScoreTrigger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.stats.ChargingBehaviorScoresCollector;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PersonUtils;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ScoreComponents;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.SumScoringFunction;

public class FineTuningChargingBehaviorScoring implements SumScoringFunction.ArbitraryEventScoring {

    private double score;

    private static final double logn_residual_utility_walking = Math.log(0.001);

    private ChargingBehaviorScoresCollector chargingBehaviorScoresCollector = ChargingBehaviorScoresCollector.getInstance();
    
    private final Id<Person> personId;
    private final boolean opportunityCharging;
    private final boolean hasChargerAtHome;
    private final boolean hasChargerAtWork;
    

    final ChargingBehaviourScoringParameters params;
    Person person;

    @Inject
    public FineTuningChargingBehaviorScoring(final ChargingBehaviourScoringParameters params, Person person) {
        
        // scoring params
        this.params = params;
        
        // person 
        this.personId =  person.getId();
        this.person = person;

        this.hasChargerAtHome = PersonUtils.hasHomeCharger(person);
        this.hasChargerAtWork = PersonUtils.hasWorkCharger(person);
        this.opportunityCharging = PersonUtils.isOpportunityCharging(person);

        PersonUtils.setAttr(person, "rangeAnxietyScoredAlready", false);
        
    }

    @Override
    public void handleEvent(Event event) {

        if(event instanceof ChargingBehaviourScoringEvent){
        
            // parse event
            ChargingBehaviourScoringEvent chargingBehaviourScoringEvent = (ChargingBehaviourScoringEvent) event;
            double time = chargingBehaviourScoringEvent.getTime();
            String activityType = chargingBehaviourScoringEvent.getActivityType();
            
            // make sure this is not called on unspecified/ini activities
            if(!PlanUtils.isIniAct(activityType))
            {
                ScoreTrigger scoreTrigger = chargingBehaviourScoringEvent.getScoreTrigger(); 

                double soc = chargingBehaviourScoringEvent.getSoc();
                double socDiff = chargingBehaviourScoringEvent.getSocDiff();

                boolean isEndAct = PlanUtils.isEndAct(activityType);
                boolean isActStart = scoreTrigger==ScoreTrigger.ACTIVITYSTART;
                boolean isActEnd = scoreTrigger==ScoreTrigger.ACTIVITYEND;
                boolean isCharging = PlanUtils.isCharging(activityType);
                boolean isFailed = PlanUtils.isFailed(activityType);

                boolean isHome = PlanUtils.isHome(activityType);
                boolean isWork = PlanUtils.isWork(activityType);
                
                boolean isPrivateCharging = isCharging&&((isHome&&hasChargerAtHome)||(isWork&&hasChargerAtWork)); 
                boolean isPublicCharging = !isPrivateCharging;

                // punish empty battery at any chance
                if(soc==0 && (isActStart || isActEnd))
                {
                    score += scoreEmptyBattery(soc,time);
                }

                // scoring on charging start acts
                if(isActStart && isCharging)
                {
                    double walkingDistance = chargingBehaviourScoringEvent.getWalkingDistance();
                    
                    // punish walking distance if
                    // a person charges and has to walk longer than the assumed baseline distance 
                    // and is not a person that charges publicly at home due to not having a private charger at home
                    if (walkingDistance>params.referenceParkingDistance) { // Todo: Re-evaluate -> && !(!hasChargerAtHome && activityType.contains("home"))
                        score += scoreWalking(walkingDistance,time);                
                    }

                }
                
                // scoring on charging end acts
                if(isActEnd && isCharging)
                {
                    // Scoring of charging hogging                    
                    if(
                        chargingBehaviourScoringEvent.isHogging() && // If the vehicle is plugged for an excessive duration
                        isPublicCharging // and charging was performed publicly
                        ) 
                    {
                        score += scoreStationHogging(time);
                    }
           
                }

                if(isActEnd && isFailed)
                {
                    score += scoreFailedCharging(time);
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

    private double scoreFailedCharging(double time)
    {
        double delta_score = params.marginalUtilityOfWalking_m*1.01; // A little more than walking the maximum distance would cost
        collectScores(personId, time, ScoreComponents.FAILED_CHARGING, delta_score);

        return delta_score;
    }

    private double scoreStationHogging(double time)
    {
        double delta_score = params.marginalUtilityOfStationHogging;
        collectScores(personId, time, ScoreComponents.STATION_HOGGING, delta_score);

        return delta_score;
    }

    private double scoreEmptyBattery(double soc, double time)
    {
        // empty battery
        double delta_score = params.utilityOfEmptyBattery;
        collectScores(personId, time, ScoreComponents.EMPTY_BATTERY, delta_score);
            
        // Add all critical agents to the criticalSOC subpopulation such that they get replanned
        PersonUtils.setCritical(person);

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

        for(Activity act:PlanUtils.getActivities(person.getSelectedPlan())){

            if(PlanUtils.isCharging(act) && !PlanUtils.isFailed(act))
            {
                boolean isPrivateHomeCharging = PlanUtils.isHome(act) && hasChargerAtHome;
                boolean isPrivateWorkCharging = PlanUtils.isWork(act) && hasChargerAtWork;

                if(!isPrivateHomeCharging && !isPrivateWorkCharging)
                {
                    // plan contains a successful opportunity charging activity in case there is a charging activity that
                    // is non-home, non-work, did not fail and is not the last activity
                    planContainsSuccessfulOpportunityCharging = true;
                    break;
                }
            }
            

        }

        return planContainsSuccessfulOpportunityCharging;

    }

}
