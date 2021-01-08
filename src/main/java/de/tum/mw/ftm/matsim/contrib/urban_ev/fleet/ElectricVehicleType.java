package de.tum.mw.ftm.matsim.contrib.urban_ev.fleet;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;

public interface ElectricVehicleType extends Identifiable<ElectricVehicleType> {
    Id<ElectricVehicleType> getId();
    String getName();
    double getConsumption(); // [kWh/100km]
    double getMaxChargingRate(); // max C-Rate for charging [1/h]
    double getMass(); // m [kg]
    double getWidth(); // w [m]
    double getHeight(); // h [m]
    double getLength(); // l [m]
    double getAerodynamicDragCoefficient(); // cw [-]
    double getRollingDragCoefficient(); // ft [-]
    double getInertiaResistanceCoefficient(); // cb [-]
    double getDriveTrainEfficiency(); // spr [-]
}
