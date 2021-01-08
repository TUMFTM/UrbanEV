/*
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
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Stack;

public class ElectricVehicleTypesReader extends MatsimXmlParser {
	private static final String TYPE = "type";

	private final Map<Id<ElectricVehicleType>, ElectricVehicleType> types;

	public ElectricVehicleTypesReader(Map<Id<ElectricVehicleType>, ElectricVehicleType> types) {
		this.types = types;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (TYPE.equals(name)) {
			ElectricVehicleType type = ElectricVehicleTypeImpl.newBuilder()
					.id(Id.create(atts.getValue("name"), ElectricVehicleType.class))
					.name(atts.getValue("name"))
					.consumption(Double.parseDouble(atts.getValue("consumption")))
					.maxChargingRate(Double.parseDouble(atts.getValue("max_charging_rate")))
					.mass(Double.parseDouble(atts.getValue("mass")))
					.width(Double.parseDouble(atts.getValue("width")))
					.height(Double.parseDouble(atts.getValue("height")))
					.length(Double.parseDouble(atts.getValue("length")))
					.aerodynamicDragCoefficient(Double.parseDouble(atts.getValue("cw")))
					.rollingDragCoefficient(Double.parseDouble(atts.getValue("ft")))
					.inertiaResistanceCoefficient(Double.parseDouble(atts.getValue("cb")))
					.driveTrainEfficiency(Double.parseDouble(atts.getValue("spr")))
					.build();
			types.put(type.getId(), type);
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}

}
