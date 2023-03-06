package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.matsim.core.gbl.MatsimRandom;

import java.util.List;
import java.util.Random;

public class ChangeChargingBehaviourModule implements PlanStrategyModule {

    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String CRITICAL_SOC_IDENTIFIER = "criticalSOC";
    private static final String NON_CRITICAL_SOC_IDENTIFIER = "nonCriticalSOC";

    private UrbanEVConfigGroup evCfg;
      

    ChangeChargingBehaviourModule(Scenario scenario) {
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
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
        List<Activity> nonStartOrEndActs = PlanUtils.getActivityTypeNotEquals(PlanUtils.getActivityTypeNotContains(activities, "end"), "");

        // Apply plan changes

        // first, analyze current charging behavior
        List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);
        List<Activity> noChargingActs = PlanUtils.getNonChargingActivities(nonStartOrEndActs);
        List<Activity> failedChargingActs = PlanUtils.getActivityTypeContains(allChargingActs, "failed");
        // Remove failed charging activities
        failedChargingActs.forEach((act) -> {act.setType(act.getType().replace(" failed", ""));});

        if(personCriticalSOC){

            // If the person has a critical soc, add a charging activity
            addRandomChargingActivity(noChargingActs);
        }
        else{
            // If a person does not have a critical soc, randomly add, remove, or switch the charging activity
            switch(getRandomInt(2)) {
                case 0:
                    if(!allChargingActs.isEmpty()){
                        removeRandomChargingActivity(allChargingActs);
                    }
                    break;
                case 1:
                    if(!noChargingActs.isEmpty()){
                        addRandomChargingActivity(noChargingActs);
                    }
                    break;
            }
        }

        // Assume that the person belongs to the non-critical group after replanning
        setNonCriticalSubpopulation(person);

        person.setSelectedPlan(plan);

    }

    private void addChargingActivity(Activity activity)
    {
        activity.setType(activity.getType() + CHARGING_IDENTIFIER);
    }

    private void removeChargingActivity(Activity activity)
    {
        activity.setType(activity.getType().replace(CHARGING_IDENTIFIER, ""));
    }

    private void addRandomChargingActivity(List<Activity> potential_add_acts) {
        // select random activity without charging and change to activity with charging
        if (!potential_add_acts.isEmpty()) {
            addChargingActivity(getRandomActivity(potential_add_acts));
        }
    }

    private void removeRandomChargingActivity(List<Activity> potential_remove_acts) {
        // select random activity with charging and change to activity without charging
        if (!potential_remove_acts.isEmpty()) {
            removeChargingActivity(getRandomActivity(potential_remove_acts));
        }
    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    private int getRandomInt(int max)
    {
        Random random = MatsimRandom.getLocalInstance();
        random.setSeed(System.currentTimeMillis());
        return random.nextInt(max);
    }

    private Activity getRandomActivity(List<Activity> activities)
    {
        
        return activities.get(getRandomInt(activities.size()));
    }

    private void setNonCriticalSubpopulation(Person person){
        person.getAttributes().putAttribute("subpopulation", NON_CRITICAL_SOC_IDENTIFIER);
    }

}
