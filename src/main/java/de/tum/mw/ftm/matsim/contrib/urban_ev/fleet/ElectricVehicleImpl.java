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

package de.tum.mw.ftm.matsim.contrib.urban_ev.fleet;

import com.google.common.collect.ImmutableList;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingPower;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.AuxEnergyConsumption;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.DriveEnergyConsumption;
import org.matsim.api.core.v01.Id;

import java.util.Objects;

public class ElectricVehicleImpl implements ElectricVehicle {
	public static ElectricVehicle create(ElectricVehicleSpecification vehicleSpecification,
			DriveEnergyConsumption.Factory driveFactory, AuxEnergyConsumption.Factory auxFactory,
			ChargingPower.Factory chargingFactory) {
		ElectricVehicleImpl ev = new ElectricVehicleImpl(vehicleSpecification);
		ev.driveEnergyConsumption = Objects.requireNonNull(driveFactory.create(ev));
		ev.auxEnergyConsumption = Objects.requireNonNull(auxFactory.create(ev));
		ev.chargingPower = Objects.requireNonNull(chargingFactory.create(ev));
		return ev;
	}

	private final ElectricVehicleSpecification vehicleSpecification;
	private final Battery battery;

	private DriveEnergyConsumption driveEnergyConsumption;
	private AuxEnergyConsumption auxEnergyConsumption;
	private ChargingPower chargingPower;

	private ElectricVehicleImpl(ElectricVehicleSpecification vehicleSpecification) {
		this.vehicleSpecification = vehicleSpecification;
		battery = new BatteryImpl(vehicleSpecification.getBatteryCapacity(), vehicleSpecification.getInitialSoc());
	}

	@Override
	public Id<ElectricVehicle> getId() {
		return vehicleSpecification.getId();
	}

	@Override
	public Battery getBattery() {
		return battery;
	}

	@Override
	public ElectricVehicleType getVehicleType() {
		return vehicleSpecification.getVehicleType();
	}

	@Override
	public ImmutableList<String> getChargerTypes() {
		return vehicleSpecification.getChargerTypes();
	}

	@Override
	public DriveEnergyConsumption getDriveEnergyConsumption() {
		return driveEnergyConsumption;
	}

	@Override
	public AuxEnergyConsumption getAuxEnergyConsumption() {
		return auxEnergyConsumption;
	}

	@Override
	public ChargingPower getChargingPower() {
		return chargingPower;
	}
}
