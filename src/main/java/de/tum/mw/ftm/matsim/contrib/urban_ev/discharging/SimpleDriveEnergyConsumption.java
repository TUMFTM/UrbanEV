package de.tum.mw.ftm.matsim.contrib.urban_ev.discharging;

import com.google.inject.Inject;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.ev.EvUnits;

/**
 * Very simple consumption model that only uses consumption per kilometre
 */
public class SimpleDriveEnergyConsumption implements DriveEnergyConsumption {

	// vehicle specific data
	private double consumption; // consumption [kWh/100km]


	@Inject
	public SimpleDriveEnergyConsumption(ElectricVehicle electricVehicle) {
		consumption = electricVehicle.getVehicleType().getConsumption();
	}

	@Override
	public double calcEnergyConsumption(Link link, double travelTime, double linkEnterTime) {
		if (travelTime == 0) {
			return 0;
		}
		return EvUnits.kWh_100km_to_J_m(consumption) * link.getLength();
	}
}
