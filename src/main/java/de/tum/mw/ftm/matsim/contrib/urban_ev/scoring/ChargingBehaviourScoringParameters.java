package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.internal.MatsimParameters;

public class ChargingBehaviourScoringParameters implements MatsimParameters {

    public final double marginalUtilityOfRangeAnxiety_soc;
    public final double utilityOfEmptyBattery;
    public final double marginalUtilityOfWalking_m;
    public final double utilityOfHomeCharging;
    public final double marginalUtilityOfSocDifference;
    public final double failedOpportunityChargingUtility;
    public final double marginalUtilityOfStationHogging;
    public final double optimalSOC;
    public final double criticalSOCThreshold;
    public final double batteryHealthStressUtility;
    public final double parkingSearchRadius;
    public final double referenceParkingDistance;
    public final double utilityOfDCCharging;

    private ChargingBehaviourScoringParameters(
            final double marginalUtilityOfRangeAnxiety_soc,
            final double utilityOfEmptyBattery,
            final double marginalUtilityOfWalking_m,
            final double utilityOfHomeCharging,
            final double marginalUtilityOfSocDifference,
            final double failedOpportunityChargingUtility,
            final double marginalUtilityOfStationHogging,
            final double optimalSOC,
            final double criticalSOCThreshold,
            final double batteryHealthStressUtility,
            final double parkingSearchRadius,
            final double referenceParkingDistance,
            final double utilityOfDCCharging) {
        this.marginalUtilityOfRangeAnxiety_soc = marginalUtilityOfRangeAnxiety_soc;
        this.utilityOfEmptyBattery = utilityOfEmptyBattery;
        this.marginalUtilityOfWalking_m = marginalUtilityOfWalking_m;
        this.utilityOfHomeCharging = utilityOfHomeCharging;
        this.marginalUtilityOfSocDifference = marginalUtilityOfSocDifference;
        this.failedOpportunityChargingUtility = failedOpportunityChargingUtility;
        this.marginalUtilityOfStationHogging = marginalUtilityOfStationHogging;
        this.batteryHealthStressUtility = batteryHealthStressUtility;
        this.optimalSOC = optimalSOC;
        this.criticalSOCThreshold = criticalSOCThreshold;
        this.parkingSearchRadius = parkingSearchRadius;
        this.referenceParkingDistance = referenceParkingDistance;
        this.utilityOfDCCharging = utilityOfDCCharging;
    }

    public static final class Builder {
        private double marginalUtilityOfRangeAnxiety_soc;
        private double utilityOfEmptyBattery;
        private double marginalUtilityOfWalking_m;
        private double utilityOfHomeCharging;
        private double marginalUtilityOfSocDifference;
        private double failedOpportunityChargingUtility;
        private double marginalUtilityOfStationHogging;
        private double optimalSOC;
        private double criticalSOCThreshold;
        private double batteryHealthStressUtility;
        private double parkingSearchRadius;
        private double referenceParkingDistance;
        private double utilityOfDCCharging;

        public Builder(final Scenario scenario) {
            this((UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev"));
        }

        public Builder(final UrbanEVConfigGroup configGroup) {
            marginalUtilityOfRangeAnxiety_soc = configGroup.getRangeAnxietyUtility();
            utilityOfEmptyBattery = configGroup.getEmptyBatteryUtility();
            marginalUtilityOfWalking_m = configGroup.getWalkingUtility();
            utilityOfHomeCharging = configGroup.getHomeChargingUtility();
            marginalUtilityOfSocDifference = configGroup.getSocDifferenceUtility();
            failedOpportunityChargingUtility = configGroup.getFailedOpportunityChargingUtility();
            marginalUtilityOfStationHogging = configGroup.getStationHoggingUtility();
            optimalSOC = configGroup.getOptimalSOC();
            criticalSOCThreshold = configGroup.getCriticalSOCThreshold();
            batteryHealthStressUtility = configGroup.getBatteryHealthStressUtility();
            parkingSearchRadius = configGroup.getParkingSearchRadius();
            referenceParkingDistance = configGroup.getReferenceParkingDistance();
            utilityOfDCCharging = configGroup.getdcChargingUtility();
        }

        public ChargingBehaviourScoringParameters build() {
            return new ChargingBehaviourScoringParameters(
                    marginalUtilityOfRangeAnxiety_soc,
                    utilityOfEmptyBattery,
                    marginalUtilityOfWalking_m,
                    utilityOfHomeCharging,
                    marginalUtilityOfSocDifference,
                    failedOpportunityChargingUtility,
                    marginalUtilityOfStationHogging,
                    optimalSOC,
                    criticalSOCThreshold,
                    batteryHealthStressUtility,
                    parkingSearchRadius,
                    referenceParkingDistance,
                    utilityOfDCCharging
            );
        }
    }
}
