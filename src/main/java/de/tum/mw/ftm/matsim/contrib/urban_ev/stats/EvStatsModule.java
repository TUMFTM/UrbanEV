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

package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import com.google.inject.Inject;
import de.tum.mw.ftm.matsim.contrib.urban_ev.EvModule;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

/**
 * @author Michal Maciejewski (michalm)
 */
public class EvStatsModule extends AbstractModule {
	@Inject
	private EvConfigGroup evCfg;

	@Override
	public void install() {
		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				if (evCfg.getTimeProfiles()) {
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							SocHistogramTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							IndividualSocTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							ChargerOccupancyTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							ChargerTypeOccupancyTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(ChargerOccupancyXYDataProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							VehicleTypeAggregatedSocTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).to(EvMobsimListener.class);
					bind(ChargerPowerCollector.class).asEagerSingleton();
					bind(ChargerOccupancyHistoryCollector.class).asEagerSingleton();
					// add more time profiles if necessary
				}
			}
		});
	}
}
