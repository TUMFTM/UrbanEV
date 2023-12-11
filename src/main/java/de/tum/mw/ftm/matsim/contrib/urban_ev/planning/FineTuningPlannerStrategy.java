package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PersonUtils;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;

import java.util.List;

public class FineTuningPlannerStrategy implements PlanStrategyModule {

    private UrbanEVConfigGroup evCfg;
      

    FineTuningPlannerStrategy(Scenario scenario) {
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
    }

    @Override
    public void finishReplanning() {
    }

    @Override
    public void handlePlan(Plan plan) {

        // retrieve relevant person characteristics
        Person person = plan.getPerson();

        // derived person characteristics
        boolean personHasHomeCharger = PersonUtils.hasHomeCharger(person);

        // person plan analysis
        List<Activity> activities = PlanUtils.getActivities(plan);
        List<Activity> nonStartOrEndActs = PlanUtils.getNonIniEndActivities(activities);

        // Apply plan changes

        // first, analyze current charging behavior
        List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);

        List<Activity> failedChargingActs = PlanUtils.getActivityTypeContains(allChargingActs, "failed");

        // Remove failed charging activities
        failedChargingActs.forEach((act) -> {PlanUtils.unsetFailed(act);});

        // Only handle plans of persons who do not have a private home charger
        // Owners of home chargers charge stochastically depending on the initialization
        if(!personHasHomeCharger && !allChargingActs.isEmpty())
        {
            
            // All persons, who cannot charge privately at home can adjust the position of charging activities
            Activity randomChargingActivity = PlanUtils.getRandomActivity(allChargingActs);

            // Try to move charging activity forward or backward
            switch(PlanUtils.getRandomInt(2)) {
                    case 0:
                        // Move charging Activitiy forward in time 
                        Activity nextActivity = PlanUtils.getNextActivity(randomChargingActivity, activities);
                        
                        // Move charging activity, but do not move it beyond another charging activity and make sure not to move it to an ini or end act
                        if(!PlanUtils.isCharging(nextActivity) && randomChargingActivity!=nextActivity && nonStartOrEndActs.contains(nextActivity))
                        {
                            PlanUtils.switchChargingActivities(randomChargingActivity, nextActivity);
                        }

                        break;
                    case 1:
                        // Move charging Activitiy backward in time    
                        Activity previousActivity = PlanUtils.getPreviousActivity(randomChargingActivity, activities);
                        
                        // Move charging activity, but do not move it beyond another charging activity and make sure not to move it to an ini or end act
                        if(!PlanUtils.isCharging(previousActivity) && randomChargingActivity!=previousActivity  && nonStartOrEndActs.contains(previousActivity))
                        {
                            PlanUtils.switchChargingActivities(randomChargingActivity, previousActivity);
                        }

                        break;
                    }

        }


        // Select the replanned plan for next iteration
        person.setSelectedPlan(plan);

    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

}
