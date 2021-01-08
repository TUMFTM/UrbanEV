/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE
 *  *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;

import java.util.Collection;

public interface ChargingLogic {
	interface Factory {
		ChargingLogic create(Charger charger);
	}

	void addVehicle(ElectricVehicle ev, double now);

	void addVehicle(ElectricVehicle ev, ChargingListener chargingListener, double now);

	void removeVehicle(ElectricVehicle ev, double now);

	void chargeVehicles(double chargePeriod, double now);

	Collection<ElectricVehicle> getPluggedVehicles();

	ChargingStrategy getChargingStrategy();
}
