/* *********************************************************************** *
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

import com.google.common.base.Preconditions;
import org.matsim.api.core.v01.Id;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.QSim;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ChargingWithQueueingLogic implements ChargingLogic {
	protected final Charger charger;
	private final ChargingStrategy chargingStrategy;
	private final EventsManager eventsManager;

	private final Map<Id<ElectricVehicle>, ElectricVehicle> pluggedVehicles = new LinkedHashMap<>();
	private final Queue<ElectricVehicle> queuedVehicles = new LinkedList<>();
	private final Queue<ElectricVehicle> arrivingVehicles = new LinkedBlockingQueue<>();
	private final Map<Id<ElectricVehicle>, ChargingListener> listeners = new LinkedHashMap<>();
	private final Map<Id<ElectricVehicle>, Double> plugInTimestamps = new LinkedHashMap<>();

	public ChargingWithQueueingLogic(Charger charger, ChargingStrategy chargingStrategy, EventsManager eventsManager) {
		this.chargingStrategy = Objects.requireNonNull(chargingStrategy);
		this.charger = Objects.requireNonNull(charger);
		this.eventsManager = Objects.requireNonNull(eventsManager);
	}

	@Override
	public void chargeVehicles(double chargePeriod, double now) {
		Iterator<ElectricVehicle> evIter = pluggedVehicles.values().iterator();
		while (evIter.hasNext()) {
			ElectricVehicle ev = evIter.next();
			// with fast charging, we charge around 4% of SOC per minute,
			// so when updating SOC every 10 seconds, SOC increases by less then 1%
			//double oldCharge = ev.getBattery().getCharge();
			//double energy = ev.getChargingPower().calcChargingPower(charger) * chargePeriod;
			//double newCharge = Math.min(oldCharge + energy, ev.getBattery().getCapacity());
			//ev.getBattery().setCharge(newCharge);
			ev.getBattery().changeSoc(ev.getChargingPower().calcChargingPower(charger) * chargePeriod);
			
			
			//eventsManager.processEvent(new EnergyChargedEvent(now, charger.getId(), ev.getId(), newCharge - oldCharge, newCharge));

			if (chargingStrategy.isChargingCompleted(ev)) {
				//evIter.remove();
				eventsManager.processEvent(new ChargingEndEvent(now, charger.getId(), ev.getId(), ev.getBattery().getSoc()/ ev.getBattery().getCapacity(),now-plugInTimestamps.get(ev.getId())));
				//listeners.remove(ev.getId()).notifyChargingEnded(ev, now);
			}
		}

		int queuedToPluggedCount = Math.min(queuedVehicles.size(), charger.getPlugCount() - pluggedVehicles.size());
		for (int i = 0; i < queuedToPluggedCount; i++) {
			plugVehicle(queuedVehicles.poll(), now);
		}

		Iterator<ElectricVehicle> arrivingVehiclesIter = arrivingVehicles.iterator();
		while (arrivingVehiclesIter.hasNext()) {
			ElectricVehicle ev = arrivingVehiclesIter.next();
			if (pluggedVehicles.size() < charger.getPlugCount()) {
				plugVehicle(ev, now);
			} else {
				queueVehicle(ev, now);
			}
			arrivingVehiclesIter.remove();
		}
	}

	@Override
	public void addVehicle(ElectricVehicle ev, double now) {
		addVehicle(ev, new ChargingListener() {
		}, now);
	}

	@Override
	public void addVehicle(ElectricVehicle ev, ChargingListener chargingListener, double now) {
		arrivingVehicles.add(ev);
		listeners.put(ev.getId(), chargingListener);
	}

	@Override
	public void removeVehicle(ElectricVehicle ev, double now) {
		if (pluggedVehicles.remove(ev.getId()) != null) {// successfully removed
			eventsManager.processEvent(new ChargingEndEvent(now, charger.getId(), ev.getId(), ev.getBattery().getSoc()/ ev.getBattery().getCapacity(),now-plugInTimestamps.get(ev.getId())));
			listeners.remove(ev.getId()).notifyChargingEnded(ev, now);
			eventsManager.processEvent(new UnpluggingEvent(now, charger.getId(), ev.getId(), now-plugInTimestamps.get(ev.getId())));

			if (!queuedVehicles.isEmpty()) {
				plugVehicle(queuedVehicles.poll(), now);
			}
		} else {
			// make sure ev was in the queue
			Preconditions.checkState(queuedVehicles.remove(ev), "Vehicle (%s) is neither queued nor plugged at charger (%s)", ev.getId(), charger.getId());
			eventsManager.processEvent(new QuitQueueAtChargerEvent(now, charger.getId(), ev.getId()));
		}
	}

	private void queueVehicle(ElectricVehicle ev, double now) {
		queuedVehicles.add(ev);
		eventsManager.processEvent(new QueuedAtChargerEvent(now, charger.getId(), ev.getId()));
		listeners.get(ev.getId()).notifyVehicleQueued(ev, now);
	}

	private void plugVehicle(ElectricVehicle ev, double now) {
		if (pluggedVehicles.put(ev.getId(), ev) != null) {
			throw new IllegalArgumentException();
		}

		eventsManager.processEvent(new ChargingStartEvent(now, charger.getId(), ev.getId(), charger.getChargerType()));
		listeners.get(ev.getId()).notifyChargingStarted(ev, now);
		plugInTimestamps.put(ev.getId(), now);
	}

	private final Collection<ElectricVehicle> unmodifiablePluggedVehicles = Collections.unmodifiableCollection(pluggedVehicles.values());

	@Override
	public Collection<ElectricVehicle> getPluggedVehicles() {
		return unmodifiablePluggedVehicles;
	}

	private final Collection<ElectricVehicle> unmodifiableQueuedVehicles = Collections.unmodifiableCollection(queuedVehicles);

	@Override
	public Collection<ElectricVehicle> getQueuedVehicles() {
		return unmodifiableQueuedVehicles;
	}

	@Override
	public ChargingStrategy getChargingStrategy() {
		return chargingStrategy;
	}
	public Map<Id<ElectricVehicle>, Double> getPlugInTimestamps(){
		return plugInTimestamps;
	}
}
