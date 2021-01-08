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

package de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure;/*
 *
 * created by jbischoff, 23.08.2018
 */

import com.google.common.primitives.Doubles;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.LTHDriveEnergyConsumption;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.vehicles.VehicleType;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class reads Energy consumption files from CSV as used in the IDEAS project between TU Berlin and LTH Lund.
 * CSVs contain a slope (in percent) in rows and columns with speeds in m/s.
 * Values in the table are in kWh
 */
public class LTHConsumptionModelReader {

	private final Id<VehicleType> vehicleTypeId;

	public LTHConsumptionModelReader(Id<VehicleType> vehicleTypeId) {
		this.vehicleTypeId = vehicleTypeId;
	}

	public LTHDriveEnergyConsumption.Factory readURL(URL fileUrl) {
		List<Double> speeds = new ArrayList<>();
		List<Double> slopes = new ArrayList<>();
		TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
		tabularFileParserConfig.setDelimiterTags(new String[] { "," });
		tabularFileParserConfig.setUrl(fileUrl);

		new TabularFileParser().parse(tabularFileParserConfig, row -> {
			if (speeds.isEmpty()) {
				for (int i = 1; i < row.length; i++) {
					speeds.add(Double.parseDouble(row[i]));
				}
			} else {
				slopes.add(Double.parseDouble(row[0]) / 100);
			}
		});

		double[][] consumptionPerSpeedAndSlope = new double[speeds.size()][slopes.size()];

		new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
			int line = 0;

			@Override
			public void startRow(String[] row) {
				if (line > 0) {
					double lastValidValue = Double.MIN_VALUE;
					for (int i = 1; i < row.length; i++) {
						double value = Double.parseDouble(row[i]);
						if (Double.isNaN(value)) {
							value = lastValidValue;
						}
						lastValidValue = value;
						consumptionPerSpeedAndSlope[i - 1][line - 1] = value;
					}
				}
				line++;
			}
		});

		return new LTHDriveEnergyConsumption.Factory(Doubles.toArray(speeds), Doubles.toArray(slopes),
				consumptionPerSpeedAndSlope, false);
	}
}
