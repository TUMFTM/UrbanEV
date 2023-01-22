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

package de.tum.mw.ftm.matsim.contrib.urban_ev;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.*;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.*;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.StartupListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Meant for event handlers that are created anew in each iteration and should operate only until the end of the current
 * mobsim. Typically, these are event handlers created in AbstractQSimModules.
 *
 * @author Michal Maciejewski (michalm)
 */
@Singleton
public class MobsimScopeEventHandling implements StartupListener, AfterMobsimListener {
	private final Collection<MobsimScopeEventHandler> eventHandlers = new ConcurrentLinkedQueue<>();
	private final EventsManager eventsManager;

	private int lastIteration = 0;

	@Inject
	public MobsimScopeEventHandling(EventsManager eventsManager) {
		this.eventsManager = eventsManager;
	}

	@Inject
	private ChargingInfrastructureSpecification chargingInfrastructureSpecification;

	@Inject
	private Population population;

	@Inject
	private ElectricFleetSpecification electricFleetSpecification;

	@Inject
	private OutputDirectoryHierarchy controlerIO;

	@Inject
	private IterationCounter iterationCounter;


	@Inject
	private Config config;

	@Inject
	private UrbanEVConfigGroup urbanEVConfig;

	public void addMobsimScopeHandler(MobsimScopeEventHandler handler) {
		eventHandlers.add(handler);
		eventsManager.addHandler(handler);
	}

	// Gets fired once in every MATSim run (multiple times when having initialization runs)
	@Override
	public void notifyStartup(StartupEvent startupEvent) {
		 
		lastIteration = config.controler().getLastIteration();

        population.getPersons().forEach((personId, person) -> {
			
			// add default range anxiety threshold to person attributes if none given
			if (person.getAttributes().getAttribute("rangeAnxietyThreshold") == null) {
				person.getAttributes().putAttribute("rangeAnxietyThreshold", String.valueOf(urbanEVConfig.getDefaultRangeAnxietyThreshold()));
			}
			
			// Add home and work chargers if necessary
			double homeChargerPower = person.getAttributes().getAttribute("homeChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("homeChargerPower").toString()) : 0.0;
			double workChargerPower = person.getAttributes().getAttribute("workChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("workChargerPower").toString()) : 0.0;

			if(homeChargerPower!=0.0) addPrivateCharger(person, "home", homeChargerPower);
			if(workChargerPower!=0.0) addPrivateCharger(person, "work", workChargerPower);
        });

		// Write final chargers to file
		ChargerWriter chargerWriter = new ChargerWriter(chargingInfrastructureSpecification.getChargerSpecifications().values().stream());
		chargerWriter.write(config.controler().getOutputDirectory().concat("/chargers_complete.xml"));
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		
		eventHandlers.forEach(eventsManager::removeHandler);
		eventHandlers.clear();

		if (iterationCounter.getIterationNumber() == lastIteration) {
			ElectricFleetWriter electricFleetWriter = new ElectricFleetWriter(electricFleetSpecification.getVehicleSpecifications().values().stream());
			electricFleetWriter.write(Paths.get(controlerIO.getOutputPath(),"output_evehicles.xml").toString());
		}
	}


	private void addPrivateCharger(Person person, String activityType, double power) {
		String ownerId = person.getId().toString();
		String chargerId = ownerId + "_" + activityType;
		Coord actCoord = new Coord();
		for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
			if (planElement instanceof Activity) {
				Activity act = (Activity) planElement;
				if (act.getType().startsWith(activityType)) {
					actCoord = act.getCoord();
					break;
				}
			}
		}

		// Add a charger only if an activity with the corresponding activityType exists within the person's plans
		if (actCoord.getX()!=0&&actCoord.getY()!=0){
			String chargerType = ChargerSpecification.DEFAULT_CHARGER_TYPE;
			int plugCount = ChargerSpecification.DEFAULT_PLUG_COUNT;
			List<Id<ElectricVehicle>> allowedEvIds = new ArrayList<>();
			allowedEvIds.add(Id.create(ownerId, ElectricVehicle.class));

			ChargerSpecification chargerSpecification = ImmutableChargerSpecification.newBuilder()
					.id(Id.create(chargerId, Charger.class))
					.coord(new Coord(actCoord.getX(), actCoord.getY()))
					.chargerType(chargerType)
					.plugPower(EvUnits.kW_to_W(power))
					.plugCount(plugCount)
					.allowedVehicles(allowedEvIds)
					.build();

			chargingInfrastructureSpecification.addChargerSpecification(chargerSpecification);

			// Also add the corresponding person attribute in case it was not set by the config
			if(activityType.contains("home")){
				person.getAttributes().putAttribute("homeChargerPower", String.valueOf(power));
			}
			else{
				person.getAttributes().putAttribute("workChargerPower", String.valueOf(power));
			}
		}
			
	}

}
