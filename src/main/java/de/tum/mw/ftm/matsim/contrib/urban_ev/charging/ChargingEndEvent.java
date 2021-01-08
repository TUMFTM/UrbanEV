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

package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;

import java.util.Map;

public class ChargingEndEvent extends Event {
	public static final String EVENT_TYPE = "charging_end";
	public static final String ATTRIBUTE_CHARGER = "charger";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String ATTRIBUTE_ENDSOC = "soc";
	public static final String ATTRIBUTE_CHARGINGDUR = "charging_duration";

	private final Id<Charger> chargerId;
	private final Id<ElectricVehicle> vehicleId;
	private final Double soc;
	private final Double charging_duration;

	public ChargingEndEvent(double time, Id<Charger> chargerId, Id<ElectricVehicle> vehicleId, double soc, double charging_duration) {
		super(time);
		this.chargerId = chargerId;
		this.vehicleId = vehicleId;
		this.soc = soc;
		this.charging_duration = charging_duration;
	}

	public Id<Charger> getChargerId() {
		return chargerId;
	}

	public Id<ElectricVehicle> getVehicleId() {
		return vehicleId;
	}

	public Double getCharging_duration() {
		return charging_duration;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	public Double getSoc() {
		return soc;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_CHARGER, chargerId.toString());
		attr.put(ATTRIBUTE_VEHICLE, vehicleId.toString());
		attr.put(ATTRIBUTE_ENDSOC, soc.toString());
		attr.put(ATTRIBUTE_CHARGINGDUR, charging_duration.toString());
		return attr;
	}
}
