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

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.EvModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ChargingModule extends AbstractModule {
	@Override
	public void install() {
		bind(ChargingLogic.Factory.class).toProvider(new Provider<ChargingLogic.Factory>() {
			@Inject
			private EventsManager eventsManager;

			@Override
			public ChargingLogic.Factory get() {
				/**return charger -> new ChargingLogicImpl(charger, new ChargeUpToTypeMaxSocStrategy(charger),
						eventsManager);
				 */
				return charger -> new ChargingWithQueueingLogic(charger, new ChargeUpToTypeMaxSocStrategy(charger),
						eventsManager);
			}
		});

		bind(ChargingPower.Factory.class).toInstance(ev -> VariableSpeedCharging.createForMaxChargingRate(ev));

		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				this.bind(ChargingHandler.class).asEagerSingleton();
				this.addQSimComponentBinding(EvModule.EV_COMPONENT).to(ChargingHandler.class);
			}
		});
	}
}
