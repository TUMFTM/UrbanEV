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
    public final double batteryHealthStressUtility;
    public final int referenceParkingDistance;

    private ChargingBehaviourScoringParameters(
            final double marginalUtilityOfRangeAnxiety_soc,
            final double utilityOfEmptyBattery,
            final double marginalUtilityOfWalking_m,
            final double utilityOfHomeCharging,
            final double marginalUtilityOfSocDifference,
            final double failedOpportunityChargingUtility,
            final double marginalUtilityOfStationHogging,
            final double optimalSOC,
            final double batteryHealthStressUtility,
            final int referenceParkingDistance) {
        this.marginalUtilityOfRangeAnxiety_soc = marginalUtilityOfRangeAnxiety_soc;
        this.utilityOfEmptyBattery = utilityOfEmptyBattery;
        this.marginalUtilityOfWalking_m = marginalUtilityOfWalking_m;
        this.utilityOfHomeCharging = utilityOfHomeCharging;
        this.marginalUtilityOfSocDifference = marginalUtilityOfSocDifference;
        this.failedOpportunityChargingUtility = failedOpportunityChargingUtility;
        this.marginalUtilityOfStationHogging = marginalUtilityOfStationHogging;
        this.batteryHealthStressUtility = batteryHealthStressUtility;
        this.optimalSOC = optimalSOC;
        this.referenceParkingDistance = referenceParkingDistance;
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
        private double batteryHealthStressUtility;
        private int referenceParkingDistance;

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
            batteryHealthStressUtility = configGroup.getBatteryHealthStressUtility();
            referenceParkingDistance = configGroup.getReferenceParkingDistance();
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
                    batteryHealthStressUtility,
                    referenceParkingDistance
            );
        }
    }
}
