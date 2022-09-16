package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEventHandler;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeChargingBehaviourModule implements PlanStrategyModule, ChargingBehaviourScoringEventHandler {

    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String CHARGING_FAILED_IDENTIFIER = " charging failed";

    private Random random = new Random();
    private Scenario scenario;
    private Population population;
    private UrbanEVConfigGroup evCfg;
    private int maxNumberSimultaneousPlanChanges;
    private Double timeAdjustmentProbability;
    private int maxTimeFlexibility;

    ChangeChargingBehaviourModule(Scenario scenario) {
        this.scenario = scenario;
        this.population = this.scenario.getPopulation();
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
        this.maxNumberSimultaneousPlanChanges = evCfg.getMaxNumberSimultaneousPlanChanges();
        this.timeAdjustmentProbability = evCfg.getTimeAdjustmentProbability();
        this.maxTimeFlexibility = evCfg.getMaxTimeFlexibility();
    }

    @Override
    public void finishReplanning() {
    }

    @Override
    public void handlePlan(Plan plan) {

        // retrieve number of plan changes to apply
        int numberOfChanges = 1 + random.nextInt(maxNumberSimultaneousPlanChanges);
        
        // retrieve relevant person characteristics
        Person person = plan.getPerson();
        Attributes personAttributes = person.getAttributes();
        double homeChargerPower = personAttributes.getAttribute("homeChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("homeChargerPower").toString()) : 0.0;
        double workChargerPower = personAttributes.getAttribute("workChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("workChargerPower").toString()) : 0.0;
        String subpopulation = personAttributes.getAttribute("subpopulation").toString();
        String opportunityCharging_str = personAttributes.getAttribute("opportunityCharging").toString();
        Boolean opportunityCharging = opportunityCharging_str.equals("true") ? true : false; 

        // derived person characteristics
        Boolean personHasHomeCharger = homeChargerPower>0.0 ? true : false;
        Boolean personHasWorkCharger = workChargerPower>0.0 ? true : false;
        Boolean personHasPrivateCharger = personHasHomeCharger || personHasWorkCharger;
        Boolean personCriticalSOC = subpopulation.equals("criticalSOC");

        // retrieve person charging history
        List<PlanElement> planElements = plan.getPlanElements();
        int planSize = planElements.size();

        // Apply plan changes
        for (int c = 0; c < numberOfChanges; c++) {

            // Todo: probably switch to sets, prevent total reevaluation
            ArrayList<Integer> successfulChargingActIds = new ArrayList<>();
            ArrayList<Integer> failedChargingActIds = new ArrayList<>();
            ArrayList<Integer> noChargingActIds = new ArrayList<>();
            ArrayList<Integer> workActIds = new ArrayList<>();
            ArrayList<Integer> homeActIds = new ArrayList<>();
            ArrayList<Integer> otherActIds = new ArrayList<>();

            for (int i = 1; i < planSize; i++) {
                PlanElement pe = planElements.get(i);
                
                // identify the charging status of each plan element
                if (pe instanceof Activity) {
                    Activity act = (Activity) pe;
                    String actType = act.getType();
                    if (actType.endsWith(CHARGING_IDENTIFIER)) {
                        successfulChargingActIds.add(i);
                    } else if (actType.endsWith(CHARGING_FAILED_IDENTIFIER)) {
                        // remove and possibly change activity or time
                        failedChargingActIds.add(i);
                        act.setType(actType.replace(CHARGING_FAILED_IDENTIFIER, CHARGING_IDENTIFIER));
                    } else {
                        noChargingActIds.add(i);
                    }
                
                    // identify home, work, and other plan elements
                    
                    if(actType.contains("home")){
                        homeActIds.add(i);
                    }
                    else if(actType.contains("work")){
                        workActIds.add(i);
                    }
                    else{
                        otherActIds.add(i);
                    }
                }
            }

            // derived activity characteristics
            ArrayList<Integer> allChargingActIds = new ArrayList<Integer>(Stream.of(successfulChargingActIds, failedChargingActIds).flatMap(x -> x.stream()).collect(Collectors.toList()));

            ArrayList<Integer> homeActsWithCharging = new ArrayList<Integer>(allChargingActIds.stream().filter(element -> homeActIds.contains(element)).collect(Collectors.toList()));
            ArrayList<Integer> workActsWithCharging = new ArrayList<Integer>(allChargingActIds.stream().filter(element -> workActIds.contains(element)).collect(Collectors.toList()));
            ArrayList<Integer> otherActsWithCharging = new ArrayList<Integer>(allChargingActIds.stream().filter(element -> otherActIds.contains(element)).collect(Collectors.toList()));

            ArrayList<Integer> homeActsWithoutCharging = new ArrayList<Integer>(noChargingActIds.stream().filter(element -> homeActIds.contains(element)).collect(Collectors.toList()));
            ArrayList<Integer> workActsWithoutCharging = new ArrayList<Integer>(noChargingActIds.stream().filter(element -> workActIds.contains(element)).collect(Collectors.toList()));
            ArrayList<Integer> otherActsWithoutCharging = new ArrayList<Integer>(noChargingActIds.stream().filter(element -> otherActIds.contains(element)).collect(Collectors.toList()));

            // charging options
            Boolean remainingHomeChargingOpportunities = homeActsWithoutCharging.size() > 0 ? true : false;
            Boolean remainingWorkChargingOpportunities = workActsWithoutCharging.size() > 0 ? true : false;
            Boolean remainingOtherChargingOpportunities = otherActsWithoutCharging.size() > 0 ? true : false;

            if(personCriticalSOC){

                // If the person has a critical soc, add a charging activity
                if(personHasHomeCharger & remainingHomeChargingOpportunities){
                    // critical soc and home charger
                    addChargingActivity(planElements, homeActsWithoutCharging);
                }
                else if(personHasWorkCharger & remainingWorkChargingOpportunities){
                    // critical soc and work, but no home charger 
                    addChargingActivity(planElements, workActsWithoutCharging);
                }
                else if(remainingHomeChargingOpportunities){
                    // if the person has to charge publicly, charging close to home is still preferred
                    addChargingActivity(planElements, homeActsWithoutCharging);
                }
                else if(remainingWorkChargingOpportunities){
                    // if the person has to charge publicly but can not charge close to home, charging close to work is preferred
                    addChargingActivity(planElements, workActsWithoutCharging);
                }
                else if(remainingOtherChargingOpportunities){
                    // critical soc, but person can not charge close to home or work
                    addChargingActivity(planElements, otherActsWithoutCharging); // Add charging activity to any activity without charging
                }
                else if(failedChargingActIds.size()>0){
                    // critical but already used up all charging opportunities
                    changeChargingActivityTime(planElements, failedChargingActIds);
                }
                else{
                    ; // Todo: Handle these hopeless cases
                }
            }
            else{
                // non-critical soc: add, change, or remove charging activities
                // constraint: always make sure that people who are flagged as taking part in opportunity charging will continue to do so

                int randomAction = 0;
                
                // Handling of failed charging activities: This is an extra action that does not count for the max number of plan changes
                if (failedChargingActIds.size()>0 && random.nextDouble()<timeAdjustmentProbability)
                {
                    // With some probability try changing start time of failed charging activity (end time of previous activity)
                    // Charging activities that continue to fail will be removed by replanning eventually (no benefit to score)
                    changeChargingActivityTime(planElements, failedChargingActIds);
                }

                if(personHasPrivateCharger)
                {
                    // Person has a private charger at home or work
                    if(personHasHomeCharger&&personHasWorkCharger){
                        // Person has both, home and work charger
                        // Options: 
                        // 0: remove work, add home charging (charge at home instead of at work)
                        // 1: remove home, add work charging (charge at work instead of at home)
                        // 2: remove work, add work charging (charge some other day at work)
                        // 3: remove home, add home charging (charge some other day at home)
                        // 4: add home charging
                        // 5: add work charging
                        // 6: add other charging
                        // 7: remove home charging
                        // 8: remove work charging
                        // 9: remove other charging (if possible)
                        // 10: remove other, add other charging (charge at some other non-home/non-work activity)
                        
                        randomAction = random.nextInt(11);

                        switch(randomAction) {
                            case 0:
                                if(workActsWithCharging.size()>0&&remainingHomeChargingOpportunities){
                                    // Remove work, add home charging
                                    changeChargingActivity(planElements, workActsWithCharging, homeActsWithoutCharging);
                                }
                                break;
                            case 1:
                                if(homeActsWithCharging.size()>0&&remainingWorkChargingOpportunities){
                                    // Remove home, add work charging
                                    changeChargingActivity(planElements, homeActsWithCharging, workActsWithoutCharging);
                                }
                                break;
                            case 2:
                                if(workActsWithCharging.size()>0&&remainingWorkChargingOpportunities){
                                    // Remove work, add work
                                    changeChargingActivity(planElements, workActsWithCharging, workActsWithoutCharging);
                                }
                                break;
                            case 3:
                                if(homeActsWithCharging.size()>0&&remainingHomeChargingOpportunities){
                                    // Remove home, add home
                                    changeChargingActivity(planElements, homeActsWithCharging, homeActsWithoutCharging);
                                }
                                break;
                            case 4: 
                                if(remainingHomeChargingOpportunities)
                                {
                                    // Add home
                                    addChargingActivity(planElements, homeActsWithoutCharging);
                                }
                                break;
                            case 5: 
                                if(remainingWorkChargingOpportunities)
                                {
                                    // Add work
                                    addChargingActivity(planElements, workActsWithoutCharging);
                                }
                                break;
                            case 6: 
                                if(remainingOtherChargingOpportunities)
                                {
                                    // Add other
                                    addChargingActivity(planElements, otherActsWithoutCharging);
                                }
                                break;
                            case 7: 
                                if(homeActsWithCharging.size()>0)
                                {
                                    // Remove home
                                    removeChargingActivity(planElements, homeActsWithCharging);
                                }
                                break;
                            case 8: 
                                if(workActsWithCharging.size()>0)
                                {
                                    // Remove work
                                    removeChargingActivity(planElements, workActsWithCharging);
                                }
                                break;
                            case 9: 
                                if((!opportunityCharging&&otherActsWithCharging.size()>0)||(opportunityCharging&&otherActsWithCharging.size()>1))
                                {
                                    // Remove other charging activity only if
                                    // A: the person is not flagged for opportunity charging and has other charging activities
                                    // B: the person is flagged for opportunity charging and has more than one other charging activity left
                                    removeChargingActivity(planElements, otherActsWithCharging);
                                }
                                break;
                            case 10: 
                                if(otherActsWithCharging.size()>0&&remainingOtherChargingOpportunities)
                                {
                                    // Remove other, add other
                                    changeChargingActivity(planElements, otherActsWithCharging, otherActsWithoutCharging);
                                }
                                break;
                        }


                    }
                    else if(personHasHomeCharger&&!personHasWorkCharger){
                        // Person has a home charger, but not a work charger
                        // Options: 
                        // 0: remove home, add home charging (charge some other day at home)
                        // 1: add home charging
                        // 2: add work charging
                        // 3: add other charging
                        // 4: remove home charging
                        // 5: remove work charging
                        // 6: remove other charging (if possible)
                        // 7: remove other, add other charging (charge at some other non-home/non-work activity)

                        randomAction = random.nextInt(8);
                        
                        switch(randomAction) {
                           
                            case 0:
                                if(homeActsWithCharging.size()>0&&remainingHomeChargingOpportunities){
                                    // Remove home, add home
                                    changeChargingActivity(planElements, homeActsWithCharging, homeActsWithoutCharging);
                                }
                                break;
                            case 1: 
                                if(remainingHomeChargingOpportunities)
                                {
                                    // Add home
                                    addChargingActivity(planElements, homeActsWithoutCharging);
                                }
                                break;
                            case 2: 
                                if(remainingWorkChargingOpportunities)
                                {
                                    // Add work
                                    addChargingActivity(planElements, workActsWithoutCharging);
                                }
                                break;
                            case 3: 
                                if(remainingOtherChargingOpportunities)
                                {
                                    // Add other
                                    addChargingActivity(planElements, otherActsWithoutCharging);
                                }
                                break;
                            case 4: 
                                if(homeActsWithCharging.size()>0)
                                {
                                    // Remove home
                                    removeChargingActivity(planElements, homeActsWithCharging);
                                }
                                break;
                            case 5: 
                                if(workActsWithCharging.size()>0)
                                {
                                    // Remove work
                                    removeChargingActivity(planElements, workActsWithCharging);
                                }
                                break;
                            case 6: 
                                if((!opportunityCharging&&otherActsWithCharging.size()>0)||(opportunityCharging&&otherActsWithCharging.size()>1))
                                {
                                    // Remove other charging activity only if
                                    // A: the person is not flagged for opportunity charging and has other charging activities
                                    // B: the person is flagged for opportunity charging and has more than one other charging activity left
                                    removeChargingActivity(planElements, otherActsWithCharging);
                                }
                                break;
                            case 7: 
                                if(otherActsWithCharging.size()>0&&remainingOtherChargingOpportunities)
                                {
                                    // Remove other, add other
                                    changeChargingActivity(planElements, otherActsWithCharging, otherActsWithoutCharging);
                                }
                                break;
                        }

                    }
                    else{
                        // Person has a work charger, but not a home charger
                        // Options: 
                        // 0: remove work, add work charging (charge some other day at work)
                        // 1: add home charging
                        // 2: add work charging
                        // 3: add other charging
                        // 4: remove home charging
                        // 5: remove work charging
                        // 6: remove other charging (if possible)
                        // 7: remove other, add other charging (charge at some other non-home/non-work activity)
                        
                        randomAction = random.nextInt(8);

                        switch(randomAction) {
                            
                            case 0:
                                if(workActsWithCharging.size()>0&&remainingWorkChargingOpportunities){
                                    // Remove work, add work
                                    changeChargingActivity(planElements, workActsWithCharging, workActsWithoutCharging);
                                }
                                break;
                            case 1: 
                                if(remainingHomeChargingOpportunities)
                                {
                                    // Add home
                                    addChargingActivity(planElements, homeActsWithoutCharging);
                                }
                                break;
                            case 2: 
                                if(remainingWorkChargingOpportunities)
                                {
                                    // Add work
                                    addChargingActivity(planElements, workActsWithoutCharging);
                                }
                                break;
                            case 3: 
                                if(remainingOtherChargingOpportunities)
                                {
                                    // Add other
                                    addChargingActivity(planElements, otherActsWithoutCharging);
                                }
                                break;
                            case 4: 
                                if(homeActsWithCharging.size()>0)
                                {
                                    // Remove home
                                    removeChargingActivity(planElements, homeActsWithCharging);
                                }
                                break;
                            case 5: 
                                if(workActsWithCharging.size()>0)
                                {
                                    // Remove work
                                    removeChargingActivity(planElements, workActsWithCharging);
                                }
                                break;
                            case 6: 
                                if((!opportunityCharging&&otherActsWithCharging.size()>0)||(opportunityCharging&&otherActsWithCharging.size()>1))
                                {
                                    // Remove other charging activity only if
                                    // A: the person is not flagged for opportunity charging and has other charging activities
                                    // B: the person is flagged for opportunity charging and has more than one other charging activity left
                                    removeChargingActivity(planElements, otherActsWithCharging);
                                }
                                break;
                            case 7: 
                                if(otherActsWithCharging.size()>0&&remainingOtherChargingOpportunities)
                                {
                                    // Remove other, add other
                                    changeChargingActivity(planElements, otherActsWithCharging, otherActsWithoutCharging);
                                }
                                break;
                        }


                    }
                }
                else
                {
                    // Person has no private charger and is entirely reliant on public chargers
                    // -> Randomly change, remove, or add a charging activity with equal probability
                    randomAction = random.nextInt(3); 

                    switch(randomAction) {
                        case 0:
                            if(noChargingActIds.size()>0&allChargingActIds.size()>0){
                                changeChargingActivity(planElements, allChargingActIds, noChargingActIds);
                            }
                            break;
                        case 1:
                            if(successfulChargingActIds.size()>0){
                                removeChargingActivity(planElements, allChargingActIds);
                            }
                            break;
                        case 2:
                            if(noChargingActIds.size()>0){
                                addChargingActivity(planElements, noChargingActIds);
                            }
                            break;
                    }
                }

            }
            
        }

    }

    private void changeChargingActivityTime(List<PlanElement> planElements, ArrayList<Integer> failedChargingActIds) {
        // select random failed charging activity and try changing end time of previous activity
        int n = failedChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = failedChargingActIds.get(randInt);
            if (actId >= 2) {
                Activity selectedActivity = (Activity) planElements.get(actId);
                Leg previousLeg = (Leg) planElements.get(actId - 1);
                Activity previousActivity = (Activity) planElements.get(actId - 2);
                double timeDifference = random.nextDouble() * maxTimeFlexibility; // 0 to 10 minutes
                double earliestPossibleTime = 0;
                if (actId >= 4) {
                    earliestPossibleTime = ((Activity) planElements.get(actId - 4)).getEndTime().seconds();
                }
                if (previousActivity.getEndTime().seconds() - timeDifference > earliestPossibleTime) {
                    previousActivity.setEndTime(previousActivity.getEndTime().seconds() - timeDifference);
                    previousLeg.setDepartureTime(previousLeg.getDepartureTime().seconds() - timeDifference);
                    selectedActivity.setType(selectedActivity.getType() + CHARGING_IDENTIFIER);
                }
            }
        }
    }

    private void addChargingActivity(List<PlanElement> planElements, ArrayList<Integer> noChargingActIds) {
        // select random activity without charging and change to activity with charging
        int n = noChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = noChargingActIds.get(randInt);
            Activity selectedActivity = (Activity) planElements.get(actId);
            selectedActivity.setType(selectedActivity.getType() + CHARGING_IDENTIFIER);
        }
    }

    private void removeChargingActivity(List<PlanElement> planElements, ArrayList<Integer> successfulChargingActIds) {
        // select random activity with charging and change to activity without charging
        int n = successfulChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = successfulChargingActIds.get(randInt);
            Activity selectedActivity = (Activity) planElements.get(actId);
            selectedActivity.setType(selectedActivity.getType().replace(CHARGING_IDENTIFIER, ""));
        }
    }

    private void changeChargingActivity(List<PlanElement> planElements,
                                ArrayList<Integer> chargingActIds,
                                ArrayList<Integer> noChargingActIds) {
        // Change by subsequently removing and adding charging activities
        removeChargingActivity(planElements, chargingActIds);
        addChargingActivity(planElements, noChargingActIds);
    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    @Override
    public void handleEvent(ChargingBehaviourScoringEvent event) {
        //double startSoc = event.getStartSoc();
        double soc = event.getSoc();
        //boolean isLastAct = event.getActivityType().contains("end");
        Person person = this.population.getPersons().get(event.getPersonId());
        double personRangeAnxiety = person.getAttributes().getAttribute("rangeAnxietyThreshold")!=null ? Double.parseDouble(person.getAttributes().getAttribute("rangeAnxietyThreshold").toString()) : this.evCfg.getDefaultRangeAnxietyThreshold();
        // Make sure agents with a criticalSOC or with a bad end-soc get replanned for sure
        if (soc<=personRangeAnxiety) {
            // Add all critical agents to the criticalSOC subpopulation such that they get replanned
            population.getPersons().get(event.getPersonId()).getAttributes().putAttribute("subpopulation", "criticalSOC");
        }
        else{
            // Remove all non-critical agents from the criticalSOC subpopulation such that they get replanned with the default probability
            population.getPersons().get(event.getPersonId()).getAttributes().putAttribute("subpopulation", "nonCriticalSOC");
        }
    }

    @Override
    public void reset(int iteration) {
    }
}
