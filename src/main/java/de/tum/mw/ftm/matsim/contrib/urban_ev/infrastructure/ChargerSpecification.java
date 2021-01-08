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

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;

import java.util.List;

/**
 * ChargerSpecification is assumed to be immutable.
 * <p>
 * Its lifespan can span over all iterations, but can be also changed before each iteration.
 * <p>
 * Changing a charger specification (e.g. setting a different plug count) should be done only "between" iterations
 * by passing a new instance to ChargingInfrastructureSpecification.
 *
 * @author Michal Maciejewski (michalm)
 */

public interface ChargerSpecification extends Identifiable<Charger> {
	String DEFAULT_CHARGER_TYPE = "default";
	int DEFAULT_PLUG_COUNT = 1;

	/**
	 * @return coordinates
	 */
	Coord getCoord();

	/**
	 * @return charger type
	 */
	String getChargerType();

	/**
	 * @return max power at a single plug, in [W]
	 */
	double getPlugPower();

	/**
	 * @return number of plugs
	 */
	int getPlugCount();

	/**
	 * @return List of ids vehicles that are allowed to use charger
	 */
	List<Id<ElectricVehicle>> getAllowedVehicles();
}
