package de.tum.mw.ftm.matsim.contrib.urban_ev.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;

public class PlanUtils {

	public static final String ACTTYPE_INI = "";
	public static final String ACTTYPE_CHARGING = " charging";
	public static final String ACTTYPE_END = " end";
	public static final String ACTTYPE_FAILED = " failed";
    
    public static List<Activity> getActivities(Plan plan){

        return plan
        .getPlanElements()
        .stream()
        .filter(f -> f instanceof Activity)
        .map(pe -> (Activity) pe)
        .collect(Collectors.toList());
        
    }

	public static List<Activity> getActivitiesAtCoord(List<Activity> activities, Coord coord)
	{
		return activities
		.stream()
		.filter(f -> f.getCoord().equals(coord))
		.collect(Collectors.toList());
	}

	public static List<Activity> getActivitiesAtCoord(Plan plan, Coord coord)
	{
		return getActivities(plan)
		.stream()
		.filter(f -> f.getCoord().equals(coord))
		.collect(Collectors.toList());
	}

	public static List<Activity> sortByEndTime(List<Activity> activities) {
        
        // Use a Comparator to sort activities by end time
        Comparator<Activity> endTimeComparator = new Comparator<Activity>() {
            public int compare(Activity activity1, Activity activity2) {
                return Double.compare(activity1.getEndTime().seconds(), activity2.getEndTime().seconds());
            }
        };
        
        // Sort the list of activities by end time
        Collections.sort(activities, endTimeComparator);
        
        return activities;
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
        return getActivityTypeContains(activities, ACTTYPE_CHARGING);
    }

	public static List<Activity> getNonChargingActivities(List<Activity> activities){
        return getActivityTypeNotContains(activities, ACTTYPE_CHARGING);
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

	public static boolean isCharging(String actType)
	{
		return actType.contains(ACTTYPE_CHARGING);
	}

	public static boolean isCharging(Activity activity)
	{
		return isCharging(activity.getType());
	}

	public static void setCharging(Activity activity)
	{
		activity.setType(activity.getType() + ACTTYPE_CHARGING);
	}

	public static void unsetCharging(Activity activity)
	{
		activity.setType(activity.getType().replace(ACTTYPE_CHARGING, ""));
	}

	public static boolean isIniAct(String actType)
	{
		return actType.equals(ACTTYPE_INI);
	}

	public static boolean isIniAct(Activity activity)
	{
		return isIniAct(activity.getType());
	}

	public static boolean isEndAct(String actType)
	{
		return actType.contains(ACTTYPE_END);
	}

	public static boolean isEndAct(Activity activity)
	{
		return isEndAct(activity.getType());
	}

	public static boolean isHome(String actType)
	{
		return actType.contains("home");
	}

	public static boolean isHome(Activity activity)
	{
		return isHome(activity.getType());
	}

	public static boolean isWork(String actType)
	{
		return actType.contains("work") && !actType.contains("_related");
	}

	public static boolean isWork(Activity activity)
	{
		return isWork(activity.getType());
	}

	public static boolean isFailed(String actType)
	{
		return actType.contains(ACTTYPE_FAILED);
	}

	public static boolean isFailed(Activity activity)
	{
		return isFailed(activity.getType());
	}

	public static void setFailed(Activity activity)
	{
		if(!isFailed(activity))
		{
			activity.setType(activity.getType() + ACTTYPE_FAILED);
		}
	}

	public static void unsetFailed(Activity activity)
	{
		activity.setType(activity.getType().replace(ACTTYPE_FAILED, ""));
	}

}
