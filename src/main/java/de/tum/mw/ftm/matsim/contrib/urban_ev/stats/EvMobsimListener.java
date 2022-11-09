/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE
 *
 * *********************************************************************** *
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
import de.tum.mw.ftm.matsim.contrib.urban_ev.discharging.DriveDischargingHandler;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoring;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;
import org.matsim.core.utils.misc.Time;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class EvMobsimListener implements MobsimBeforeCleanupListener {

	static private final int SECS_PER_DAY = 24*60*60;

	@Inject
	DriveDischargingHandler driveDischargingHandler;
	@Inject
	ChargerPowerCollector chargerPowerCollector;
	@Inject
	ChargerOccupancyHistoryCollector chargerOccupancyHistoryCollector;
	@Inject
	OutputDirectoryHierarchy controlerIO;
	@Inject
	IterationCounter iterationCounter;
	@Inject
	Network network;

	@Override
	public void notifyMobsimBeforeCleanup(MobsimBeforeCleanupEvent event) {
		
		// CleanUp Last Iteration
		if (iterationCounter.getIterationNumber()>0){
			File dir_to_delete = new File(controlerIO.getIterationPath(iterationCounter.getIterationNumber()-1));
			deleteDir(dir_to_delete);
		}

		//write stats for new iteration
	
		// Retrieve ChargingBehaviorScoresCollector Singleton
		ChargingBehaviorScoresCollector chargingBehaviorScoresCollector = ChargingBehaviorScoresCollector.getInstance();

		// Data Output
		writeChargingBehaviourScoringStats(chargingBehaviorScoresCollector);
		writeChargingStats();
		writeChargerOccupancyStats();
		writeLinkEnergyStats();

		// Reset ChargingBehaviorScoresCollector for next iteration
		chargingBehaviorScoresCollector.reset();
	}

	private void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	private void writeChargingStats(){
		try{
			CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getIterationFilename(iterationCounter.getIterationNumber(), "chargingStats.csv"))), CSVFormat.DEFAULT.withDelimiter(';').
					withHeader(
							"chargerId",
							"xCoord",
							"yCoord",
							"vehicleId",
							"startTime_matsim",
							"startTime_24h",
							"startTime",
							"startDay",
							"endTime_matsim",
							"endTime_24h",
							"endTime",
							"endDay",
							"chargingDuration",
							"unplugTime_matsim",
							"unplugTime_24h",
							"unplugTime",
							"unplugDay",
							"pluggedDuration",
							"chargingRatio",
							"startSoc",
							"startSoc_kWh",
							"endSoc",
							"endSoc_kWh",
							"transmittedEnergy_kWh",
							"walkingDistance"
					));

			for (ChargingLogEntry e : chargerPowerCollector.getLogList()) {
				csvPrinter.printRecord(
						e.getCharger().getId().toString(),
						e.getCharger().getCoord().getX(),
						e.getCharger().getCoord().getY(),
						e.getElectricVehicleId().toString(),
						Time.writeTime(e.getStartTime()),
						Time.writeTime(e.getStartTime()%SECS_PER_DAY),
						Double.toString(e.getStartTime()),
						Integer.toString((int) e.getStartTime()/SECS_PER_DAY+1),
						Time.writeTime(e.getEndTime()),
						Time.writeTime(e.getEndTime()%SECS_PER_DAY),
						Double.toString(e.getEndTime()),
						Integer.toString((int) e.getEndTime()/SECS_PER_DAY+1),
						Double.toString(e.getChargingDuration()),
						Time.writeTime(e.getUnplugTime()),
						Time.writeTime(e.getUnplugTime()%SECS_PER_DAY),
						Double.toString(e.getUnplugTime()),
						Integer.toString((int) e.getUnplugTime()/SECS_PER_DAY+1),
						Double.toString(e.getPluggedDuration()),
						Double.toString(Math.round(e.getChargingRatio()*1000.0)/1000.0),
						Double.toString(Math.round(e.getStartSOC()*1000.0)/1000.0),
						Double.toString(Math.round(EvUnits.J_to_kWh(e.getStartSOC_J()*1000.0)/1000.0)),
						Double.toString(Math.round(e.getEndSOC()*1000.0)/1000.0),
						Double.toString(Math.round(EvUnits.J_to_kWh(e.getEndSOC_J()*1000.0)/1000.0)),
						Double.toString(Math.round(EvUnits.J_to_kWh(e.getTransmittedEnergy_J()*1000.0)/1000.0)),
						Double.toString(Math.round(e.getWalkingDistance()*1000.0)/1000.0)
						);
			}

			csvPrinter.close();
	}
		catch (RuntimeException e){
		e.printStackTrace();
	}
		catch (IOException io){
		io.printStackTrace();
	}
	}

	private void writeLinkEnergyStats(){
		try {
			CSVPrinter csvPrinter2 = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getIterationFilename(iterationCounter.getIterationNumber(), "evConsumptionPerLink.csv"))), CSVFormat.DEFAULT.withDelimiter(';').withHeader("Link", "TotalConsumptionPerKm", "TotalConsumption"));
			for (Map.Entry<Id<Link>, Double> e : driveDischargingHandler.getEnergyConsumptionPerLink().entrySet()) {
				csvPrinter2.printRecord(e.getKey(), (EvUnits.J_to_kWh(e.getValue())) / (network.getLinks()
						.get(e.getKey())
						.getLength() / 1000.0), EvUnits.J_to_kWh(e.getValue()));
			}
			csvPrinter2.close();
		}
		catch (RuntimeException e){
			e.printStackTrace();
		}
		catch (IOException io){
			io.printStackTrace();
		}
	}

	private void writeChargerOccupancyStats(){
		// write charger occupancy plan file
		ChargerOccupancyPlanWriter chargerOccupancyPlanWriter = new ChargerOccupancyPlanWriter(chargerOccupancyHistoryCollector.getOccupancyHistories());
		chargerOccupancyPlanWriter.write(Paths.get(controlerIO.getIterationFilename(iterationCounter.getIterationNumber(), "charger_occupancy_plan.xml")).toString());
	}

	private void writeChargingBehaviourScoringStats(ChargingBehaviorScoresCollector chargingBehaviorScoresCollector){

		int curIteration = iterationCounter.getIterationNumber();

		// Write Scoring Components
		try{

			CSVPrinter csvPrinter;

			if(curIteration==0) {
				csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getOutputPath(), "scoringComponents.csv"), StandardOpenOption.CREATE), CSVFormat.DEFAULT.withDelimiter(';').withHeader(
						"iterationNumber",
						"sumRangeAnxiety",
						"rangeAnxietyScoringPersons",
						"meanRangeAnxiety",
						"sumEmptyBattery",
						"emptyBatteryScoringPersons",
						"meanEmptyBattery",
						"sumWalkingDistance",
						"walkingDistanceScoringPersons",
						"meanWalkingDistance",
						"sumHomeCharging",
						"homeChargingScoringPersons",
						"meanHomeCharging",
						"sumEnergyBalance",
						"energyBalanceScoringPersons",
						"meanEnergyBalance",
						"opportunityChargingScoringPersons",
						"meanOpportunityChargingScore"
				));
			} else {
				csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getOutputPath(), "scoringComponents.csv"), StandardOpenOption.APPEND), CSVFormat.DEFAULT.withDelimiter(';'));
			}


			csvPrinter.printRecord(
					Integer.toString(curIteration),
					Double.toString(chargingBehaviorScoresCollector.getComponentSum(ChargingBehaviourScoring.ScoreComponents.RANGE_ANXIETY)), 							// sum RANGE_ANXIETY
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.RANGE_ANXIETY)), 	// number of persons scoring RANGE_ANXIETY
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.RANGE_ANXIETY)), 							// mean RANGE_ANXIETY
					Double.toString(chargingBehaviorScoresCollector.getComponentSum(ChargingBehaviourScoring.ScoreComponents.EMPTY_BATTERY)), 							// sum EMPTY_BATTERY
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.EMPTY_BATTERY)), 	// number of persons scoring EMPTY_BATTERY
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.EMPTY_BATTERY)), 							// mean EMPTY_BATTERY
					Double.toString(chargingBehaviorScoresCollector.getComponentSum(ChargingBehaviourScoring.ScoreComponents.WALKING_DISTANCE)), 						// sum WALKING_DISTANCE
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.WALKING_DISTANCE)), 	// number of persons scoring WALKING_DISTANCE
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.WALKING_DISTANCE)), 						// mean WALKING_DISTANCE
					Double.toString(chargingBehaviorScoresCollector.getComponentSum(ChargingBehaviourScoring.ScoreComponents.HOME_CHARGING)), 							// sum HOME_CHARGING
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.HOME_CHARGING)), 	// number of persons scoring HOME_CHARGING
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.HOME_CHARGING)), 							// mean HOME_CHARGING
					Double.toString(chargingBehaviorScoresCollector.getComponentSum(ChargingBehaviourScoring.ScoreComponents.ENERGY_BALANCE)), 							// sum ENERGY_BALANCE
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.ENERGY_BALANCE)), 	// number of persons scoring ENERGY_BALANCE
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.ENERGY_BALANCE)), 							// mean ENERGY_BALANCE
					Double.toString(chargingBehaviorScoresCollector.getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents.OPPORTUNITY_CHARGING)), 	// number of persons scoring OPPORTUNITY_CHARGING
					Double.toString(chargingBehaviorScoresCollector.getComponentMean(ChargingBehaviourScoring.ScoreComponents.OPPORTUNITY_CHARGING)) 							// mean OPPORTUNITY_CHARGING
			);


			csvPrinter.close();
		}
		catch (RuntimeException e){
			e.printStackTrace();
		}
		catch (IOException io){
			io.printStackTrace();
		}
	}
}
