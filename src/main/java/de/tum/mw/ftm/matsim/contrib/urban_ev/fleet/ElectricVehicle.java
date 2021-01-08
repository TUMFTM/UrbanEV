/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE

 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package de.tum.mw.ftm.matsim.contrib.urban_ev.fleet;

import com.google.common.collect.ImmutableList;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingPower;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.AuxEnergyConsumption;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.DriveEnergyConsumption;
import org.matsim.api.core.v01.Identifiable;

public interface ElectricVehicle extends Identifiable<ElectricVehicle> {
	DriveEnergyConsumption getDriveEnergyConsumption();

	AuxEnergyConsumption getAuxEnergyConsumption();

	ChargingPower getChargingPower();

	Battery getBattery();

	ElectricVehicleType getVehicleType();

	ImmutableList<String> getChargerTypes();
}
