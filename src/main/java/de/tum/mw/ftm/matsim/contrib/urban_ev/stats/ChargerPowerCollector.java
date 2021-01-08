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

package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;/*
 * created by jbischoff, 26.10.2018
 */

import com.google.inject.Inject;
import de.tum.mw.ftm.matsim.contrib.urban_ev.MobsimScopeEventHandling;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.*;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricFleet;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargingInfrastructure;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEventHandler;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.MobsimScopeEventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChargerPowerCollector
		implements ChargingStartEventHandler, ChargingEndEventHandler, UnpluggingEventHandler, ChargingBehaviourScoringEventHandler, MobsimScopeEventHandler {

	private final ChargingInfrastructure chargingInfrastructure;
	private final ElectricFleet fleet;
	private HashMap<Id<ElectricVehicle>, ChargingLogEntry> activeChargingProcesses = new HashMap<>();

	private List<ChargingLogEntry> logList = new ArrayList<>();

	@Inject
	public ChargerPowerCollector(ElectricFleet fleet, ChargingInfrastructure chargingInfrastructure,
			MobsimScopeEventHandling events) {
		this.fleet = fleet;
		this.chargingInfrastructure = chargingInfrastructure;
		events.addMobsimScopeHandler(this);
	}

	@Override
	public void handleEvent(ChargingStartEvent event) {
		ElectricVehicle ev = this.fleet.getElectricVehicles().get(event.getVehicleId());

		if (ev != null) {
			// Todo: Maybe handle the possibility that there might be an ongoing charging process for that vehicle
			ChargingLogEntry chargingProcess = new ChargingLogEntry(ev.getId());
			chargingProcess.setCharger(this.chargingInfrastructure.getChargers().get(event.getChargerId()));
			chargingProcess.setStartTime(event.getTime());
			chargingProcess.setStartSOC(ev.getBattery().getSoc()/ev.getBattery().getCapacity());
			chargingProcess.setStartSOC_J(ev.getBattery().getSoc());
			this.activeChargingProcesses.put(ev.getId(), chargingProcess);

		} else
			throw new NullPointerException(event.getVehicleId().toString() + " is not in list");
	}

	@Override
	public void handleEvent(ChargingEndEvent event) {
		ElectricVehicle ev = this.fleet.getElectricVehicles().get(event.getVehicleId());
		if (ev != null) {
			// Todo: Maybe handle the possibility that there might not be an ongoing charging process for that vehicle
			// Todo: Maybe handle the possibility that there is an ongoing charging process for that vehicle that does
			//  not take place at the expected charger
			ChargingLogEntry chargingProcess = this.activeChargingProcesses.get(ev.getId());
			chargingProcess.setEndTime(event.getTime());
			chargingProcess.setEndSOC_J(ev.getBattery().getSoc());
			chargingProcess.setEndSOC(ev.getBattery().getSoc()/ev.getBattery().getCapacity());
			chargingProcess.setChargingDuration(event.getCharging_duration());
			chargingProcess.setTransmittedEnergy_J(chargingProcess.getEndSOC_J()-chargingProcess.getStartSOC_J());
		} else
			throw new NullPointerException(event.getVehicleId().toString() + " is not in list");

	}

	@Override
	public void handleEvent(UnpluggingEvent event) {

		ElectricVehicle ev = this.fleet.getElectricVehicles().get(event.getVehicleId());

		if (ev != null) {
			if (activeChargingProcesses.containsKey(ev.getId())){
				ChargingLogEntry chargingProcess = this.activeChargingProcesses.remove(ev.getId());
				chargingProcess.setUnplugTime(event.getTime());
				chargingProcess.setPluggedDuration(chargingProcess.getUnplugTime()-chargingProcess.getStartTime());
				chargingProcess.setChargingRatio(chargingProcess.getChargingDuration()/chargingProcess.getPluggedDuration());

				if(chargingProcess.complete()&&chargingProcess.valid()){
					logList.add(chargingProcess);
				} else
					throw new RuntimeException("Failed to add invalid or incomplete ChargingLogEntry to logList!");
			} else
				throw new RuntimeException(event.getVehicleId().toString() + " has no ongoing charging process at charger " + event.getChargerId().toString());


		} else
			throw new NullPointerException(event.getVehicleId().toString() + " is not in list");
	}

	@Override
	public void handleEvent(ChargingBehaviourScoringEvent event) {
		// Todo: Maybe handle the possibility that there might not be an ongoing charging process for that vehicle
		// Todo: Maybe handle the possibility that there is an ongoing charging process for that vehicle that does
		//  not take place at the expected charger
		ElectricVehicle ev = this.fleet.getElectricVehicles().get(Id.create(event.getPersonId(), ElectricVehicle.class));

		if (ev != null) {
			if(this.activeChargingProcesses.containsKey(ev.getId())) {
				ChargingLogEntry chargingProcess = this.activeChargingProcesses.get(ev.getId());
				chargingProcess.setWalkingDistance(event.getWalkingDistance());
			}
		} else
			throw new NullPointerException(ev.getId().toString() + " is not in list");

	}

	public List<ChargingLogEntry> getLogList() {
		return logList;
	}
}
