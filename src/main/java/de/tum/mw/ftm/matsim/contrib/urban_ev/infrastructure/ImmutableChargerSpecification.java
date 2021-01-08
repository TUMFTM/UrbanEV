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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import java.util.List;
import java.util.Objects;

/**
 * Immutable implementation of ChargerSpecification
 *
 * @author Michal Maciejewski (michalm)
 */

public class ImmutableChargerSpecification implements ChargerSpecification {
	private final Id<Charger> id;
	private final Coord coord;
	private final String chargerType;
	private final double plugPower;
	private final int plugCount;
	private final List<Id<ElectricVehicle>> allowedVehicles;


	private ImmutableChargerSpecification(Builder builder) {
		id = Objects.requireNonNull(builder.id);
		coord = Objects.requireNonNull(builder.coord);
		chargerType = Objects.requireNonNull(builder.chargerType);
		plugPower = Objects.requireNonNull(builder.plugPower);
		plugCount = Objects.requireNonNull(builder.plugCount);
		allowedVehicles = Objects.requireNonNull(builder.allowedVehicles);

		Preconditions.checkArgument(plugPower >= 0, "Negative plugPower of charger: %s", id);
		Preconditions.checkArgument(plugCount >= 0, "Negative plugCount of charger: %s", id);
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(ChargerSpecification copy) {
		Builder builder = new Builder();
		builder.id = copy.getId();
		builder.coord = copy.getCoord();
		builder.chargerType = copy.getChargerType();
		builder.plugPower = copy.getPlugPower();
		builder.plugCount = copy.getPlugCount();
		builder.allowedVehicles = copy.getAllowedVehicles();
		return builder;
	}

	@Override
	public Id<Charger> getId() {
		return id;
	}

	@Override
	public Coord getCoord() {
		return coord;
	}

	@Override
	public String getChargerType() {
		return chargerType;
	}

	@Override
	public double getPlugPower() {
		return plugPower;
	}

	@Override
	public int getPlugCount() {
		return plugCount;
	}

	@Override
	public List<Id<ElectricVehicle>> getAllowedVehicles() {
		return allowedVehicles;
	}


	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("coord", coord)
				.add("chargerType", chargerType)
				.add("plugPower", plugPower)
				.add("plugCount", plugCount)
				.add("allowedVehicles", allowedVehicles)
				.toString();
	}

	public static final class Builder {
		private Id<Charger> id;
		private Coord coord;
		private String chargerType;
		private Double plugPower;
		private Integer plugCount;
		private List<Id<ElectricVehicle>> allowedVehicles;

		private Builder() {
		}

		public Builder id(Id<Charger> val) {
			id = val;
			return this;
		}

		public Builder coord(Coord val) {
			coord = val;
			return this;
		}

		public Builder chargerType(String val) {
			chargerType = val;
			return this;
		}

		public Builder plugPower(double val) {
			plugPower = val;
			return this;
		}

		public Builder plugCount(int val) {
			plugCount = val;
			return this;
		}

		public Builder allowedVehicles(List<Id<ElectricVehicle>> val) {
			allowedVehicles = val;
			return this;
		}

		public ImmutableChargerSpecification build() {
			return new ImmutableChargerSpecification(this);
		}
	}
}
