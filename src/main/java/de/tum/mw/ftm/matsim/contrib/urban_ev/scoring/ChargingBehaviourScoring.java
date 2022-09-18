package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import com.google.inject.Inject;
import de.tum.mw.ftm.matsim.contrib.urban_ev.stats.ChargingBehaviorScoresCollector;
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
        OPPORTUNITY_CHARGING
    }

    private double score;
    private boolean opportunityCharging;
    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String LAST_ACT_IDENTIFIER = " end";
    private ChargingBehaviorScoresCollector chargingBehaviorScoresCollector = ChargingBehaviorScoresCollector.getInstance();

    final ChargingBehaviourScoringParameters params;
    Person person;

    @Inject
    public ChargingBehaviourScoring(final ChargingBehaviourScoringParameters params, Person person) {
        this.params = params;
        this.person = person;
        String opportunityCharging_str = person.getAttributes().getAttribute("opportunityCharging").toString();
        this.opportunityCharging = opportunityCharging_str.equals("true") ? true : false; 
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals("scoring")) {
            ChargingBehaviourScoringEvent chargingBehaviourScoringEvent = (ChargingBehaviourScoringEvent) event;

            double soc = chargingBehaviourScoringEvent.getSoc();
            String activityType = chargingBehaviourScoringEvent.getActivityType();

            // punish soc below threshold
            double rangeAnxietyThreshold = Double.parseDouble(person.getAttributes().getAttribute("rangeAnxietyThreshold").toString());
            if (soc > 0 && soc < rangeAnxietyThreshold) {
                double delta_score = params.marginalUtilityOfRangeAnxiety_soc * (rangeAnxietyThreshold - soc) / rangeAnxietyThreshold;
                chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.RANGE_ANXIETY, delta_score);
                chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.RANGE_ANXIETY, person.getId());
                score += delta_score;
            }

            // severely punish empty battery
            if (soc == 0) {

                double delta_score = params.utilityOfEmptyBattery;
                chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.EMPTY_BATTERY, delta_score);
                chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.EMPTY_BATTERY, person.getId());
                score += delta_score;
            }

            boolean hasChargerAtHome = person.getAttributes().getAttribute("homeChargerPower") != null;

            // punish walking distance if
            // a person charges and has to walk (i.e. does not use a private charger) and is not a person that charges publicly at home due to not having a private charger at home
            double walkingDistance = chargingBehaviourScoringEvent.getWalkingDistance();
            if (activityType.contains(CHARGING_IDENTIFIER) && walkingDistance>0) { // Todo: Re-evaluate -> && !(!hasChargerAtHome && activityType.contains("home"))
                    
                    // inverted utility based on Geurs, van Wee 2004 Equation (1)
                    double beta = 0.005;
                    double delta_score = params.marginalUtilityOfWalking_m * (1 - Math.exp(-beta * walkingDistance));
                    chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.WALKING_DISTANCE, delta_score);
                    chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.WALKING_DISTANCE, person.getId());
                    score += delta_score;                
            }
            
            // reward charging at home
            
            if (activityType.equals("home" + CHARGING_IDENTIFIER) && hasChargerAtHome) {
                double delta_score = params.utilityOfHomeCharging;
                chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.HOME_CHARGING, delta_score);
                chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.HOME_CHARGING, person.getId());
                score += delta_score;
            }

            // punish difference between end soc and start soc to get realistic soc distribution
            if (activityType.contains(LAST_ACT_IDENTIFIER)) {
                
                // Calculate SOC difference
                Double soc_diff =  soc - chargingBehaviourScoringEvent.getStartSoc();
                if(soc_diff<=0){
                    // Only punish soc difference if SOC is smaller than at the beginning of the cycle.
                    double delta_score = params.marginalUtilityOfSocDifference * Math.abs(soc_diff);
                    chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.ENERGY_BALANCE, delta_score);
                    score += delta_score;
                } else
                {
                    chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.ENERGY_BALANCE, 0);
                }
                chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.ENERGY_BALANCE, person.getId());
            }

            // punish if opportunity charging person fails to opportunity charge during the simulation
            if (activityType.contains(LAST_ACT_IDENTIFIER)){
                
                String opportunityChargingPerson_str = person.getAttributes().getAttribute("opportunityCharging") != null ? person.getAttributes().getAttribute("opportunityCharging").toString() : "false";
                boolean opportunityChargingPerson = opportunityChargingPerson_str.equals("true") ? true : false;
                double delta_score = 0;

                if(!opportunityChargingPerson){
                    delta_score=0;
                }
                else{
                    if(!successfulOpportunityCharging(person)){
                        // agent failed to opportunity charge even though it should have
                        delta_score = params.failedOpportunityChargingUtility;
                        chargingBehaviorScoresCollector.addScoringComponentValue(ScoreComponents.OPPORTUNITY_CHARGING, delta_score);
                        chargingBehaviorScoresCollector.addScoringPerson(ScoreComponents.OPPORTUNITY_CHARGING, person.getId());
                        score += delta_score;
                    }
                    else{
                        // person successfully performed opportunity charging
                        delta_score=0;
                    }
                }
                

            }

        }
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
                
                if(
                    ((!actType.contains("home")&&!actType.contains("work")&&!actType.equals(""))||actType.contains("work_related"))
                    &&
                    actType.contains(CHARGING_IDENTIFIER)
                    &&
                    !actType.contains("failed")
                    &&
                    !actType.contains(LAST_ACT_IDENTIFIER)
                    )
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
