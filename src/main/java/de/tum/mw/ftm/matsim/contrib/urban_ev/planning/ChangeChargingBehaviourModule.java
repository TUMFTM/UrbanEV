package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ChangeChargingBehaviourModule implements PlanStrategyModule {

    private static final String CRITICAL_SOC_IDENTIFIER = "criticalSOC";
    private static final String NON_CRITICAL_SOC_IDENTIFIER = "nonCriticalSOC";

    private Random random = new Random();

    ChangeChargingBehaviourModule(Scenario scenario) {
    }

    @Override
    public void finishReplanning() {
    }

    @Override
    public void handlePlan(Plan plan) {
        
        // retrieve relevant person characteristics
        Person person = plan.getPerson();
        Attributes personAttributes = person.getAttributes();

        String subpopulation = personAttributes.getAttribute("subpopulation").toString();

        // derived person characteristics
        boolean personCriticalSOC = subpopulation.equals(CRITICAL_SOC_IDENTIFIER);      

        // person plan analysis
        List<Activity> activities = PlanUtils.getActivities(plan);
        List<Activity> nonStartOrEndActs = activities.stream().filter(a -> !PlanUtils.isIniAct(a) && !PlanUtils.isEndAct(a)).collect(Collectors.toList());

        // Make sure to skip the very first activity. The charging state of this activity always equals the charging state of the last activity
        // This makes sure that overnight charging activites are propagated between iterations
        Activity firstActivity = nonStartOrEndActs.get(0);
        nonStartOrEndActs = nonStartOrEndActs.stream().skip(1).collect(Collectors.toList());
        Activity lastActivity = nonStartOrEndActs.get(nonStartOrEndActs.size()-1);

        // first, analyze current charging behavior
        List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);
        List<Activity> noChargingActs = PlanUtils.getNonChargingActivities(nonStartOrEndActs);

        List<Activity> failedChargingActs = allChargingActs.stream().filter(a -> PlanUtils.failed(a)).collect(Collectors.toList());

        // Remove failed charging activities
        failedChargingActs.forEach((act) -> PlanUtils.removeFailed(act));

        if(personCriticalSOC){
            
            // Persons with critical socs get a random charging activity
            addRandomChargingActivity(noChargingActs);

        }
        else{
                // Persons who do not have a critical soc perform a random action
                // -> Randomly change, remove, or add a charging activity with equal probability

                switch(random.nextInt(3)) {
                    case 0:
                        if(!noChargingActs.isEmpty()&&!allChargingActs.isEmpty()){
                            changeRandomChargingActivity(allChargingActs, noChargingActs);
                        }
                        break;
                    case 1:
                        if(!allChargingActs.isEmpty()){
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

        // Assume that the person belongs to the non-critical group after replanning
        setNonCriticalSubpopulation(person);

        // Make sure that the states of the first and the last activity match
        propagateOvernightChargingBetweenIterations(firstActivity, lastActivity);

        // Make sure the newly created plan is selected
        person.setSelectedPlan(plan);
    }

    private void addChargingActivity(Activity activity)
    {
        PlanUtils.setCharging(activity);
    }

    private void removeChargingActivity(Activity activity)
    {
        PlanUtils.removeCharging(activity);
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

    private void propagateOvernightChargingBetweenIterations(Activity firstActivity, Activity lastActivity)
    {
        if(PlanUtils.isCharging(lastActivity) && !PlanUtils.isCharging(firstActivity))
        {
            addChargingActivity(firstActivity);
        }
        else if(!PlanUtils.isCharging(lastActivity) && PlanUtils.isCharging(firstActivity))
        {
            removeChargingActivity(firstActivity);
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
