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

import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingLogic;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.List;
import java.util.Objects;

public class ChargerImpl implements Charger {
	/**
	 * @param specification charger specification
	 * @param link          link at which the charger is located
	 * @param logicFactory  ChargingLogic factory
	 */
	public static Charger create(ChargerSpecification specification, Link link, ChargingLogic.Factory logicFactory) {

		ChargerImpl charger = new ChargerImpl(specification, link);
		charger.logic = Objects.requireNonNull(logicFactory.create(charger));
		return charger;
	}

	private final ChargerSpecification specification;
	private final Link link;
	private ChargingLogic logic;

	private ChargerImpl(ChargerSpecification specification, Link link) {
		this.specification = specification;
		this.link = link;
	}

	@Override
	public ChargingLogic getLogic() {
		return logic;
	}

	@Override
	public Id<Charger> getId() {
		return specification.getId();
	}

	@Override
	public Coord getCoord() { return specification.getCoord(); }

	@Override
	public Link getLink() { return link; }

	@Override
	public String getChargerType() {
		return specification.getChargerType();
	}

	@Override
	public double getPlugPower() {
		return specification.getPlugPower();
	}

	@Override
	public int getPlugCount() {
		return specification.getPlugCount();
	}

	@Override
	public List<Id<ElectricVehicle>> getAllowedVehicles() { return specification.getAllowedVehicles(); }

}
