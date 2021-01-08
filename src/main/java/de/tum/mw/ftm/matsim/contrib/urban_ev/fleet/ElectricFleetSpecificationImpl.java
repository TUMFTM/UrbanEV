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
import org.matsim.contrib.util.SpecificationContainer;

import java.util.Map;

/**
 * @author Michal Maciejewski (michalm)
 */
public final class ElectricFleetSpecificationImpl implements ElectricFleetSpecification {
	private final SpecificationContainer<ElectricVehicle, ElectricVehicleSpecification> container = new SpecificationContainer<>();

	@Override
	public Map<Id<ElectricVehicle>, ElectricVehicleSpecification> getVehicleSpecifications() {
		return container.getSpecifications();
	}

	@Override
	public void addVehicleSpecification(ElectricVehicleSpecification specification) {
		container.addSpecification(specification);
	}

	@Override
	public void replaceVehicleSpecification(ElectricVehicleSpecification specification) {
		container.replaceSpecification(specification);
	}

	@Override
	public void removeVehicleSpecification(Id<ElectricVehicle> vehicleId) {
		container.removeSpecification(vehicleId);
	}
}

