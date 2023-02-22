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
        Activity activity = null;
		List<PlanElement> planElements = plan.getPlanElements();
		for (int i = 0; i < planElements.size(); i++) {
			PlanElement planElement = planElements.get(i);
			if (planElement instanceof Activity) {
				if (((Activity) planElement).getEndTime().isDefined()) {
					double activityEndTime = ((Activity) planElement).getEndTime().seconds();
					if (activityEndTime >= time || i == planElements.size() - 1) {
						activity = ((Activity) planElement);
						break;
					}
				}
				else if (i == planElements.size() - 1) {
					// Accept a missing end time for the last activity of a plan
					activity = ((Activity) planElement);
					break;
				}
				else{
					// There is a missing end time for an activity that is not the plan's last -> This should end in null being returned
					continue;
				}
			}
		}
		if (activity != null) {
			return activity;
		}
		else return null;
    }

}
