package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ChangeChargingBehaviourModule implements PlanStrategyModule {

    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String CRITICAL_SOC_IDENTIFIER = "criticalSOC";
    private static final String NON_CRITICAL_SOC_IDENTIFIER = "nonCriticalSOC";

    private Random random = new Random();
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

        double homeChargerPower = personAttributes.getAttribute("homeChargerPower") != null ? ((Double) personAttributes.getAttribute("homeChargerPower")).doubleValue() : 0.0;
		double workChargerPower = personAttributes.getAttribute("workChargerPower") != null ? ((Double) personAttributes.getAttribute("workChargerPower")).doubleValue() : 0.0;
        String subpopulation = personAttributes.getAttribute("subpopulation").toString();

        // derived person characteristics
        boolean personHasHomeCharger = homeChargerPower>0.0 ? true : false;
        boolean personHasWorkCharger = workChargerPower>0.0 ? true : false;
        boolean personHasPrivateCharger = personHasHomeCharger || personHasWorkCharger;
        boolean personCriticalSOC = subpopulation.equals(CRITICAL_SOC_IDENTIFIER);      

        // person plan analysis
        List<Activity> activities = PlanUtils.getActivities(plan);
        List<Activity> nonStartOrEndActs = PlanUtils.getActivityTypeNotEquals(PlanUtils.getActivityTypeNotContains(activities, "end"), "");


        List<Activity> homeActs = PlanUtils.getActivityTypeContains(nonStartOrEndActs, "home");
        List<Activity> workActs = PlanUtils.getActivityTypeNotContains(PlanUtils.getActivityTypeContains(nonStartOrEndActs, "work"), "work_related");
        List<Activity> otherActs = nonStartOrEndActs.stream().filter(a -> !homeActs.contains(a) & !workActs.contains(a)).collect(Collectors.toList());

        // Apply plan changes
        for (int c = 0; c < numberOfChanges; c++) {

            // first, analyze current charging behavior
            List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);
            List<Activity> noChargingActs = PlanUtils.getNonChargingActivities(nonStartOrEndActs);

            List<Activity> failedChargingActs = PlanUtils.getActivityTypeContains(allChargingActs, "failed");
            List<Activity> successfulChargingActs = PlanUtils.getActivityTypeNotContains(allChargingActs, "failed");

            List<Activity> homeActsWithCharging = PlanUtils.getChargingActivities(homeActs);
            List<Activity> workActsWithCharging = PlanUtils.getChargingActivities(workActs);
            List<Activity> otherActsWithCharging = PlanUtils.getChargingActivities(otherActs);

            List<Activity> homeActsWithoutCharging = PlanUtils.getNonChargingActivities(homeActs);
            List<Activity> workActsWithoutCharging = PlanUtils.getNonChargingActivities(workActs);
            List<Activity> otherActsWithoutCharging = PlanUtils.getNonChargingActivities(otherActs);

            // Remove failed charging activities
            failedChargingActs.forEach((act) -> {act.setType(act.getType().replace(" failed", ""));});

            if(personCriticalSOC){

                // If the person has a critical soc, add a charging activity
                if(personHasHomeCharger & !homeActsWithoutCharging.isEmpty()){
                    // critical soc and home charger
                    addRandomChargingActivity(homeActsWithoutCharging);
                }
                else if(personHasWorkCharger & !workActsWithoutCharging.isEmpty()){
                    // critical soc and work, but no home charger 
                    addRandomChargingActivity(workActsWithoutCharging);
                }
                else if(!homeActsWithoutCharging.isEmpty()){
                    // if the person has to charge publicly, charging close to home is still preferred
                    addRandomChargingActivity(homeActsWithoutCharging);
                }
                else if(!workActsWithoutCharging.isEmpty()){
                    // if the person has to charge publicly but can not charge close to home, charging close to work is preferred
                    addRandomChargingActivity(workActsWithoutCharging);
                }
                else if(!otherActsWithoutCharging.isEmpty()){
                    // critical soc, but person can not charge close to home or work
                    addRandomChargingActivity(otherActsWithoutCharging); // Add charging activity to any activity without charging
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
                    if(!otherActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEOTHER);

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

                    if(!viableChanges.isEmpty()) // If there are any viable actions...
                    {
                        ChargingStrategyChange randomAction = viableChanges.get(random.nextInt(viableChanges.size())); // ...select a random action ...

                        switch(randomAction) { // ...and execute it. If there are no viable actions the person should still be fine, because they are "uncritical" -> do not change anything
                            case REMOVEWORK_ADDHOME:
                                changeRandomChargingActivity(workActsWithCharging, homeActsWithoutCharging);
                                break;
                            case REMOVEHOME_ADDWORK:
                                // Remove home, add work charging
                                changeRandomChargingActivity(homeActsWithCharging, workActsWithoutCharging);
                                break;
                            case REMOVEWORK_ADDWORK:
                                // Remove work, add work
                                changeRandomChargingActivity(workActsWithCharging, workActsWithoutCharging);
                                break;
                            case REMOVEHOME_ADDHOME:
                                // Remove home, add home
                                changeRandomChargingActivity(homeActsWithCharging, homeActsWithoutCharging);
                                break;
                            case ADDHOME: 
                                // Add home
                                addRandomChargingActivity(homeActsWithoutCharging);
                                break;
                            case ADDWORK: 
                                // Add work
                                addRandomChargingActivity(workActsWithoutCharging);
                                break;
                            case ADDOTHER: 
                                // Add other
                                addRandomChargingActivity(otherActsWithoutCharging);
                                break;
                            case REMOVEHOME:
                                // Remove home
                                removeRandomChargingActivity(homeActsWithCharging);
                                break;
                            case REMOVEWORK: 
                                // Remove work
                                removeRandomChargingActivity(workActsWithCharging);
                                break;
                            case REMOVEOTHER: 
                                // Remove other, add other
                                removeRandomChargingActivity(otherActsWithCharging);
                                break;
                            case REMOVEOTHER_ADDOTHER: 
                                // Remove other, add other
                                changeRandomChargingActivity(otherActsWithCharging, otherActsWithoutCharging);
                                break;
                        }
                    }
                    

                }
                else
                {
                    // Person has no private charger and is entirely reliant on public chargers
                    // -> Randomly change, remove, or add a charging activity with equal probability

                    switch(random.nextInt(3)) {
                        case 0:
                            if(!noChargingActs.isEmpty()&&!allChargingActs.isEmpty()){
                                changeRandomChargingActivity(allChargingActs, noChargingActs);
                            }
                            break;
                        case 1:
                            if(!successfulChargingActs.isEmpty()){
                                removeRandomChargingActivity(allChargingActs);
                            }
                            break;
                        case 2:
                            if(!noChargingActs.isEmpty()){
                                addRandomChargingActivity(noChargingActs);
                            }
                            break;
                    }
                }

            }
            
        }

        // Assume that the person belongs to the non-critical group after replanning
        setNonCriticalSubpopulation(person);
    }

    private void addChargingActivity(Activity activity)
    {
        activity.setType(activity.getType() + CHARGING_IDENTIFIER);
    }

    private void removeChargingActivity(Activity activity)
    {
        activity.setType(activity.getType().replace(CHARGING_IDENTIFIER, ""));
    }

    private void addRandomChargingActivity(List<Activity> noChargingActs) {
        // select random activity without charging and change to activity with charging
        if (!noChargingActs.isEmpty()) {
            Activity selectedActivity = getRandomActivity(noChargingActs);
            addChargingActivity(selectedActivity);
        }
    }

    private void removeRandomChargingActivity(List<Activity> successfulChargingActs) {
        // select random activity with charging and change to activity without charging
        if (!successfulChargingActs.isEmpty()) {
            Activity selectedActivity = getRandomActivity(successfulChargingActs);
            removeChargingActivity(selectedActivity);
        }
    }

    private void changeRandomChargingActivity(
                                List<Activity> chargingActs,
                                List<Activity> noChargingActs) {
        // Change by subsequently removing and adding charging activities
        if (!chargingActs.isEmpty() && !noChargingActs.isEmpty()) {
            removeRandomChargingActivity(chargingActs);
            addRandomChargingActivity(noChargingActs);
        }
    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    private Activity getRandomActivity(List<Activity> activities)
    {
        return activities.get(random.nextInt(activities.size()));
    }

    private void setNonCriticalSubpopulation(Person person){
        person.getAttributes().putAttribute("subpopulation", NON_CRITICAL_SOC_IDENTIFIER);
    }

}
