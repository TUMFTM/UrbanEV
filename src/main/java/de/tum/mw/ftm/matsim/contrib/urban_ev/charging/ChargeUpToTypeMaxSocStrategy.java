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

package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.Battery;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ChargeUpToTypeMaxSocStrategy implements ChargingStrategy {
	private final Charger charger;
	private final double maxRelativeSoc;

	private final Map<String,Double> maxRelativeSocByChargerType = Stream.of(new Object[][] {{ "private_ac", 0.8 },{ "public_dc", 0.8 }, { "public_ac", 0.9 }, { "default", 0.5 }}).collect(Collectors.toMap(data -> (String) data[0], data -> (double) data[1]));

	public ChargeUpToTypeMaxSocStrategy(Charger charger) {

		this.maxRelativeSoc = maxRelativeSocByChargerType.getOrDefault(charger.getChargerType(), 1.0);

		if (maxRelativeSoc < 0 || maxRelativeSoc > 1) {
			throw new IllegalArgumentException();
		}
		this.charger = charger;
	}

	@Override
	public double calcRemainingEnergyToCharge(ElectricVehicle ev) {
		Battery battery = ev.getBattery();
		return maxRelativeSoc * battery.getCapacity() - battery.getSoc();
	}

	@Override
	public double calcRemainingTimeToCharge(ElectricVehicle ev) {
		return ((BatteryCharging)ev.getChargingPower()).calcChargingTime(charger, calcRemainingEnergyToCharge(ev));
	}
}
