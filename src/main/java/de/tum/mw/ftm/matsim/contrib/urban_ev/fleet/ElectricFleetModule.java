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

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingPower;
import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.AuxEnergyConsumption;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.DriveEnergyConsumption;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ElectricFleetModule extends AbstractModule {
	@Inject
	private EvConfigGroup evCfg;

	@Inject
	private UrbanEVConfigGroup urbanEVCfg;

	@Override
	public void install() {
		bind(ElectricFleetSpecification.class).toProvider(() -> {
			Map<Id<ElectricVehicleType>, ElectricVehicleType> electricVehicleTypes = new HashMap<>();
			new ElectricVehicleTypesReader(electricVehicleTypes).parse(
					ConfigGroup.getInputFileURL(getConfig().getContext(), urbanEVCfg.getVehicleTypesFile()));
			ElectricFleetSpecification fleetSpecification = new ElectricFleetSpecificationImpl();
			new ElectricFleetReader(fleetSpecification, electricVehicleTypes).parse(
					ConfigGroup.getInputFileURL(getConfig().getContext(), evCfg.getVehiclesFile()));
			return fleetSpecification;
		}).asEagerSingleton();

		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				bind(ElectricFleet.class).toProvider(new Provider<ElectricFleet>() {
					@Inject
					private ElectricFleetSpecification fleetSpecification;
					@Inject
					private DriveEnergyConsumption.Factory driveConsumptionFactory;
					@Inject
					private AuxEnergyConsumption.Factory auxConsumptionFactory;
					@Inject
					private ChargingPower.Factory chargingPowerFactory;

					@Override
					public ElectricFleet get() {
						return ElectricFleets.createDefaultFleet(fleetSpecification, driveConsumptionFactory,
								auxConsumptionFactory, chargingPowerFactory);
					}
				}).asEagerSingleton();
			}
		});
	}
}
