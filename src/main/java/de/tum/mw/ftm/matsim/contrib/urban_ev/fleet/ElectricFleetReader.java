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
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargerSpecification;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ElectricFleetReader extends MatsimXmlParser {
	private static final String VEHICLE = "vehicle";

	private final ElectricFleetSpecification fleet;
	private final Map<Id<ElectricVehicleType>, ElectricVehicleType> types;

	public ElectricFleetReader(ElectricFleetSpecification fleet, Map<Id<ElectricVehicleType>, ElectricVehicleType> types) {
		this.fleet = fleet;
		this.types = types;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (VEHICLE.equals(name)) {
			fleet.addVehicleSpecification(createSpecification(atts));
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}

	private ElectricVehicleSpecification createSpecification(Attributes atts) {
		return ImmutableElectricVehicleSpecification.newBuilder()
				.id(Id.create(atts.getValue("id"), ElectricVehicle.class))
				.batteryCapacity(EvUnits.kWh_to_J(Double.parseDouble(atts.getValue("battery_capacity"))))
				.initialSoc(EvUnits.kWh_to_J(Double.parseDouble(atts.getValue("initial_soc"))))
				.vehicleType(types.get(Id.create(atts.getValue("vehicle_type"), ElectricVehicleType.class)))
				.chargerTypes(ImmutableList.copyOf(Optional.ofNullable(atts.getValue("charger_types"))
						.orElse(ChargerSpecification.DEFAULT_CHARGER_TYPE)
						.split(",")))
				.build();
	}
}
