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

package de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure;

import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingLogic;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;

import java.util.List;

public interface Charger extends BasicLocation, Identifiable<Charger> {
	ChargingLogic getLogic();

	Coord getCoord();

	Link getLink();

	String getChargerType();

	/**
	 * @return max power of a single plug, in [W]
	 */
	double getPlugPower();

	/**
	 * @return number of plugs
	 */
	int getPlugCount();

	/**
	 * @return ids of allowed vehicles
	 */
	List<Id<ElectricVehicle>> getAllowedVehicles();
}
