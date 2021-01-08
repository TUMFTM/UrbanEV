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

import com.google.common.base.Preconditions;

public class BatteryImpl implements Battery {
	private final double capacity; // J
	private double soc; // J
	private double startSoc; // J

	public BatteryImpl(double capacity, double soc) {
		this.capacity = capacity;
		this.soc = soc;
		this.startSoc = soc;
	}

	@Override
	public double getCapacity() {
		return capacity;
	}

	@Override
	public double getSoc() {
		return soc;
	}

	@Override
	public void setSoc(double soc) {
		Preconditions.checkArgument(soc >= 0 && soc <= capacity, "SoC outside allowed range: %s", soc);
		this.soc = soc;
	}
	@Override
	public double getStartSoc() {
		return startSoc;
	}

}
