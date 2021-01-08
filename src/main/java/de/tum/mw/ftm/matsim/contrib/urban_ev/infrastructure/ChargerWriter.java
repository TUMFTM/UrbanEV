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

import org.matsim.contrib.ev.EvUnits;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ChargerWriter extends MatsimXmlWriter {
	private final Stream<? extends ChargerSpecification> chargerSpecifications;

	public ChargerWriter(Stream<? extends ChargerSpecification> chargerSpecifications) {
		this.chargerSpecifications = chargerSpecifications;
	}

	public void write(String file) {
		openFile(file);
		writeDoctype("chargers", "http://matsim.org/files/dtd/chargers_v1.dtd");
		writeStartTag("chargers", Collections.<Tuple<String, String>>emptyList());
		writeChargers();
		writeEndTag("chargers");
		close();
	}

	private void writeChargers() {
		chargerSpecifications.forEach(c -> {
			List<Tuple<String, String>> atts = Arrays.asList(
					Tuple.of("id", c.getId().toString()),
					Tuple.of("x", c.getCoord().getX() + ""),
					Tuple.of("y", c.getCoord().getY() + ""),
					Tuple.of("type", c.getChargerType()),
					Tuple.of("plug_power", EvUnits.W_to_kW(c.getPlugPower()) + ""),
					Tuple.of("plug_count", c.getPlugCount() + ""),
					Tuple.of("allowed_vehicles", c.getAllowedVehicles().toString() + ""));
			writeStartTag("charger", atts, true);
		});
	}
}
