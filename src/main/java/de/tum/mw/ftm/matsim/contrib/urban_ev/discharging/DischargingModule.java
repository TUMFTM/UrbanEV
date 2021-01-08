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

package de.tum.mw.ftm.matsim.contrib.urban_ev.discharging;

import de.tum.mw.ftm.matsim.contrib.urban_ev.EvModule;
import org.matsim.contrib.ev.temperature.TemperatureService;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

/**
 * @author Michal Maciejewski (michalm)
 */
public class DischargingModule extends AbstractModule {
	@Override
	public void install() {
		bind(DriveEnergyConsumption.Factory.class).toInstance(SimpleDriveEnergyConsumption::new);
		bind(TemperatureService.class).toInstance(linkId -> 15);// XXX fixed temperature 15 oC
		bind(AuxEnergyConsumption.Factory.class).to(OhdeSlaskiAuxEnergyConsumption.Factory.class).asEagerSingleton();

		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				this.bind(DriveDischargingHandler.class).asEagerSingleton();
				this.bind(AuxDischargingHandler.class).asEagerSingleton();
				this.addQSimComponentBinding(EvModule.EV_COMPONENT).to(AuxDischargingHandler.class);

				//by default, no vehicle will be AUX-discharged when not moving
				this.bind(AuxDischargingHandler.VehicleProvider.class).toInstance(event -> null);
			}
		});
	}
}
