package de.tum.mw.ftm.matsim.contrib.urban_ev.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;

public class PlanUtils {
    
    public static List<Activity> getActivities(Plan plan){

        return plan
        .getPlanElements()
        .stream()
        .filter(f -> f instanceof Activity)
        .map(pe -> (Activity) pe)
        .collect(Collectors.toList());
        
    }

	public static List<Activity> getActivityTypeContains(List<Activity> activities, String type){
		return activities
		.stream()
		.filter(a -> a.getType().contains(type))
		.collect(Collectors.toList());
	}

	public static List<Activity> getActivityTypeNotEquals(List<Activity> activities, String type){
		return activities
		.stream()
		.filter(a -> !a.getType().equals(type))
		.collect(Collectors.toList());
	}

	public static List<Activity> getActivityTypeNotContains(List<Activity> activities, String type){
		return activities
		.stream()
		.filter(a -> !a.getType().contains(type))
		.collect(Collectors.toList());
	}

    public static List<Activity> getChargingActivities(List<Activity> activities){
        return getActivityTypeContains(activities, "charging");
    }

	public static List<Activity> getNonChargingActivities(List<Activity> activities){
        return getActivityTypeNotContains(activities, "charging");
    }

    /**
	 * gets ativity from agent's plan by looking for current time
	 * @param person
	 * @param time
	 * @return
	 */
	public static Activity getActivity(Plan plan, double time)
    {	
		List<Activity> all_acts = getActivities(plan);
        return all_acts
		.stream()
		.filter(a -> a.getEndTime().isDefined() && a.getEndTime().seconds()>=time)
		.findFirst()
		.orElse(all_acts.get(all_acts.size()-1));
	}

}
