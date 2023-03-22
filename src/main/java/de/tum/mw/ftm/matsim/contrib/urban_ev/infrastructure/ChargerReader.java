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
import org.matsim.contrib.ev.EvUnits;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class ChargerReader extends MatsimXmlParser {
	private final static String CHARGER = "charger";

	private final ChargingInfrastructureSpecification chargingInfrastructure;

	public ChargerReader(ChargingInfrastructureSpecification chargingInfrastructure) {
		this.chargingInfrastructure = chargingInfrastructure;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (CHARGER.equals(name)) {
			chargingInfrastructure.addChargerSpecification(createSpecification(atts));
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}

	private ChargerSpecification createSpecification(Attributes atts) {
		return ImmutableChargerSpecification.newBuilder()
				.id(Id.create(atts.getValue("id"), Charger.class))
				.coord(new Coord(
						Double.parseDouble(atts.getValue("x")),
						Double.parseDouble(atts.getValue("y"))))
				.chargerType(
						Optional.ofNullable(atts.getValue("type")).orElse(ChargerSpecification.DEFAULT_CHARGER_TYPE))
				.plugPower(EvUnits.kW_to_W(Double.parseDouble(atts.getValue("plug_power"))))
				.plugCount(Optional.ofNullable(atts.getValue("plug_count"))
						.map(Integer::parseInt)
						.orElse(ChargerSpecification.DEFAULT_PLUG_COUNT))
				.allowedVehicles(getAllowedEvIds(atts))
				.build();
	}

	private List<Id<ElectricVehicle>> getAllowedEvIds(Attributes attributes) {
		List<Id<ElectricVehicle>> allowedEvIds = new ArrayList<>();
		if (attributes.getValue("allowed_vehicles") != null) {
			String[] evIds = attributes.getValue("allowed_vehicles").replace("[","").replace("]","").split(",");
			for (String evId : evIds) {
				if(!evId.isEmpty()){
					allowedEvIds.add(Id.create(evId, ElectricVehicle.class));
				}
			}
		}
		return allowedEvIds;
	}
}
