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
/*
 * created by jbischoff, 09.10.2018
 *  This is an events based approach to trigger vehicle charging. Vehicles will be charged as soon as a person begins a charging activity.
 */

import de.tum.mw.ftm.matsim.contrib.urban_ev.MobsimScopeEventHandling;
import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricFleet;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargingInfrastructure;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent.ScoreTrigger;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.util.PartialSort;
import org.matsim.contrib.util.distance.DistanceUtils;
import org.matsim.core.api.experimental.events.EventsManager;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VehicleChargingHandler
		implements ActivityStartEventHandler, ActivityEndEventHandler,
		MobsimScopeEventHandler {

	private static final Logger log = Logger.getLogger(VehicleChargingHandler.class);

	public static final String CHARGING_IDENTIFIER = " charging";
	public static final Integer SECONDS_PER_MINUTE = 60;
	public static final Integer SECONDS_PER_HOUR = 60*SECONDS_PER_MINUTE;
	public static final Integer SECONDS_PER_DAY = 24*SECONDS_PER_HOUR;
	private Map<Id<ElectricVehicle>, Id<Charger>> vehiclesAtChargers = new HashMap<>();

	private final ChargingInfrastructure chargingInfrastructure;
	private final Network network;
	private final ElectricFleet electricFleet;
	private final Population population;
	private final double parkingSearchRadius;
	private final double hoggingExemptionHourStart;
	private final double hoggingExemptionHourStop;
	private final double hoggingThresholdMinutes;
	private final EventsManager eventsManager;

	private enum ChargerSelectionMethod{
		CLOSEST,
		RANDOMOFCLOSEST
	}

	@Inject
	public VehicleChargingHandler(ChargingInfrastructure chargingInfrastructure,
								  Network network,
								  ElectricFleet electricFleet,
								  Population population,
								  EventsManager eventsManager,
								  MobsimScopeEventHandling events,
								  UrbanEVConfigGroup urbanEVCfg) {
		this.chargingInfrastructure = chargingInfrastructure;
		this.network = network;
		this.electricFleet = electricFleet;
		this.population = population;
		this.eventsManager = eventsManager;
		this.parkingSearchRadius = urbanEVCfg.getParkingSearchRadius();
		this.hoggingExemptionHourStart = urbanEVCfg.getHoggingExemptionHourStart();
		this.hoggingExemptionHourStop = urbanEVCfg.getHoggingExemptionHourStop();
		this.hoggingThresholdMinutes = urbanEVCfg.getStationHoggingThresholdMinutes();
		events.addMobsimScopeHandler(this);
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {

		String actType = event.getActType();
		Id<Person> personId = event.getPersonId();

		if (personId != null) {

			Id<ElectricVehicle> evId = Id.create(personId, ElectricVehicle.class);

			if (electricFleet.getElectricVehicles().containsKey(evId)) {
				
				ElectricVehicle ev = electricFleet.getElectricVehicles().get(evId);
				Person person = population.getPersons().get(personId);
				double walkingDistance = 0.0;

				if (event.getActType().endsWith(CHARGING_IDENTIFIER)) {
					
					Activity activity = getActivity(person, event.getTime());
					Coord activityCoord = activity != null ? activity.getCoord() : network.getLinks().get(event.getLinkId()).getCoord();

					// Location choice
					List<Charger> suitableChargers = findSuitableChargers(activityCoord, ev);
					Charger selectedCharger = null;

					// Use private charger if possible
					for(Charger charger : suitableChargers)
					{
						if (charger.getAllowedVehicles().contains(evId))
						{
							selectedCharger = charger;
							break;
						}
					}

					// Select public charger if no private charger is available
					if(selectedCharger == null)
					{
						selectedCharger = selectCharger(suitableChargers, activityCoord, ChargerSelectionMethod.CLOSEST);
					}
					
					// Start charging if possible
					if (selectedCharger != null) { // if charger was found, start charging
						selectedCharger.getLogic().addVehicle(ev, event.getTime());
						vehiclesAtChargers.put(evId, selectedCharger.getId());
						walkingDistance = DistanceUtils.calculateDistance(
								activityCoord, selectedCharger.getCoord());
					} else {
						// if no charger was found, mark as failed attempt in plan
						if (activity != null) {
							actType = activity.getType() + " failed";
							activity.setType(actType);
						}
					}
				}

				double time = event.getTime();
				double socUponArrival = ev.getBattery().getSoc() / ev.getBattery().getCapacity();
				double startSoc = ev.getBattery().getStartSoc() / ev.getBattery().getCapacity();

				// Issue a charging behaviour scoring event
				// Needed to score walking and catch empty or low soc after driving
				eventsManager.processEvent(new ChargingBehaviourScoringEvent(
					time,
					personId,
					actType,
					socUponArrival,
					startSoc,
					walkingDistance,
					0.0,
					false,
					ScoreTrigger.ACTIVITYSTART
					)
					);
			}
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {

		String actType = event.getActType();
		
		// If the last activity included charging
		if (actType.endsWith(CHARGING_IDENTIFIER)) {

			Id<Person> personId = event.getPersonId();
			Id<ElectricVehicle> evId = Id.create(personId, ElectricVehicle.class);

			// If the vehicle is currently plugged in
			if(vehiclesAtChargers.containsKey(evId))
			{
				Person person = population.getPersons().get(personId);
				ElectricVehicle ev = electricFleet.getElectricVehicles().get(evId);

				Id<Charger> chargerId = vehiclesAtChargers.get(evId);
				Charger charger = chargingInfrastructure.getChargers().get(chargerId);
				Activity activity = getActivity(person, event.getTime());
				Coord activityCoord = activity != null ? activity.getCoord() : network.getLinks().get(event.getLinkId()).getCoord();

				ChargingLogicImpl chargingLogic = (ChargingLogicImpl) charger.getLogic();
				double plugInTS = chargingLogic.getPlugInTimestamps().get(evId);

				unplugVehicle(evId, event.getTime());

				double time = event.getTime();
				double socUponDeparture = ev.getBattery().getSoc() / ev.getBattery().getCapacity();
				double startSoc = ev.getBattery().getStartSoc() / ev.getBattery().getCapacity();
				double walkingDistance = DistanceUtils.calculateDistance(activityCoord, charger.getCoord());
				double pluggedDuration = event.getTime()-plugInTS;
				
				// Determine whether hogging is an issue
				Boolean hogging = isHogging(plugInTS, event.getTime(), hoggingExemptionHourStart, hoggingExemptionHourStop, hoggingThresholdMinutes);

				// Issue a charging behaviour scoring event
				// Needed to score the charging process itself 
				eventsManager.processEvent(
					new ChargingBehaviourScoringEvent(
						time,
						personId,
						actType,
						socUponDeparture,
						startSoc,
						walkingDistance,
						pluggedDuration,
						hogging,
						ScoreTrigger.ACTIVITYEND
						)
					);
			}
		}

	}

	// @Override
	// public void handleEvent(ChargingEndEvent event) {
	//	// vehiclesAtChargers.remove(event.getVehicleId());
	//	// Charging has ended before activity ends
	// }

	/**
	 * gets ativity from agent's plan by looking for current time
	 * @param person
	 * @param time
	 * @return
	 */
	private Activity getActivity(Person person, double time){
		Activity activity = null;
		List<PlanElement> planElements = person.getSelectedPlan().getPlanElements();
		for (int i = 0; i < planElements.size(); i++) {
			PlanElement planElement = planElements.get(i);
			if (planElement instanceof Activity) {
				if (((Activity) planElement).getEndTime().isDefined()) {
					double activityEndTime = ((Activity) planElement).getEndTime().seconds();
					if (activityEndTime >= time || i == planElements.size() - 1) {
						activity = ((Activity) planElement);
						break;
					}
				}
				else if (i == planElements.size() - 1) {
					// Accept a missing end time for the last activity of a plan
					activity = ((Activity) planElement);
					break;
				}
				else{
					// There is a missing end time for an activity that is not the plan's last -> This should end in null being returned
					continue;
				}
			}
		}
		if (activity != null) {
			return activity;
		}
		else return null;
	}

	/**
	 * Tries to find the closest free chargers of fitting type in vicinity of activity location
	 * If a charger is private, only allowed vehicles can charge there
	 */

	private List<Charger> findSuitableChargers(Coord stopCoord, ElectricVehicle electricVehicle) {

		List<Charger> filteredChargers = new ArrayList<>();

		chargingInfrastructure.getChargers().values().forEach(charger -> {
			// filter out private chargers unless vehicle is allowed
			if (charger.getAllowedVehicles().isEmpty() || charger.getAllowedVehicles().contains(electricVehicle.getId())) {
				// filter out chargers that are out of range
				if (DistanceUtils.calculateDistance(stopCoord, charger.getCoord()) < parkingSearchRadius) {
					// filter out chargers with wrong type
					if (electricVehicle.getChargerTypes().contains(charger.getChargerType())) {
						// filter out occupied chargers
						if ((charger.getLogic().getPluggedVehicles().size() < charger.getPlugCount())) {
							filteredChargers.add(charger);
						}
					}
				}
			}
		});

		return filteredChargers;
	}

	private Charger selectCharger(List<Charger> suitableChargers, Coord stopCoord, ChargerSelectionMethod method) {

		final int N_CONSIDERED_CANDIDATES = 3;
		Charger selectedCharger = null;

		if(suitableChargers.isEmpty())
		{
			// if we do not have suitable chargers available
			selectedCharger = null;
		}
		else{
			// if there are suitable chargers available 
			if(method==ChargerSelectionMethod.CLOSEST || method==ChargerSelectionMethod.RANDOMOFCLOSEST)
			{
				// If the location is chosen based on its distance, sort all candidates
				List<Charger> nearestChargers = PartialSort.kSmallestElements(N_CONSIDERED_CANDIDATES, suitableChargers.stream(),
				(charger) -> DistanceUtils.calculateSquaredDistance(stopCoord, charger.getCoord()));

				if(method==ChargerSelectionMethod.CLOSEST)
				{
					// Select the closest charger
					selectedCharger = nearestChargers.get(0);
				}
				else
				{
					// Select one of the N_CONSIDERED_CANDIDATES closest chargers
					Random rand = new Random();
					selectedCharger = nearestChargers.get(rand.nextInt(nearestChargers.size()));
				}

			}
		}
	
		return selectedCharger;

	}

	private void unplugVehicle(Id<ElectricVehicle> evId, double time)
	{
		if (evId != null) {
			Id<Charger> chargerId = vehiclesAtChargers.remove(evId);
			if (chargerId != null) {
				Charger charger = chargingInfrastructure.getChargers().get(chargerId);
				charger.getLogic().removeVehicle(electricFleet.getElectricVehicles().get(evId), time);
			}
		}
	}
	
	private double lastMidnight(double t)
	{
		return SECONDS_PER_DAY*((int) t/SECONDS_PER_DAY);
	}

	private double nextMidnight(double t)
	{
		return lastMidnight(t)+SECONDS_PER_DAY;
	}

	private boolean isHogging(double plugInTS, double plugOutTS, double hoggingExemptionHourStart, double hoggingExemptionHourStop, double hoggingThresholdMinutes)
	{

		// Normalize all non-second timestamps
		double hoggingExemptionStart = this.hoggingExemptionHourStart*SECONDS_PER_HOUR;
		double hoggingExemptionStop = this.hoggingExemptionHourStop*SECONDS_PER_HOUR;
		double hoggingThreshold = this.hoggingThresholdMinutes*SECONDS_PER_MINUTE;

		// Depending on the relative arrival time...
		double arrivalWithinDay = plugInTS%SECONDS_PER_DAY;

		// ... determine absolute time from which on a transaction is considered as hogging
		double plugOutTSPenalty;

		if(
			arrivalWithinDay >= hoggingExemptionStop &&
			arrivalWithinDay <= hoggingExemptionStart-hoggingThreshold	
			)
		{
			// If the car arrived within hogging controlled time and is technically able to violate the threshold on this day
			plugOutTSPenalty = plugInTS+hoggingThreshold; // The car needs to leave before the hoggingThreshold
		}
		else if (arrivalWithinDay>hoggingExemptionStart-hoggingThreshold){
			// If the car arrived so late that it can technically not violate the threshold on the arrival day
			plugOutTSPenalty = nextMidnight(plugInTS) + hoggingExemptionStop + hoggingThreshold; // The car needs to leave before the earliest hoggingThreshold the next day
		}
		else {
			// If the car arrived before the start of the hogging controlled time -> arrivalWithinDay<hoggingExemptionStop
			plugOutTSPenalty = lastMidnight(plugInTS)+hoggingExemptionStop+hoggingThreshold; // The car needs to leave before the earliest hoggingThreshold this day
		}

		// Check whether the vehicle was connected too long
		return plugOutTS>plugOutTSPenalty;
	}

}
