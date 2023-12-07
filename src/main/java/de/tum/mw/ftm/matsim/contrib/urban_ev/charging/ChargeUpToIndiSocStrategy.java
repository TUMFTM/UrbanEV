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

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.Battery;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ChargeUpToIndiSocStrategy implements ChargingStrategy {
	private final Charger charger;
	private final double maxRelativeSoc;

	public ChargeUpToIndiSocStrategy(Charger charger, double maxRelativeSoc) {

		this.charger = charger;
	
		if (charger.getId().toString().contains("work") || charger.getId().toString().contains("home")){
			maxRelativeSoc = 0.8;
		}else{

			if (charger.getChargerType().contains("default")){
				maxRelativeSoc = 0.9;
			}
			else{
				maxRelativeSoc = 0.8;
			}
		}

		if (maxRelativeSoc < 0 || maxRelativeSoc > 1) {
			throw new IllegalArgumentException();
		}
		this.maxRelativeSoc = maxRelativeSoc;
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
