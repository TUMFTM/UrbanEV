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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ChangeChargingBehaviourModule implements PlanStrategyModule, ChargingBehaviourScoringEventHandler {

    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String CHARGING_FAILED_IDENTIFIER = " charging failed";

    private Random random = new Random();
    private Scenario scenario;
    private Population population;
    private UrbanEVConfigGroup evCfg;
    private int maxNumberSimultaneousPlanChanges;

    private enum ChargingStrategyChange {
        REMOVEWORK_ADDHOME,
        REMOVEHOME_ADDWORK,
        REMOVEWORK_ADDWORK,
        REMOVEHOME_ADDHOME,
        ADDHOME,
        ADDWORK,
        ADDOTHER,
        REMOVEHOME,
        REMOVEWORK,
        REMOVEOTHER,
        REMOVEOTHER_ADDOTHER
    }
      

    ChangeChargingBehaviourModule(Scenario scenario) {
        this.scenario = scenario;
        this.population = this.scenario.getPopulation();
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
        this.maxNumberSimultaneousPlanChanges = evCfg.getMaxNumberSimultaneousPlanChanges();
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

        // person plan analysis
        HashMap<Integer, Activity> acts = getActivities(plan);
        HashMap<Integer, Activity> nonStartOrEndActs = getActsFromHashMapNotEqualsType(getActsFromHashMapNotContainsType(acts, "end"),"");
        HashMap<Integer, Activity> homeActs = getActsFromHashMapContainsType(nonStartOrEndActs, "home");
        HashMap<Integer, Activity> workOrWorkRelatedActs = getActsFromHashMapContainsType(nonStartOrEndActs, "work");
        HashMap<Integer, Activity> workActs = getActsFromHashMapNotContainsType(workOrWorkRelatedActs, "work_related");
        HashMap<Integer, Activity> otherActs = new HashMap<>(nonStartOrEndActs.entrySet().stream().filter(actEntry -> !homeActs.containsKey(actEntry.getKey())&&!workActs.containsKey(actEntry.getKey())).collect(Collectors.toMap(e->e.getKey(),e->e.getValue())));

        // Apply plan changes
        for (int c = 0; c < numberOfChanges; c++) {

            // first, analyze current charging behavior
            HashMap<Integer, Activity> allChargingActs = getActsFromHashMapContainsType(nonStartOrEndActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> noChargingActs = getActsFromHashMapNotContainsType(nonStartOrEndActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> failedChargingActs = getActsFromHashMapContainsType(allChargingActs, CHARGING_FAILED_IDENTIFIER);
            HashMap<Integer, Activity> successfulChargingActs = getActsFromHashMapNotContainsType(allChargingActs, CHARGING_FAILED_IDENTIFIER);

            HashMap<Integer, Activity> homeActsWithCharging = getActsFromHashMapContainsType(homeActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> workActsWithCharging = getActsFromHashMapContainsType(workActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> otherActsWithCharging = getActsFromHashMapContainsType(otherActs, CHARGING_IDENTIFIER);

            HashMap<Integer, Activity> homeActsWithoutCharging = getActsFromHashMapNotContainsType(homeActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> workActsWithoutCharging = getActsFromHashMapNotContainsType(workActs, CHARGING_IDENTIFIER);
            HashMap<Integer, Activity> otherActsWithoutCharging = getActsFromHashMapNotContainsType(otherActs, CHARGING_IDENTIFIER);

            // Remove failed charging activities
            failedChargingActs.forEach((idx, act) -> {act.setType(act.getType().replace(" failed", ""));});

            if(personCriticalSOC){

                // If the person has a critical soc, add a charging activity
                if(personHasHomeCharger & !homeActsWithoutCharging.isEmpty()){
                    // critical soc and home charger
                    addChargingActivity(homeActsWithoutCharging);
                }
                else if(personHasWorkCharger & !workActsWithoutCharging.isEmpty()){
                    // critical soc and work, but no home charger 
                    addChargingActivity(workActsWithoutCharging);
                }
                else if(!homeActsWithoutCharging.isEmpty()){
                    // if the person has to charge publicly, charging close to home is still preferred
                    addChargingActivity(homeActsWithoutCharging);
                }
                else if(!workActsWithoutCharging.isEmpty()){
                    // if the person has to charge publicly but can not charge close to home, charging close to work is preferred
                    addChargingActivity(workActsWithoutCharging);
                }
                else if(!otherActsWithoutCharging.isEmpty()){
                    // critical soc, but person can not charge close to home or work
                    addChargingActivity(otherActsWithoutCharging); // Add charging activity to any activity without charging
                }
                else{
                    ; // Todo: Handle these hopeless cases
                }
            }
            else{

                // non-critical soc: add, change, or remove charging activities
                // constraint: always make sure that people who are flagged as taking part in opportunity charging will continue to do so
                ArrayList<ChargingStrategyChange> viableChanges = new ArrayList<ChargingStrategyChange>();

                if(personHasPrivateCharger)
                {

                    // Person has both, home and work charger
                    // All options (case dependend): 
                    // REMOVEWORK_ADDHOME: remove work, add home charging (charge at home instead of at work)
                    // REMOVEHOME_ADDWORK: remove home, add work charging (charge at work instead of at home)
                    // REMOVEWORK_ADDWORK: remove work, add work charging (charge some other day at work)
                    // REMOVEHOME_ADDHOME: remove home, add home charging (charge some other day at home)
                    // ADDHOME: add home charging
                    // ADDWORK: add work charging
                    // ADDOTHER: add other charging
                    // REMOVEHOME: remove home charging
                    // REMOVEWORK: remove work charging
                    // REMOVEOTHER: remove other charging (if possible)
                    // REMOVEOTHER_ADDOTHER: remove other, add other charging (charge at some other non-home/non-work activity)

                    // These are the generally available options for people who own a private charger
                    if(!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDHOME);
                    if(!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDWORK);
                    if(!otherActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDOTHER);

                    if(!homeActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME);
                    if(!workActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK);

                    // Remove other charging activity only if
                    // A: the person is not flagged for opportunity charging and has other charging activities
                    // B: the person is flagged for opportunity charging and has more than one other charging activity left
                    if((!opportunityCharging&&!otherActsWithCharging.isEmpty())||(opportunityCharging&&otherActsWithCharging.size()>1)) viableChanges.add(ChargingStrategyChange.REMOVEOTHER);

                    if(!otherActsWithCharging.isEmpty()&&!otherActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEOTHER_ADDOTHER);

                    // Person has a private charger at home or work
                    if(personHasHomeCharger&&personHasWorkCharger){
                        
                        // Person has both, home and work charger                 
                        if(!workActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDHOME);
                        if(!homeActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDWORK);
                        if(!workActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDWORK);
                        if(!homeActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDHOME);

                    }
                    else if(personHasHomeCharger&&!personHasWorkCharger){
                        // Person has a home charger, but not a work charger
                        if(!homeActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDHOME);
                    }
                    else{
                        // Person has a work charger, but not a home charger                       
                        if(!workActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDWORK);
                    }

                    ChargingStrategyChange randomAction = viableChanges.get(random.nextInt(viableChanges.size()));

                    switch(randomAction) {
                        case REMOVEWORK_ADDHOME:
                            changeChargingActivity(workActsWithCharging, homeActsWithoutCharging);
                            break;
                        case REMOVEHOME_ADDWORK:
                            // Remove home, add work charging
                            changeChargingActivity(homeActsWithCharging, workActsWithoutCharging);
                            break;
                        case REMOVEWORK_ADDWORK:
                            // Remove work, add work
                            changeChargingActivity(workActsWithCharging, workActsWithoutCharging);
                            break;
                        case REMOVEHOME_ADDHOME:
                            // Remove home, add home
                            changeChargingActivity(homeActsWithCharging, homeActsWithoutCharging);
                            break;
                        case ADDHOME: 
                            // Add home
                            addChargingActivity(homeActsWithoutCharging);
                            break;
                        case ADDWORK: 
                            // Add work
                            addChargingActivity(workActsWithoutCharging);
                            break;
                        case ADDOTHER: 
                            // Add other
                            addChargingActivity(otherActsWithoutCharging);
                            break;
                        case REMOVEHOME:
                            // Remove home
                            removeChargingActivity(homeActsWithCharging);
                            break;
                        case REMOVEWORK: 
                            // Remove work
                            removeChargingActivity(workActsWithCharging);
                            break;
                        case REMOVEOTHER: 
                            // Remove other, add other
                            removeChargingActivity(otherActsWithCharging);
                            break;
                        case REMOVEOTHER_ADDOTHER: 
                            // Remove other, add other
                            changeChargingActivity(otherActsWithCharging, otherActsWithoutCharging);
                            break;
                    }

                }
                else
                {
                    // Person has no private charger and is entirely reliant on public chargers
                    // -> Randomly change, remove, or add a charging activity with equal probability

                    switch(random.nextInt(3)) {
                        case 0:
                            if(!noChargingActs.isEmpty()&&!allChargingActs.isEmpty()){
                                changeChargingActivity(allChargingActs, noChargingActs);
                            }
                            break;
                        case 1:
                            if(!successfulChargingActs.isEmpty()){
                                removeChargingActivity(allChargingActs);
                            }
                            break;
                        case 2:
                            if(!noChargingActs.isEmpty()){
                                addChargingActivity(noChargingActs);
                            }
                            break;
                    }
                }

            }
            
        }

    }

    private void addChargingActivity(HashMap<Integer, Activity> noChargingActs) {
        // select random activity without charging and change to activity with charging
        if (!noChargingActs.isEmpty()) {
            Activity selectedActivity = getRandomActivity(noChargingActs);
            selectedActivity.setType(selectedActivity.getType() + CHARGING_IDENTIFIER);
        }
    }

    private void removeChargingActivity(HashMap<Integer, Activity> successfulChargingActs) {
        // select random activity with charging and change to activity without charging
        if (!successfulChargingActs.isEmpty()) {
            Activity selectedActivity = getRandomActivity(successfulChargingActs);
            selectedActivity.setType(selectedActivity.getType().replace(CHARGING_IDENTIFIER, ""));
        }
    }

    private void changeChargingActivity(
                                HashMap<Integer, Activity> chargingActs,
                                HashMap<Integer, Activity> noChargingActs) {
        // Change by subsequently removing and adding charging activities
        if (!chargingActs.isEmpty() && !noChargingActs.isEmpty()) {
            removeChargingActivity(chargingActs);
            addChargingActivity(noChargingActs);
        }
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

    private HashMap<Integer, Activity> getActivities(Plan plan){

		HashMap<Integer, Activity> activityMap = new HashMap<>();

        List<PlanElement> plan_elements = plan.getPlanElements();
        int N_plan_elements = plan_elements.size();

		for(int i=0; i<N_plan_elements; i++){

            PlanElement pe = plan_elements.get(i);

			if (pe instanceof Activity) {

				activityMap.put(i, (Activity) pe);
				
			}
		
		}
		return activityMap;
	}

    private HashMap<Integer, Activity> getActsFromHashMapContainsType(HashMap<Integer, Activity> hashmap_in, String actType)
    {
        return new HashMap<>(
            hashmap_in.entrySet().stream().filter(actEntry -> actEntry.getValue().getType().contains(actType)).collect(Collectors.toMap(e->e.getKey(),e->e.getValue())));
    }

    private HashMap<Integer, Activity> getActsFromHashMapNotContainsType(HashMap<Integer, Activity> hashmap_in, String actType)
    {
        return new HashMap<>(hashmap_in.entrySet().stream().filter(actEntry -> !actEntry.getValue().getType().contains(actType)).collect(Collectors.toMap(e->e.getKey(),e->e.getValue())));
    }

    private HashMap<Integer, Activity> getActsFromHashMapNotEqualsType(HashMap<Integer, Activity> hashmap_in, String actType)
    {
        return new HashMap<>(hashmap_in.entrySet().stream().filter(actEntry -> !actEntry.getValue().getType().equals(actType)).collect(Collectors.toMap(e->e.getKey(),e->e.getValue())));
    }

    private Integer getRandomKey(HashMap<Integer, Activity> hashmap_in)
    {
        Random random = new Random();
        Integer randomKey = (Integer) hashmap_in.keySet().toArray()[random.nextInt(hashmap_in.keySet().toArray().length)];

        return randomKey;
    }

    private Activity getRandomActivity(HashMap<Integer, Activity> hashmap_in)
    {
        return hashmap_in.get(getRandomKey(hashmap_in));
    }

}
