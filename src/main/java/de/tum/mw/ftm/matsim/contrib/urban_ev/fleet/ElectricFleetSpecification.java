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

import org.matsim.api.core.v01.Id;

import java.util.Map;

/**
 * A container of ElectricVehicleSpecifications. Its lifespan covers all iterations.
 * <p>
 * It can be modified between iterations by add/replace/removeVehicleSpecification().
 * <p>
 * The contained ElectricVehicleSpecifications are (meant to be) immutable, so to modify them, use replaceVehicleSpecification()
 *
 * @author Michal Maciejewski (michalm)
 */
public interface ElectricFleetSpecification {
	Map<Id<ElectricVehicle>, ElectricVehicleSpecification> getVehicleSpecifications();

	void addVehicleSpecification(ElectricVehicleSpecification specification);

	void replaceVehicleSpecification(ElectricVehicleSpecification specification);

	void removeVehicleSpecification(Id<ElectricVehicle> vehicleId);
}
