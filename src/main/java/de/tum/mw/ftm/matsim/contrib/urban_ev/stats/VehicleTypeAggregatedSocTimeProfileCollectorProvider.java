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
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector.ProfileCalculator;
import org.matsim.contrib.util.timeprofile.TimeProfiles;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VehicleTypeAggregatedSocTimeProfileCollectorProvider implements Provider<MobsimListener> {
	private final ElectricFleet evFleet;
	private final MatsimServices matsimServices;

	@Inject
	public VehicleTypeAggregatedSocTimeProfileCollectorProvider(ElectricFleet evFleet, MatsimServices matsimServices) {
		this.evFleet = evFleet;
		this.matsimServices = matsimServices;
	}

	@Override
	public MobsimListener get() {
		ProfileCalculator calc = createIndividualSocCalculator(evFleet);
		return new TimeProfileCollector(calc, 300, "average_soc_time_profiles", matsimServices);
	}

	public static ProfileCalculator createIndividualSocCalculator(final ElectricFleet evFleet) {

		Set<String> vehicleTypes = evFleet.getElectricVehicles()
				.values()
				.stream()
				.map(electricVehicle -> electricVehicle.getVehicleType().getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		vehicleTypes.add("Fleet Average");
		String[] header = vehicleTypes.stream().toArray(String[]::new);
		return TimeProfiles.createProfileCalculator(header, () -> {
			Double[] result = new Double[header.length];
			for (int i = 0; i < header.length - 1; i++) {
				String type = header[i];
				result[i] = evFleet.getElectricVehicles()
						.values()
						.stream()
						.filter(electricVehicle -> electricVehicle.getVehicleType().getName().equals(type))
						.mapToDouble(ev -> EvUnits.J_to_kWh(ev.getBattery().getSoc()))
						.average()
						.orElse(Double.NaN);
			}
			result[header.length - 1] = evFleet.getElectricVehicles()
					.values()
					.stream()
					.mapToDouble(ev -> EvUnits.J_to_kWh(ev.getBattery().getSoc()))
					.average()
					.getAsDouble();
			return result;
		});
	}

}
