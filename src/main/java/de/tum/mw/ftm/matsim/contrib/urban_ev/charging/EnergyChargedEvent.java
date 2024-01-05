/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2023 by the members listed in the COPYING,        *
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

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;

/**
 * @author Michal Maciejewski (michalm)
 */
public class EnergyChargedEvent extends Event {
	public static final String EVENT_TYPE = "energy_charged";
	public static final String ATTRIBUTE_VEHICLE = "ElectricVehicle";
	public static final String ATTRIBUTE_CHARGER = "charger";
	public static final String ATTRIBUTE_ENERGY = "energy";
	public static final String ATTRIBUTE_END_CHARGE = "endCharge";

	private final Id<Charger> chargerId;
	private final Id<ElectricVehicle> vehicleId;
	private final double energy;
	private final double endCharge;

	public EnergyChargedEvent(double time, Id<Charger> chargerId, Id<ElectricVehicle> vehicleId, double energy, double endCharge) {
		super(time);
		this.chargerId = chargerId;
		this.vehicleId = vehicleId;
		this.energy = energy;
		this.endCharge = endCharge;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	public Id<Charger> getChargerId() {
		return chargerId;
	}

	public Id<ElectricVehicle> getVehicleId() {
		return vehicleId;
	}

	public double getEnergy() {
		return energy;
	}

	public double getEndCharge() {
		return endCharge;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_CHARGER, chargerId + "");
		attr.put(ATTRIBUTE_VEHICLE, vehicleId + "");
		attr.put(ATTRIBUTE_ENERGY, energy + "");
		attr.put(ATTRIBUTE_END_CHARGE, endCharge + "");
		return attr;
	}
}
