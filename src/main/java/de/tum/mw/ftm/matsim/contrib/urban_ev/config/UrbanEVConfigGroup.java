package de.tum.mw.ftm.matsim.contrib.urban_ev.config;


import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Map;

public final class UrbanEVConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUP_NAME = "urban_ev";

    // Scoring

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

    public static final String DEFAULT_RANGE_ANXIETY_THRESHOLD = "defaultRangeAnxietyThreshold";
    static final String DEFAULT_RANGE_ANXIETY_THRESHOLD_EXP = "Default threshold for scoring. Set person attribute to overwrite. [% soc]";

    public static final String OPPORTUNITY_CHARGING_SHARE = "opportunityChargingShare";
    static final String OPPORTUNITY_CHARGING_SHARE_EXP = "The share of people who own a private charger at home/work but engage in opportunity charging at least once during simulation time.";

    public static final String FAILED_OPPORTUNITY_CHARGING_UTILITY = "failedOpportunityChargingUtility";
    static final String FAILED_OPPORTUNITY_CHARGING_UTILITY_EXP = "[utils] utility for missing to engage in opportunity charging if flagged for opportunity charging.";

    public static final String STATION_HOGGING_UTILITY = "stationHoggingUtility";
    static final String STATION_HOGGING_UTILITY_EXP = "[utils] utility of excessive charging, usually negative";

    public static final String HOGGING_EXEMPTION_HOUR_START = "hoggingExemptionHourStart";
    static final String HOGGING_EXEMPTION_HOUR_START_EXP = "[double - hour] the time from which on excessive plugged durations are tolerated. Example: 20.5 -> From 20:30:00 onwards, hogging is acceptable.";

    public static final String HOGGING_EXEMPTION_HOUR_STOP = "hoggingExemptionHourStop";
    static final String HOGGING_EXEMPTION_HOUR_STOP_EXP = "[double - hour] the time from which on excessive plugged durations are not tolerated anymore. Example: 8.5 -> From 08:30:00 onwards, hogging is not acceptable.";

    public static final String HOGGING_THRESHOLD_MINUTES = "hoggingThresholdMinutes";
    static final String HOGGING_THRESHOLD_MINUTES_EXP = "[min] the number of minutes after which a plugged duration is considered as punishable station hogging.";

    // DataIO

    public static final String VEHICLE_TYPES_FILE = "vehicleTypesFile";
    static final String VEHICLE_TYPES_FILE_EXP = "Location of the vehicle types file";

    public static final String DELETE_ITERATIONS_ON_THE_FLY = "deleteIterationsOnTheFly";
    static final String DELETE_ITERATIONS_ON_THE_FLY_EXP = "[boolean] If set to true, delete iterations to save disk space. Behavior can further be specified using parameters 'forceKeepNthIteration' and 'keepIterationsModulo'. The first and last iteration will be kept in any case.";

    public static final String FORCE_KEEP_NTH_ITERATION = "forceKeepNthIteration";
    static final String FORCE_KEEP_NTH_ITERATION_EXP = "[boolean] Only applies if 'deleteIterationsOnTheFly' is enabled. Then, if set to true, 'keepIterationsModulo' takes effect to retain the nth-iteration folder. 'keepIterationsModulo' defaults to 10 (e.g. iterations 0,10,20,..., N are kept).";

    public static final String KEEP_ITERATIONS_MODULO = "keepIterationsModulo";
    static final String KEEP_ITERATIONS_MODULO_EXP = "[integer] Only applies if 'deleteIterationsOnTheFly' and 'forceKeepNthIteration' are enabled. The, 'keepIterationsModulo' determines which interation folders are kept in addition to the first and last iteration (e.g. if keepIterationsModulo = 10: iterations 0,10,20,..., N are kept). Defaults to 10.";


    // General Options

    public static final String PARKING_SEARCH_RADIUS = "parkingSearchRadius";
    static final String PARKING_SEARCH_RADIUS_EXP = "Radius around activity location in which agents looks for available chargers [m]";

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

    // Replanning

    public static final String MAXNUMBERSIMULTANEOUSPLANCHANGES = "maxNumberSimultaneousPlanChanges";
    static final String MAXNUMBERSIMULTANEOUSPLANCHANGES_EXP = "The maximum number of changes to a persons charging plan that are introduced in one replanning step.";

    // Initialization
    
    public static final String INITIALIZATION_ITERATIONS = "initializationIterations";
    static final String INITIALIZATION_ITERATIONS_EXP = "[integer] Number of initialization iterations to setup socs and get the simulation starting. After the specified number of iterations concludes, the initialization outputs are used to start the actual simiulation (or trigger another initialization run based on the value of 'initializationRepetitions').";

    public static final String INITIALIZATION_REPETITIONS = "initializationRepetitions";
    static final String INITIALIZATION_REPETITIONS_EXP = "[integer] Number of repetitions of the initialization routine.";

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

    @NotNull
    private double failedOpportunityChargingUtility = -10;

    @Positive
    private double defaultRangeAnxietyThreshold = 0.2;

    @NotNull
    private String vehicleTypesFile = null;

    @NotNull
    private double stationHoggingUtility = -3;

    @Positive
    private double stationHoggingThresholdMinutes = 4*60.0;

    @Positive
    private double hoggingExemptionHourStart = 20.0;

    @Positive
    private double hoggingExemptionHourStop = 8.0;

    // Charging parameters
    @Positive
    private int parkingSearchRadius = 500;

    @Positive
    private double opportunityChargingShare = 0.02;

    // Replanning

    @Positive
    private int maxNumberSimultaneousPlanChanges = 2;

    // DataIO
    @NotNull
    private boolean deleteIterationsOnTheFly = false;

    @NotNull
    private boolean forceKeepNthIteration = false;

    @Positive
    private int keepIterationsModulo = 10;

    // Initialization
    @PositiveOrZero
    private int initializationIterations = 0;

    @PositiveOrZero
    private int initializationRepetitions = 0;

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
        map.put(GENERATE_HOME_CHARGERS_BY_PERCENTAGE, GENERATE_HOME_CHARGERS_BY_PERCENTAGE_EXP);
        map.put(GENERATE_WORK_CHARGERS_BY_PERCENTAGE, GENERATE_WORK_CHARGERS_BY_PERCENTAGE_EXP);
        map.put(HOME_CHARGER_PERCENTAGE, HOME_CHARGER_PERCENTAGE_EXP);
        map.put(WORK_CHARGER_PERCENTAGE, WORK_CHARGER_PERCENTAGE_EXP);
        map.put(DEFAULT_HOME_CHARGER_POWER, DEFAULT_HOME_CHARGER_POWER_EXP);
        map.put(DEFAULT_WORK_CHARGER_POWER, DEFAULT_WORK_CHARGER_POWER_EXP);
        map.put(OPPORTUNITY_CHARGING_SHARE, OPPORTUNITY_CHARGING_SHARE_EXP);
        map.put(FAILED_OPPORTUNITY_CHARGING_UTILITY, FAILED_OPPORTUNITY_CHARGING_UTILITY_EXP);
        map.put(DELETE_ITERATIONS_ON_THE_FLY, DELETE_ITERATIONS_ON_THE_FLY_EXP);
        map.put(FORCE_KEEP_NTH_ITERATION, FORCE_KEEP_NTH_ITERATION_EXP);
        map.put(KEEP_ITERATIONS_MODULO, KEEP_ITERATIONS_MODULO_EXP);
        map.put(INITIALIZATION_ITERATIONS, INITIALIZATION_ITERATIONS_EXP);
        map.put(INITIALIZATION_REPETITIONS, INITIALIZATION_REPETITIONS_EXP);
        map.put(STATION_HOGGING_UTILITY, STATION_HOGGING_UTILITY_EXP);
        map.put(HOGGING_THRESHOLD_MINUTES, HOGGING_THRESHOLD_MINUTES_EXP);
        map.put(HOGGING_EXEMPTION_HOUR_START, HOGGING_EXEMPTION_HOUR_START_EXP);
        map.put(HOGGING_EXEMPTION_HOUR_STOP, HOGGING_EXEMPTION_HOUR_STOP_EXP);
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

    @StringGetter(OPPORTUNITY_CHARGING_SHARE)
    public double getOpportunityChargingShare() {
        return opportunityChargingShare;
    }

    @StringSetter(OPPORTUNITY_CHARGING_SHARE)
    public void setOpportunityChargingShare(double opportunityChargingShare) {
        this.opportunityChargingShare = opportunityChargingShare;
    }

    @StringGetter(FAILED_OPPORTUNITY_CHARGING_UTILITY)
    public double getFailedOpportunityChargingUtility() {
        return failedOpportunityChargingUtility;
    }

    @StringSetter(FAILED_OPPORTUNITY_CHARGING_UTILITY)
    public void setFailedOpportunityChargingUtility(double failedOpportunityChargingUtility) {
        this.failedOpportunityChargingUtility = failedOpportunityChargingUtility;
    }

    @StringSetter(STATION_HOGGING_UTILITY)
    public void setStationHoggingUtility(double stationHoggingUtility) {
        this.stationHoggingUtility = stationHoggingUtility;
    }

    @StringGetter(STATION_HOGGING_UTILITY)
    public double getStationHoggingUtility(){ return stationHoggingUtility; }

    @StringSetter(HOGGING_THRESHOLD_MINUTES)
    public void setStationHoggingThresholdMinutes(double stationHoggingThresholdMinutes) {
        this.stationHoggingThresholdMinutes = stationHoggingThresholdMinutes;
    }

    @StringGetter(HOGGING_THRESHOLD_MINUTES)
    public double getStationHoggingThresholdMinutes(){ return stationHoggingThresholdMinutes; }

    @StringSetter(HOGGING_EXEMPTION_HOUR_START)
    public void setHoggingExemptionHourStart(double hoggingExemptionHourStart) {
        this.hoggingExemptionHourStart = hoggingExemptionHourStart;
    }

    @StringGetter(HOGGING_EXEMPTION_HOUR_START)
    public double getHoggingExemptionHourStart(){ return hoggingExemptionHourStart; }

    @StringSetter(HOGGING_EXEMPTION_HOUR_STOP)
    public void setHoggingExemptionHourStop(double hoggingExemptionHourStop) {
        this.hoggingExemptionHourStop = hoggingExemptionHourStop;
    }

    @StringGetter(HOGGING_EXEMPTION_HOUR_STOP)
    public double getHoggingExemptionHourStop(){ return hoggingExemptionHourStop; }

    @StringGetter(DELETE_ITERATIONS_ON_THE_FLY)
    public boolean isDeleteIterationsOnTheFly() {
        return deleteIterationsOnTheFly;
    }

    @StringSetter(DELETE_ITERATIONS_ON_THE_FLY)
    public void setDeleteIterationsOnTheFly(boolean deleteIterationsOnTheFly) {
        this.deleteIterationsOnTheFly = deleteIterationsOnTheFly;
    }

    @StringGetter(FORCE_KEEP_NTH_ITERATION)
    public boolean isForceKeepNthIteration() {
        return forceKeepNthIteration;
    }

    @StringSetter(FORCE_KEEP_NTH_ITERATION)
    public void setForceKeepNthIteration(boolean forceKeepNthIteration) {
        this.forceKeepNthIteration = forceKeepNthIteration;
    }

    @StringGetter(KEEP_ITERATIONS_MODULO)
    public int getKeepIterationsModulo() {
        return keepIterationsModulo;
    }

    @StringSetter(KEEP_ITERATIONS_MODULO)
    public void setkeepIterationsModulo(int keepIterationsModulo) {
        this.keepIterationsModulo = keepIterationsModulo;
    }

    @StringGetter(INITIALIZATION_ITERATIONS)
    public int getInitializationIterations() {
        return initializationIterations;
    }

    @StringSetter(INITIALIZATION_ITERATIONS)
    public void setInitializationIterations(int initializationIterations) {
        this.initializationIterations = initializationIterations;
    }

    @StringGetter(INITIALIZATION_REPETITIONS)
    public int getInitializationRepetitions() {
        return initializationRepetitions;
    }

    @StringSetter(INITIALIZATION_REPETITIONS)
    public void setInitializationRepetitions(int initializationRepetitions) {
        this.initializationRepetitions = initializationRepetitions;
    }

}
