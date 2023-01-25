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

    public static final String CRITICAL_SOC_THRESHOLD = "criticalSOCThreshold";
    static final String CRITICAL_SOC_THRESHOLD_EXP = "Threshold under which an SOC is considered to be critical. [0.0-1.0]";

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

    public static final String OPTIMAL_SOC = "optimalSOC";
    static final String OPTIMAL_SOC_EXP = "Optimal soc: agents will be punished if they have greater or smaller socs (see doc for details). [0.0-1.0]";

    public static final String BATTERY_HEALTH_STRESS_UTILITY = "batteryHealthStressUtility";
    static final String BATTERY_HEALTH_STRESS_UTILITY_EXP = "The utility of exceeding the optimal soc to the max (scoring is calculated linearly depending on the offset from the optimal soc)";

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

    public static final String REFERENCE_PARKING_DISTANCE = "referenceParkingDistance";
    static final String REFERENCE_PARKING_DISTANCE_EXP = "Distance assumed to be the basline walking distance between a public parking space and the activity location without charging [m].";

    public static final String TRANSFERFINALSOCTONEXTITERATION = "transferFinalSoCToNextIteration";
    static final String TRANSFERFINALSOCTONEXTITERATION_EXP = "determines whether the resulting SoC at the end of the iteration X is set to be the initial SoC in iteration X+1 for each EV. If set to true, bear in mind that EV might start with 0% battery charge.";

    // Replanning

    public static final String MAXNUMBERSIMULTANEOUSPLANCHANGES = "maxNumberSimultaneousPlanChanges";
    static final String MAXNUMBERSIMULTANEOUSPLANCHANGES_EXP = "The maximum number of changes to a persons charging plan that are introduced in one replanning step.";

    // Initialization
    
    public static final String INITIALIZATION_ITERATIONS = "initializationIterations";
    static final String INITIALIZATION_ITERATIONS_EXP = "[integer] Number of initialization iterations to setup socs and get the simulation starting. After the specified number of iterations concludes, the initialization outputs are used to start the actual simiulation (or trigger another initialization run based on the value of 'initializationRepetitions').";

    public static final String INITIALIZATION_REPETITIONS = "initializationRepetitions";
    static final String INITIALIZATION_REPETITIONS_EXP = "[integer] Number of repetitions of the initialization routine.";

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
    private double criticalSOCThreshold = 0.2;

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

    @Positive
    private double optimalSOC = 0.8;

    @NotNull
    private double batteryHealthStressUtility = -0.1;

    // Charging parameters
    @Positive
    private double parkingSearchRadius = 500;

    @Positive
    private double referenceParkingDistance = 200;

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

    // General
    @NotNull
    private boolean transferFinalSoCToNextIteration = true;

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
        map.put(CRITICAL_SOC_THRESHOLD, CRITICAL_SOC_THRESHOLD_EXP);
        map.put(MAXNUMBERSIMULTANEOUSPLANCHANGES, MAXNUMBERSIMULTANEOUSPLANCHANGES_EXP);
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
        map.put(TRANSFERFINALSOCTONEXTITERATION, TRANSFERFINALSOCTONEXTITERATION_EXP);
        map.put(OPTIMAL_SOC, OPTIMAL_SOC_EXP);
        map.put(BATTERY_HEALTH_STRESS_UTILITY, BATTERY_HEALTH_STRESS_UTILITY_EXP);
        map.put(REFERENCE_PARKING_DISTANCE, REFERENCE_PARKING_DISTANCE_EXP);
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

    @StringGetter(CRITICAL_SOC_THRESHOLD)
    public double getCriticalSOCThreshold() {
        return criticalSOCThreshold;
    }

    @StringSetter(CRITICAL_SOC_THRESHOLD)
    public void setCriticalSOCThreshold(double criticalSOCThreshold) {
        this.criticalSOCThreshold = criticalSOCThreshold;
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
    public double getParkingSearchRadius() {
        return parkingSearchRadius;
    }

    @StringSetter(PARKING_SEARCH_RADIUS)
    public void setParkingSearchRadius(double parkingSearchRadius) {
        this.parkingSearchRadius = parkingSearchRadius;
    }
    
    @StringGetter(REFERENCE_PARKING_DISTANCE)
    public double getReferenceParkingDistance() {
        return referenceParkingDistance;
    }

    @StringSetter(REFERENCE_PARKING_DISTANCE)
    public void setReferenceParkingDistance(double referenceParkingDistance) {
        this.referenceParkingDistance = referenceParkingDistance;
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

    @StringSetter(TRANSFERFINALSOCTONEXTITERATION)
    public void setTransferFinalSoCToNextIteration(boolean transferFinalSoCToNextIteration) {
        this.transferFinalSoCToNextIteration = transferFinalSoCToNextIteration;
    }

    @StringGetter(TRANSFERFINALSOCTONEXTITERATION)
    public boolean isTransferFinalSoCToNextIteration() {
        return transferFinalSoCToNextIteration;
    }

    @StringGetter(BATTERY_HEALTH_STRESS_UTILITY)
    public double getBatteryHealthStressUtility() {
        return batteryHealthStressUtility;
    }

    @StringSetter(BATTERY_HEALTH_STRESS_UTILITY)
    public void setBatteryHealthStressUtility(double batteryHealthStressUtility) {
        this.batteryHealthStressUtility = batteryHealthStressUtility;
    }

    @StringGetter(OPTIMAL_SOC)
    public double getOptimalSOC() {
        return optimalSOC;
    }

    @StringSetter(OPTIMAL_SOC)
    public void setOptimalSOC(double optimalSOC) {
        this.optimalSOC = optimalSOC;
    }

}
