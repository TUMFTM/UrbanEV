package de.tum.mw.ftm.matsim.contrib.urban_ev.config;


import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;

public final class UrbanEVConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUP_NAME = "urban_ev";

    private static final String RANGE_ANXIETY_UTILITY = "rangeAnxietyUtility";
    static final String RANGE_ANXIETY_UTILITY_EXP = "[utils/percent_points_of_soc_under_threshold] utility for going below battery threshold. negative";

    private static final String EMPTY_BATTERY_UTILITY = "emptyBatteryUtility";
    static final String EMPTY_BATTERY_UTILITY_EXP = "[utils] utility for empty battery. should not happen. very negative";

    private static final String WALKING_UTILITY = "walkingUtility";
    static final String WALKING_UTILITY_EXP = "[utils/m] utility for walking from charger to activity. negative";

    private static final String HOME_CHARGING_UTILITY = "homeChargingUtility";
    static final String HOME_CHARGING_UTILITY_EXP = "[utils] utility for using private home charger. positive";

    private static final String SOC_DIFFERENCE_UTILITY = "socDifferenceUtility";
    static final String SOC_DIFFERENCE_UTILITY_EXP = "[utils] utility for difference between start and end soc";

    public static final String VEHICLE_TYPES_FILE = "vehicleTypesFile";
    static final String VEHICLE_TYPES_FILE_EXP = "Location of the vehicle types file";

    public static final String DEFAULT_RANGE_ANXIETY_THRESHOLD = "defaultRangeAnxietyThreshold";
    static final String DEFAULT_RANGE_ANXIETY_THRESHOLD_EXP = "Default threshold for scoring. Set person attribute to overwrite. [% soc]";

    public static final String PARKING_SEARCH_RADIUS = "parkingSearchRadius";
    static final String PARKING_SEARCH_RADIUS_EXP = "Radius around activity location in which agents looks for available chargers [m]";

    public static final String MAXNUMBERSIMULTANEOUSPLANCHANGES = "maxNumberSimultaneousPlanChanges";
    static final String MAXNUMBERSIMULTANEOUSPLANCHANGES_EXP = "The maximum number of changes to a persons charging plan that are introduced in one replanning step.";

    public static final String TIMEADJUSTMENTPROBABILITY = "timeAdjustmentProbability";
    static final String TIMEADJUSTMENTPROBABILITY_EXP = "The probability with which a persons decides to adjust their activity end times in order to increase their chances for a free charging spot at their next activity.";

    public static final String MAXTIMEFLEXIBILITY = "maxTimeFlexibility";
    static final String MAXTIMEFLEXIBILITY_EXP = "The maximum time span a person is willing to adjust their activity end times in order to increase their chances for a free charging spot at their next activity [s].";

    public static final String GENERATE_HOME_CHARGERS_BY_PERCENTAGE = "generateHomeChargersByPercentage";
    static final String GENERATE_HOME_CHARGERS_BY_PERCENTAGE_EXP = "If set to true, home charger information from the population file will be ignored. Instead home chargers will be generated randomly given the homeChargerPercentage share. [true/false]";

    public static final String GENERATE_WORK_CHARGERS_BY_PERCENTAGE = "generateWorkChargersByPercentage";
    static final String GENERATE_WORK_CHARGERS_BY_PERCENTAGE_EXP = "If set to true, work charger information from the population file will be ignored. Instead work chargers will be generated randomly given the workChargerPercentage share. [true/false]";

    public static final String HOME_CHARGER_PERCENTAGE = "homeChargerPercentage";
    static final String HOME_CHARGER_PERCENTAGE_EXP = "Share of the population that will be equipped with a home charger if generateHomeChargersByPercentage is set to true. [%]";

    public static final String WORK_CHARGER_PERCENTAGE = "workChargerPercentage";
    static final String WORK_CHARGER_PERCENTAGE_EXP = "Share of the population that will be equipped with a work charger if generateWorkChargersByPercentage is set to true. [%]";

    public static final String DEFAULT_HOME_CHARGER_POWER = "defaultHomeChargerPower";
    static final String DEFAULT_HOME_CHARGER_POWER_EXP = "The power of home chargers if generateHomeChargersByPercentage is set to true [kW].";

    public static final String DEFAULT_WORK_CHARGER_POWER = "defaultWorkChargerPower";
    static final String DEFAULT_WORK_CHARGER_POWER_EXP = "The power of work chargers if generateWorkChargersByPercentage is set to true [kW].";


    // Charger parameters
    private boolean generateHomeChargersByPercentage = false;

    private boolean generateWorkChargersByPercentage = false;

    @PositiveOrZero
    private double homeChargerPercentage = 0.0;

    @PositiveOrZero
    private double workChargerPercentage = 0.0;

    @PositiveOrZero
    private double defaultHomeChargerPower = 11.0;

    @PositiveOrZero
    private double defaultWorkChargerPower = 11.0;


    // Scoring parameters
    @NotNull
    private double rangeAnxietyUtility = -5;

    @NotNull
    private double emptyBatteryUtility = -10;

    @NotNull
    private double walkingUtility = -1;

    @NotNull
    private double homeChargingUtility = +1;

    @NotNull
    private double socDifferenceUtility = -10;

    @Positive
    private double defaultRangeAnxietyThreshold = 0.2;

    @NotNull
    private String vehicleTypesFile = null;

    // Charging parameters
    @Positive
    private int parkingSearchRadius = 500;

    // Replanning parameters

    @Positive
    private int maxNumberSimultaneousPlanChanges = 2;

    @PositiveOrZero
    private Double timeAdjustmentProbability = 0.1;

    @PositiveOrZero
    private int maxTimeFlexibility = 600;


    public UrbanEVConfigGroup() {
        super(GROUP_NAME);
    }

    @Override
    public Map<String, String> getComments() {
        Map<String, String> map = super.getComments();
        map.put(RANGE_ANXIETY_UTILITY, RANGE_ANXIETY_UTILITY_EXP);
        map.put(EMPTY_BATTERY_UTILITY, EMPTY_BATTERY_UTILITY_EXP);
        map.put(WALKING_UTILITY, WALKING_UTILITY_EXP);
        map.put(HOME_CHARGING_UTILITY, HOME_CHARGING_UTILITY_EXP);
        map.put(SOC_DIFFERENCE_UTILITY, SOC_DIFFERENCE_UTILITY_EXP);
        map.put(VEHICLE_TYPES_FILE, VEHICLE_TYPES_FILE_EXP);
        map.put(PARKING_SEARCH_RADIUS, PARKING_SEARCH_RADIUS_EXP);
        map.put(DEFAULT_RANGE_ANXIETY_THRESHOLD, DEFAULT_RANGE_ANXIETY_THRESHOLD_EXP);
        map.put(MAXNUMBERSIMULTANEOUSPLANCHANGES, MAXNUMBERSIMULTANEOUSPLANCHANGES_EXP);
        map.put(TIMEADJUSTMENTPROBABILITY, TIMEADJUSTMENTPROBABILITY_EXP);
        map.put(MAXTIMEFLEXIBILITY, MAXTIMEFLEXIBILITY_EXP);
        map.put(GENERATE_HOME_CHARGERS_BY_PERCENTAGE, GENERATE_HOME_CHARGERS_BY_PERCENTAGE_EXP);
        map.put(GENERATE_WORK_CHARGERS_BY_PERCENTAGE, GENERATE_WORK_CHARGERS_BY_PERCENTAGE_EXP);
        map.put(HOME_CHARGER_PERCENTAGE, HOME_CHARGER_PERCENTAGE_EXP);
        map.put(WORK_CHARGER_PERCENTAGE, WORK_CHARGER_PERCENTAGE_EXP);
        map.put(DEFAULT_HOME_CHARGER_POWER, DEFAULT_HOME_CHARGER_POWER_EXP);
        map.put(DEFAULT_WORK_CHARGER_POWER, DEFAULT_WORK_CHARGER_POWER_EXP);
        return map;
    }

    @StringGetter(MAXNUMBERSIMULTANEOUSPLANCHANGES)
    public int getMaxNumberSimultaneousPlanChanges() {
        return maxNumberSimultaneousPlanChanges;
    }

    @StringSetter(MAXNUMBERSIMULTANEOUSPLANCHANGES)
    public void setMaxNumberSimultaneousPlanChanges(int maxNumberSimultaneousPlanChanges) {
        this.maxNumberSimultaneousPlanChanges = maxNumberSimultaneousPlanChanges;
    }

    @StringGetter(TIMEADJUSTMENTPROBABILITY)
    public Double getTimeAdjustmentProbability() {
        return timeAdjustmentProbability;
    }

    @StringSetter(TIMEADJUSTMENTPROBABILITY)
    public void setTimeAdjustmentProbability(Double timeAdjustmentProbability) {
        this.timeAdjustmentProbability = timeAdjustmentProbability;
    }

    @StringGetter(MAXTIMEFLEXIBILITY)
    public int getMaxTimeFlexibility() {
        return maxTimeFlexibility;
    }

    @StringSetter(MAXTIMEFLEXIBILITY)
    public void setMaxTimeFlexibility(int maxTimeFlexibility) {
        this.maxTimeFlexibility = maxTimeFlexibility;
    }

    @StringGetter(RANGE_ANXIETY_UTILITY)
    public double getRangeAnxietyUtility() { return rangeAnxietyUtility; }

    @StringSetter(RANGE_ANXIETY_UTILITY)
    public void setRangeAnxietyUtility(double rangeAnxietyUtility) { this.rangeAnxietyUtility = rangeAnxietyUtility; }

    @StringGetter(EMPTY_BATTERY_UTILITY)
    public double getEmptyBatteryUtility() { return emptyBatteryUtility; }

    @StringSetter(EMPTY_BATTERY_UTILITY)
    public void setEmptyBatteryUtility(double emptyBatteryUtility) { this.emptyBatteryUtility = emptyBatteryUtility; }

    @StringGetter(WALKING_UTILITY)
    public double getWalkingUtility() { return walkingUtility; }

    @StringSetter(WALKING_UTILITY)
    public void setWalkingUtility(double walkingUtility) { this.walkingUtility = walkingUtility; }

    @StringGetter(HOME_CHARGING_UTILITY)
    public double getHomeChargingUtility() { return homeChargingUtility; }

    @StringSetter(HOME_CHARGING_UTILITY)
    public void setHomeChargingUtility(double homeChargingUtility) { this.homeChargingUtility = homeChargingUtility; }

    @StringGetter(SOC_DIFFERENCE_UTILITY)
    public double getSocDifferenceUtility() { return socDifferenceUtility; }

    @StringSetter(SOC_DIFFERENCE_UTILITY)
    public void setSocDifferenceUtility(double socDifferenceUtility) { this.socDifferenceUtility = socDifferenceUtility; }

    @StringGetter(DEFAULT_RANGE_ANXIETY_THRESHOLD)
    public double getDefaultRangeAnxietyThreshold() {
        return defaultRangeAnxietyThreshold;
    }

    @StringSetter(DEFAULT_RANGE_ANXIETY_THRESHOLD)
    public void setDefaultRangeAnxietyThreshold(double defaultRangeAnxietyThreshold) {
        this.defaultRangeAnxietyThreshold = defaultRangeAnxietyThreshold;
    }

    @StringGetter(VEHICLE_TYPES_FILE)
    public String getVehicleTypesFile() {
        return vehicleTypesFile;
    }

    @StringSetter(VEHICLE_TYPES_FILE)
    public void setVehicleTypesFile(String vehicleTypesFile) {
        this.vehicleTypesFile = vehicleTypesFile;
    }

    @StringGetter(PARKING_SEARCH_RADIUS)
    public int getParkingSearchRadius() {
        return parkingSearchRadius;
    }

    @StringSetter(PARKING_SEARCH_RADIUS)
    public void setParkingSearchRadius(int parkingSearchRadius) {
        this.parkingSearchRadius = parkingSearchRadius;
    }

    @StringGetter(GENERATE_HOME_CHARGERS_BY_PERCENTAGE)
    public boolean isGenerateHomeChargersByPercentage() {
        return generateHomeChargersByPercentage;
    }

    @StringSetter(GENERATE_HOME_CHARGERS_BY_PERCENTAGE)
    public void setGenerateHomeChargersByPercentage(boolean generateHomeChargersByPercentage) {
        this.generateHomeChargersByPercentage = generateHomeChargersByPercentage;
    }

    @StringGetter(GENERATE_WORK_CHARGERS_BY_PERCENTAGE)
    public boolean isGenerateWorkChargersByPercentage() {
        return generateWorkChargersByPercentage;
    }

    @StringSetter(GENERATE_WORK_CHARGERS_BY_PERCENTAGE)
    public void setGenerateWorkChargersByPercentage(boolean generateWorkChargersByPercentage) {
        this.generateWorkChargersByPercentage = generateWorkChargersByPercentage;
    }


    @StringGetter(HOME_CHARGER_PERCENTAGE)
    public double getHomeChargerPercentage() {
        return homeChargerPercentage;
    }

    @StringSetter(HOME_CHARGER_PERCENTAGE)
    public void setHomeChargerPercentage(double homeChargerPercentage) {
        this.homeChargerPercentage = homeChargerPercentage;
    }


    @StringGetter(WORK_CHARGER_PERCENTAGE)
    public double getWorkChargerPercentage() {
        return workChargerPercentage;
    }

    @StringSetter(WORK_CHARGER_PERCENTAGE)
    public void setWorkChargerPercentage(double workChargerPercentage) {
        this.workChargerPercentage = workChargerPercentage;
    }


    @StringGetter(DEFAULT_HOME_CHARGER_POWER)
    public double getDefaultHomeChargerPower() {
        return defaultHomeChargerPower;
    }

    @StringSetter(DEFAULT_HOME_CHARGER_POWER)
    public void setDefaultHomeChargerPower(double defaultHomeChargerPower) {
        this.defaultHomeChargerPower = defaultHomeChargerPower;
    }


    @StringGetter(DEFAULT_WORK_CHARGER_POWER)
    public double getDefaultWorkChargerPower() {
        return defaultWorkChargerPower;
    }

    @StringSetter(DEFAULT_WORK_CHARGER_POWER)
    public void setDefaultWorkChargerPower(double defaultWorkChargerPower) {
        this.defaultWorkChargerPower = defaultWorkChargerPower;
    }

}
