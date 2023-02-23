package de.tum.mw.ftm.matsim.contrib.urban_ev.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;

public class PlanUtils {
    
	public static boolean parseBooleanActivityAttribute(Activity activity, String attr_name)
	{
		return ((Boolean)activity.getAttributes().getAttribute(attr_name)).booleanValue();
	}

	public static boolean isCharging(Activity activity)
	{
		return parseBooleanActivityAttribute(activity, "charging");
	}

	public static boolean isEndAct(Activity activity)
	{
		return parseBooleanActivityAttribute(activity, "end_act");
	}

	public static boolean isIniAct(Activity activity)
	{
		return parseBooleanActivityAttribute(activity, "ini_act");
	}

	public static boolean failed(Activity activity)
	{
		return parseBooleanActivityAttribute(activity, "failed");
	}

	public static void setCharging(Activity activity)
	{
		activity.getAttributes().putAttribute("charging", true);
	}

	public static void removeCharging(Activity activity)
	{
		activity.getAttributes().putAttribute("charging", false);
	}

	public static void setFailed(Activity activity)
	{
		activity.getAttributes().putAttribute("failed", true);
	}

	public static void removeFailed(Activity activity)
	{
		activity.getAttributes().putAttribute("failed", false);
	}

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
        return activities.stream().filter(a -> isCharging(a)).collect(Collectors.toList());
    }

	public static List<Activity> getNonChargingActivities(List<Activity> activities){
        return activities.stream().filter(a -> !isCharging(a)).collect(Collectors.toList());
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
