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
import org.matsim.core.population.PopulationUtils;
import org.matsim.contrib.util.distance.DistanceUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
	private Random random = new Random();
	private static final String CHARGING_IDENTIFIER = " charging";

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
		UrbanEVConfigGroup urbanEVConfigGroup = (UrbanEVConfigGroup) config.getModules().get("urban_ev");
		double parkingSearchRadius = urbanEVConfigGroup.getParkingSearchRadius();
		double opportunityChargingShare = urbanEVConfigGroup.getOpportunityChargingShare();

        population.getPersons().forEach((personId, person) -> {

			// Make sure all activities end shortly before the simulation ends
			for(Plan plan: person.getPlans()){
				ArrayList<Activity> activities = getActivities(plan);
				if(activities.size()!=0)
				{
					Activity lastActinPlanEndingAfterSim = null;
					List<Integer> delActivitiesIdx = new ArrayList<Integer>();
					// Find the first activity that ends after the end of the simulation if there is any
					for(int i = 0; i<plan.getPlanElements().size(); i++)
					{
						PlanElement pe = plan.getPlanElements().get(i);
						if(pe instanceof Activity)
						{
							Activity act = (Activity) pe;
							if(Objects.isNull(lastActinPlanEndingAfterSim) && act.getEndTime().isDefined() && act.getEndTime().seconds()>endTime)
							{
								lastActinPlanEndingAfterSim = act;
							}
							else if(!Objects.isNull(lastActinPlanEndingAfterSim) && act.getEndTime().isDefined() && act.getEndTime().seconds()>endTime)
							{
								// There are even more activities that end after the simulation time
								if(act.getEndTime().seconds()>lastActinPlanEndingAfterSim.getEndTime().seconds())
								{
									// Remove if the activity ends even later
									delActivitiesIdx.add(i);
								}

							}
							else if(!act.getEndTime().isDefined())
							{
								// Remove if the activity has no end time
								delActivitiesIdx.add(i);
							}
						}

					}
					Collections.sort(delActivitiesIdx, Collections.reverseOrder());  
					// Delete all activities that end even later than the expected last activity
					for(int i = 0; i<delActivitiesIdx.size(); i++)
					{
						PopulationUtils.removeActivity(plan, delActivitiesIdx.get(i));
					}

					if(!Objects.isNull(lastActinPlanEndingAfterSim))
					{
						// If there is at least one activity ending after the simulation time 
						// Make it end right before the end of the simulation and introduce an aritifical activity in its place 
						// that serves to trigger unplugging events for all chargers at simulation end time
						lastActinPlanEndingAfterSim.setEndTime(endTime-1);
						Activity newLastActinPlan = PopulationUtils.createActivityFromCoord(lastActinPlanEndingAfterSim.getType(),lastActinPlanEndingAfterSim.getCoord());
						newLastActinPlan.setEndTime(endTime);
						if(!newLastActinPlan.getType().contains("end"))
						{
							newLastActinPlan.setType(newLastActinPlan.getType().concat(" end"));
						}
						// make sure charging is never associated with the end activity 
						newLastActinPlan.setType(newLastActinPlan.getType().replace(" charging", ""));
						lastActinPlanEndingAfterSim.setType(lastActinPlanEndingAfterSim.getType().replace(" end", ""));
						plan.addActivity(newLastActinPlan);
					}
				}
			}

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
			
        });

		// Determine persons that could potentially engage in opportunity charging at public chargers		
		List<Person> possibleOpportunityChargingAgents = new ArrayList<>();

		population.getPersons().forEach((personId, person) -> {
			
			// Remove all existing characteristics to not interfere with initialization runs 
			person.getAttributes().putAttribute("opportunityCharging", "false");

			// Determine if the person would be suited for opportunity charging
			if(determineOpportunityChargingPossibility(person, parkingSearchRadius, chargingInfrastructureSpecification)) possibleOpportunityChargingAgents.add(person);
		});

		// Determine population to engage in opportunity charging
		int n_opportunity_charging_target = (int)(population.getPersons().size()*opportunityChargingShare);
		List<Person> opportunityChargingAgents = new ArrayList<>();

		if(n_opportunity_charging_target>=possibleOpportunityChargingAgents.size())
		{
			// If there are not enough agents that can potentially engage in opportunity charging, take what's there
			opportunityChargingAgents = possibleOpportunityChargingAgents;
		}
		else{
			// If there are more agents with the desired characteristics than necessary, randomly take as much as needed
			for(int i = 0; i<n_opportunity_charging_target; i++)
			{
				opportunityChargingAgents.add(possibleOpportunityChargingAgents.remove(random.nextInt(possibleOpportunityChargingAgents.size())));
			}
		}

		// Flag persons for opportunity charging
		opportunityChargingAgents.forEach(person -> {prepareOpportunityChargingPerson(person, parkingSearchRadius);});

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

	private boolean determineOpportunityChargingPossibility(Person person, double parkingSearchRadius, ChargingInfrastructureSpecification chargingInfrastructure)
	{
		
		double homeChargerPower = person.getAttributes().getAttribute("homeChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("homeChargerPower").toString()) : 0.0;
		double workChargerPower = person.getAttributes().getAttribute("workChargerPower") != null ? Double.parseDouble(person.getAttributes().getAttribute("workChargerPower").toString()) : 0.0;

		// If the person owns at least one private charger
		if(homeChargerPower==0.0&&workChargerPower==0.0)
		{
			// If the agent does not own a private charger it is not a candidate for opportunity charging
			return false;
		}

		// Check if at least one suited opportunity charging instance exists in all plans of the person
		for(Plan plan:person.getPlans()){
			
			boolean planContainsOtherActsWithPublicChargingOpportunities = false;

			for(PlanElement pe:plan.getPlanElements()){

				if (pe instanceof Activity) {

					Activity act = (Activity) pe;
					String actType = act.getType();
					Coord stopCoord = act.getCoord();

					if((!actType.contains("home")&&!actType.contains("work")&&!actType.equals(""))||actType.contains("work_related"))
					{
						if(availablePublicChargers(stopCoord, parkingSearchRadius)){
							planContainsOtherActsWithPublicChargingOpportunities = true;
							break;
						}
					}
					
				}

			}

			if(!planContainsOtherActsWithPublicChargingOpportunities)
			{
				return false;
			}

		}			
		
		return true;
	}

	private boolean availablePublicChargers(Coord stopCoord, double parkingSearchRadius){

		List<ChargerSpecification> filteredChargers = new ArrayList<>();

		chargingInfrastructureSpecification.getChargerSpecifications().values().forEach(charger -> {
			// filter out private chargers
			if (charger.getAllowedVehicles().isEmpty()) {
				// filter out chargers that are out of range
				if (DistanceUtils.calculateDistance(stopCoord, charger.getCoord()) < parkingSearchRadius) {
					filteredChargers.add(charger);
				}
			}
		});
		if(!filteredChargers.isEmpty()){
			return true;
		}
		else{
			return false;
		}
	}

	private void prepareOpportunityChargingPerson(Person person, double parkingSearchRadius){
					
		person.getAttributes().putAttribute("opportunityCharging", "true");

		// Make sure that at least one opportunity charging instance exists in all plans of the person
		for(Plan plan:person.getPlans()){
			
			boolean planContainsSuitableOtherChargingAct = false;
			ArrayList<Activity> suitableNonHomeNonWorkActs = new ArrayList<>();

			for(PlanElement pe:plan.getPlanElements()){

				if (pe instanceof Activity) {

					Activity act = (Activity) pe;
					String actType = act.getType();
					
					if(
						((!actType.contains("home")&&!actType.contains("work")&&!actType.equals(""))||actType.contains("work_related"))
						&&
						!actType.contains("end")
						&&
						availablePublicChargers(act.getCoord(), parkingSearchRadius)						
						)
					{
						// store all activities that are not at home or work to later add a non-home/non-work charging activity if none is present in the plan
						suitableNonHomeNonWorkActs.add(act);

						if(actType.contains(CHARGING_IDENTIFIER)){
							// this plan contains a non-home/non-work charging activity
							planContainsSuitableOtherChargingAct=true;
							break;
						}
						
					}
					
				}

			}

			if(!planContainsSuitableOtherChargingAct){
				// add a charging activity away from home/work to the plan if possible. Otherwise make sure the person is not marked to participate in opportunity charging
				int n_nonHomeNonWork = suitableNonHomeNonWorkActs.size();
				if(n_nonHomeNonWork>0)
				{
					Activity randomNonHomeNonWorkActivity = suitableNonHomeNonWorkActs.get(random.nextInt(n_nonHomeNonWork));
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

	private ArrayList<Activity> getActivities(Plan plan){

		ArrayList<Activity> activities = new ArrayList<>();

		for(PlanElement pe:plan.getPlanElements()){

			if (pe instanceof Activity) {

				activities.add((Activity) pe);
				
			}
		
		}
		return activities;
	}

}
