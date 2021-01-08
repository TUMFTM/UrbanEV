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

package de.tum.mw.ftm.matsim.contrib.urban_ev;

import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.DischargingModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricFleetModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargingInfrastructureModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.stats.EvStatsModule;
import org.matsim.core.controler.AbstractModule;

public class EvModule extends AbstractModule {
	public static final String EV_COMPONENT = "EV_COMPONENT";

	@Override
	public void install() {
		bind(MobsimScopeEventHandling.class).asEagerSingleton();
		addControlerListenerBinding().to(MobsimScopeEventHandling.class);

		install(new ElectricFleetModule());
		install(new ChargingInfrastructureModule());
		install(new ChargingModule());
		install(new DischargingModule());
		install(new EvStatsModule());
	}
}
