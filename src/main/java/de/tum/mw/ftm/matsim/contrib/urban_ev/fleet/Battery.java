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

public interface Battery {
	/**
	 * @return Battery Capacity [J]
	 */
	double getCapacity();

	/**
	 * @return Vehicle State of Charge [J]
	 */
	double getSoc();

	/**
	 * @param soc Vehicle State of Charge [J]
	 */
	void setSoc(double soc);

	/**
	 * Changes SOC, making sure the charge level does not increase above the battery capacity or decrease below 0.
	 *
	 * @param energy change in energy [J], can be negative or positive
	 */
	default void changeSoc(double energy) {
		setSoc(Math.max(0, Math.min(getSoc() + energy, getCapacity())));
	}

	double getStartSoc(); // in kWh
}
