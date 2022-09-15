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
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.utils.misc.Time;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
	private Random random = new Random();
	private StrategyManager strategyManager;
	private static final String CHARGING_IDENTIFIER = " charging";

	private int iterationNumber = 0;
	private int lastIteration = 0;
	private double endTime = 0;

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
	private MatsimServices matsimServices;

	@Inject
	private Config config;

	@Inject
	private UrbanEVConfigGroup urbanEVConfig;

	public void addMobsimScopeHandler(MobsimScopeEventHandler handler) {
		eventHandlers.add(handler);
		eventsManager.addHandler(handler);
	}


	@Override
	public void notifyStartup(StartupEvent startupEvent) {
		lastIteration = config.controler().getLastIteration();
		endTime = config.qsim().getEndTime().seconds();
		strategyManager = matsimServices.getStrategyManager();
		UrbanEVConfigGroup urbanEVConfigGroup = (UrbanEVConfigGroup) config.getModules().get("urban_ev");
		double opportunityChargingShare = urbanEVConfigGroup.getOpportunityChargingShare();

        population.getPersons().forEach((personId, person) -> {

			// add default range anxiety threshold to person attributes if none given
			if (person.getAttributes().getAttribute("rangeAnxietyThreshold") == null) {
				person.getAttributes().putAttribute("rangeAnxietyThreshold", String.valueOf(urbanEVConfig.getDefaultRangeAnxietyThreshold()));
			}

			// add work and home chargers
			double homeChargerPower;
			double workChargerPower;

			// Determine home charging power
			if(!urbanEVConfig.isGenerateHomeChargersByPercentage()) {
				// Generate home chargers based on population attributes
				homeChargerPower = person.getAttributes().getAttribute("homeChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("homeChargerPower").toString()) : 0.0;
			} else {
				if(random.nextDouble()<=urbanEVConfig.getHomeChargerPercentage()/100.0){
					// Randomly assign home charger with the corresponding probability
					homeChargerPower = urbanEVConfig.getDefaultHomeChargerPower();
				} else {
					homeChargerPower = 0.0;
				}
			}

			// Determine work charging power
			if(!urbanEVConfig.isGenerateWorkChargersByPercentage()) {
				// Generate work chargers based on population attributes
				workChargerPower = person.getAttributes().getAttribute("workChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("workChargerPower").toString()) : 0.0;
			} else {
				if(random.nextDouble()<=urbanEVConfig.getWorkChargerPercentage()/100.0){
					// Randomly assign work charger with the corresponding probability
					workChargerPower = urbanEVConfig.getDefaultWorkChargerPower();
				} else {
					workChargerPower = 0.0;
				}
			}

			// Add home and work chargers if necessary
			if(homeChargerPower!=0.0) addPrivateCharger(person, "home", homeChargerPower);
			if(workChargerPower!=0.0) addPrivateCharger(person, "work", workChargerPower);
			
			// Handle opportunity charging characteristics
			determineOpportunityChargingStatus(person, homeChargerPower, workChargerPower, opportunityChargingShare);
			
        });

        // Write final chargers to file
		ChargerWriter chargerWriter = new ChargerWriter(chargingInfrastructureSpecification.getChargerSpecifications().values().stream());
		chargerWriter.write(config.controler().getOutputDirectory().concat("/chargers_complete.xml"));
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		iterationNumber = iterationCounter.getIterationNumber();
		eventHandlers.forEach(eventsManager::removeHandler);
		eventHandlers.clear();

		// Todo: Check and revise this whole part
		if (iterationNumber == lastIteration && endTime/(24*60*60)>1 && endTime%(24*60*60)==0 && lastIteration!=0) {
			// get average soc distribution
			int socDistibutionAtMidnight[] = new int[11];
			AtomicInteger n = new AtomicInteger();
			List<String[]> socs = CSVReaders.readTSV(Paths.get(controlerIO.getIterationFilename(iterationNumber, "soc_histogram_time_profiles.txt")).toString());
			socs.forEach(row -> {
				if (!row[0].equals("time")) {
					double time = Time.parseTime(row[0]);
					if (time > 0.3 * endTime && time % (24*60*60) == 0 && time < 0.8 * endTime) {
						for (int i = 1; i < row.length; i++) {
							socDistibutionAtMidnight[i-1] += Integer.parseInt(row[i]);
						}
						n.getAndIncrement();
					}
				}
			});

			// find relative soc distribution at midnight
			double relativeSocDistribution[] = new double[11];
			double sum = 0;
			for (int value : socDistibutionAtMidnight) {
				sum += value;
			}
			for (int i = 0; i < relativeSocDistribution.length; i++) {
				relativeSocDistribution[i] = socDistibutionAtMidnight[i] / sum;
			}

			// update start socs
			electricFleetSpecification.getVehicleSpecifications().forEach((id, ev) -> {

				double startSoc = 0.0;
				while (startSoc < urbanEVConfig.getDefaultRangeAnxietyThreshold()) {
					double r = random.nextDouble();
					double distributionSum = 0;
					for (int i = 0; i < relativeSocDistribution.length; i++) {
						distributionSum += relativeSocDistribution[i];
						if (r < distributionSum) {
							startSoc = 0.1 * ((double) i - random.nextDouble());
							break;
						}
					}
				}

				double initialSoc = startSoc * ev.getBatteryCapacity();

				ElectricVehicleSpecification electricVehicleSpecification = ImmutableElectricVehicleSpecification.newBuilder()
						.id(id)
						.vehicleType(ev.getVehicleType())
						.chargerTypes(ev.getChargerTypes())
						.initialSoc(initialSoc)
						.batteryCapacity(ev.getBatteryCapacity())
						.build();

				electricFleetSpecification.replaceVehicleSpecification(electricVehicleSpecification);
			});
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
			List<Id<ElectricVehicle>> allowedEvIds = new ArrayList();
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

	private void determineOpportunityChargingStatus(Person person, double homeChargerPower, double workChargerPower, double opportunityChargingShare){
		
		// Remove all existing characteristics to not interfere with initialization runs 
		person.getAttributes().putAttribute("opportunityCharging", "false");

		// If the person owns at least one private charger
		if(homeChargerPower!=0.0||workChargerPower!=0.0)
		{
			boolean assignOpportunityCharging = random.nextDouble()<opportunityChargingShare;
			// If this person statistically is a person who engages in opportunity charging despite owning a private charger
			if(assignOpportunityCharging){
				
				person.getAttributes().putAttribute("opportunityCharging", "true");

				// Make sure that at least one opportunity charging instance exists in all plans of the person
				for(Plan plan:person.getPlans()){
					
					boolean planContainsOtherChargingAct = false;
					ArrayList<Activity> nonHomeNonWorkActs = new ArrayList<>();

					for(PlanElement pe:plan.getPlanElements()){

						if (pe instanceof Activity) {

							Activity act = (Activity) pe;
							String actType = act.getType();
							
							if(!actType.contains("home")&&!actType.contains("work")&&!actType.equals(""))
							{
								// store all activities that are not at home or work to later add a non-home/non-work charging activity if none is present in the plan
								nonHomeNonWorkActs.add(act);

								if(actType.contains(CHARGING_IDENTIFIER)){
									// this plan contains a non-home/non-work charging activity
									planContainsOtherChargingAct=true;
									break;
								}
								
							}
							
						}

					}

					if(!planContainsOtherChargingAct){
						// add a charging activity away from home/work to the plan if possible. Otherwise make sure the person is not marked to participate in opportunity charging
						int n_nonHomeNonWork = nonHomeNonWorkActs.size();
						if(n_nonHomeNonWork>0)
						{
							int randIdx = n_nonHomeNonWork-1>0 ? random.nextInt(n_nonHomeNonWork-1) : 0;
							Activity randomNonHomeNonWorkActivity = nonHomeNonWorkActs.get(randIdx);
							randomNonHomeNonWorkActivity.setType(randomNonHomeNonWorkActivity.getType() + CHARGING_IDENTIFIER);
						}
						else
						{
							// The agent can not take part in opportunity charging, because it only dwells at home and work
							person.getAttributes().putAttribute("opportunityCharging", "false");

							// once a single plan of a person does not allow for opportunity charging, abort
							break;
						}
					}

				}					
			}
		}
	}

}
