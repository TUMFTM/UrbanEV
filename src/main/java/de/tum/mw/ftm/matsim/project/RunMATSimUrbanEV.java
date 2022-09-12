package de.tum.mw.ftm.matsim.project;

import de.tum.mw.ftm.matsim.contrib.urban_ev.EvModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.VehicleChargingHandler;
import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.planning.ChangeChargingBehaviour;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoring;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringParameters;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;

import java.io.IOException;

/**
 * Prepares and runs simulation
 * Added scoring and replanning strategies
 */

public class RunMATSimUrbanEV {
	private static final Logger log = Logger.getLogger(RunMATSimUrbanEV.class);

	public RunMATSimUrbanEV() {
	}

	public static void main(String[] args) throws IOException {

		String configPath = "";
		int initIterations = 0;
		int initIterationRepetitions = 0;

		if (args != null && args.length == 3) {
			configPath = args[0];
			initIterations = Integer.parseInt(args[1]);
			initIterationRepetitions = Integer.parseInt(args[2]);
		} else if (args != null && args.length == 2){
			configPath = args[0];
			initIterations = Integer.parseInt(args[1]);
			initIterationRepetitions = 0;
		} else if (args != null && args.length == 1){
			configPath = args[0];
			initIterations = 0;
			initIterationRepetitions = 0;
		}
		else{
			System.out.println("Config file missing. Please supply a config file path as a program argument.");
			throw new IOException("Could not start simulation. Config file missing.");
		}

		log.info("Config file path: " + configPath);
		log.info("Number of iterations to initialize SOC distribution: " + initIterations);

		ConfigGroup[] configGroups = new ConfigGroup[]{new EvConfigGroup(), new UrbanEVConfigGroup()};
		Config config = ConfigUtils.loadConfig(configPath, configGroups);

		if (initIterations > 0) {
			
			Config initConfig = ConfigUtils.loadConfig(configPath, configGroups);
			initConfig.controler().setLastIteration(initIterations);
			String baseOutputDirectory = initConfig.controler().getOutputDirectory();

			// If initialization iterations are needed
			for(int repetition = 0; repetition <= initIterationRepetitions; repetition++)
			{
				String outputDirectory = baseOutputDirectory + "/init" + Integer.toString(repetition);
				initConfig.controler().setOutputDirectory(outputDirectory);
				loadConfigAndRun(initConfig);

				// use new vehicles file for next initialization
				EvConfigGroup evConfigGroup = (EvConfigGroup) initConfig.getModules().get("ev");
				evConfigGroup.setVehiclesFile("output/init" + Integer.toString(repetition) + "/output_evehicles.xml");
				
			}

			// use new vehicles file for training
			EvConfigGroup evConfigGroup = (EvConfigGroup) initConfig.getModules().get("ev");
			evConfigGroup.setVehiclesFile("output/init" + Integer.toString(initIterationRepetitions) + "/output_evehicles.xml");
			config.controler().setOutputDirectory(baseOutputDirectory + "/train");
		}

		// Start final run
		loadConfigAndRun(config);

	}

	private static void loadConfigAndRun(Config config) {

		final Scenario scenario = ScenarioUtils.loadScenario(config);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new EvModule());
		controler.addOverridingModule(new AbstractModule() {
			public void install() {
				this.installQSimModule(new AbstractQSimModule() {
					protected void configureQSim() {
						this.bind(VehicleChargingHandler.class).asEagerSingleton();
					}
				});
			}
		});

		controler.configureQSimComponents((components) -> {
			components.addNamedComponent("EV_COMPONENT");
		});

		// add plan strategies
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addPlanStrategyBinding("ChangeChargingBehaviour").toProvider(ChangeChargingBehaviour.class);
			}
		});

		// replace scoring function
		controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
			@Override
			public ScoringFunction createNewScoringFunction(Person person) {
				ChargingBehaviourScoringParameters chargingBehaviourScoringParameters = new ChargingBehaviourScoringParameters.Builder(scenario).build();
				SumScoringFunction sumScoringFunction = new SumScoringFunction();
				sumScoringFunction.addScoringFunction(new ChargingBehaviourScoring(chargingBehaviourScoringParameters, person));
				return sumScoringFunction;
			}
		});

		// At first, assign every person to a default (nonCriticalSOC) replanning subpopulation. Later on, persons, whose vehicles reach an SOC<=0 will be added to a special replanning subpopulation that will be replanned in any case
		Population population = controler.getScenario().getPopulation();
		population.getPersons().entrySet().forEach(entry->{entry.getValue().getAttributes().putAttribute("subpopulation", "nonCriticalSOC");});

		controler.run();
	}
}
