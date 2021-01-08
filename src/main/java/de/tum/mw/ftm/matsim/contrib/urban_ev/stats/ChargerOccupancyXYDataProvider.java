/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE
 *  *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingLogic;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargingInfrastructure;
import org.matsim.contrib.util.XYDataCollector;
import org.matsim.contrib.util.XYDataCollector.XYDataCalculator;
import org.matsim.contrib.util.XYDataCollectors;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;

public class ChargerOccupancyXYDataProvider implements Provider<MobsimListener> {
	private final ChargingInfrastructure chargingInfrastructure;
	private final MatsimServices matsimServices;

	@Inject
	public ChargerOccupancyXYDataProvider(ChargingInfrastructure chargingInfrastructure,
			MatsimServices matsimServices) {
		this.chargingInfrastructure = chargingInfrastructure;
		this.matsimServices = matsimServices;
	}

	@Override
	public MobsimListener get() {
		XYDataCalculator<Charger> calc = createChargerOccupancyCalculator(chargingInfrastructure, false);
		return new XYDataCollector<>(chargingInfrastructure.getChargers().values(), calc, 300,
				"charger_occupancy_absolute", matsimServices);
	}

	public static XYDataCalculator<Charger> createChargerOccupancyCalculator(
			final ChargingInfrastructure chargingInfrastructure, boolean relative) {
		String[] header = relative ?
				new String[] { "plugs", "plugged_rel" } :
				new String[] { "plugs", "plugged" };

		return XYDataCollectors.createCalculator(header, charger -> {
			ChargingLogic logic = charger.getLogic();
			int plugs = charger.getPlugCount();
			return new String[] { charger.getPlugCount() + "", //
					getValue(logic.getPluggedVehicles().size(), plugs, relative)
			};
		});
	}

	private static String getValue(int count, int plugs, boolean relative) {
		return relative ? ((double)count / plugs) + "" : count + "";
	}
}
