package de.tum.mw.ftm.matsim.project;

import de.tum.mw.ftm.matsim.contrib.urban_ev.EvModule;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.VehicleChargingHandler;
import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.planning.ChangeChargingBehaviour;
import de.tum.mw.ftm.matsim.contrib.urban_ev.planning.FineTuningReplanner;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoring;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.FineTuningChargingBehaviorScoring;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringParameters;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
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


		// Read program args
		String configPath = "";

		// Check, if MATSim runs inside container and determine config file path
        String inputPath = Environment.getMatsimInputPath();
        String outputPath = Environment.getMatsimOutputPath();
        String matsimVersion = Environment.getMatsimVersion();
		
		if (inputPath != null) {

			log.info("Starting MATSim in Docker container." + matsimVersion);
			configPath = String.format("%s/%s", inputPath, "config.xml");
		}
        else 
		{
			// Not running in a docker container. Retrieve config file from program args
			if (args != null && args.length == 1){
				configPath = args[0];
			}
			else{
				System.out.println("Config file missing. Please supply a config file path as a program argument.");
				throw new IOException("Could not start simulation. Config file missing.");
			}

		}

		// Prepare configs and output path
		ConfigGroup[] configGroups = new ConfigGroup[]{new EvConfigGroup(), new UrbanEVConfigGroup()};
		Config config = ConfigUtils.loadConfig(configPath, configGroups);
		Config initConfig = ConfigUtils.loadConfig(configPath, configGroups);
		UrbanEVConfigGroup urbanEVConfigGroup = (UrbanEVConfigGroup) config.getModules().get("urban_ev");

		if (outputPath != null){
			// If this is running within a docker container...
			// .. correct the output directory accordingly
			config.controler().setOutputDirectory(outputPath);
			initConfig.controler().setOutputDirectory(outputPath);
		}

		// Determine number of initializations
		int initIterations = urbanEVConfigGroup.getInitializationIterations();
		int initRepetitions = urbanEVConfigGroup.getInitializationRepetitions();

		// Inform user
		log.info("Config file path: " + configPath);
		log.info("Number of iterations to initialize SOC distribution: " + initIterations);
		log.info("Repetitions of the initialization procedure: " + initRepetitions);

		if (initIterations > 0) {
			
			// Configure initialization
			initConfig.controler().setLastIteration(initIterations);
			String baseOutputDirectory = initConfig.controler().getOutputDirectory();
			StrategyConfigGroup strategyConfigGroup = (StrategyConfigGroup) initConfig.getModules().get("strategy");
			
			// Make sure that innovation is not disabled during initialization
			strategyConfigGroup.setFractionOfIterationsToDisableInnovation(1.1);
			
			// If initialization iterations are needed
			for(int repetition = 0; repetition <= initRepetitions; repetition++)
			{
				// Construct init specific output dir
				String outputDirectory = baseOutputDirectory + "/init" + Integer.toString(repetition);
				initConfig.controler().setOutputDirectory(outputDirectory);
				
				// Run simulation
				loadConfigAndRun(initConfig);

				// use new vehicles file for next initialization
				EvConfigGroup evConfigGroup = (EvConfigGroup) initConfig.getModules().get("ev");
				PlansConfigGroup plansConfigGroup = (PlansConfigGroup) initConfig.getModules().get("plans");

				// use new vehicles file and plans for training
				evConfigGroup.setVehiclesFile("output/init" + Integer.toString(repetition) + "/output_evehicles.xml");
				plansConfigGroup.setInputFile("output/init" + Integer.toString(repetition) + "/output_plans.xml.gz");
			}

			// use new vehicles file and plans for training
			EvConfigGroup evConfigGroup = (EvConfigGroup) config.getModules().get("ev");
			PlansConfigGroup plansConfigGroup = (PlansConfigGroup) config.getModules().get("plans");
			evConfigGroup.setVehiclesFile("output/init" + Integer.toString(initRepetitions) + "/output_evehicles.xml");
			plansConfigGroup.setInputFile("output/init" + Integer.toString(initRepetitions) + "/output_plans.xml.gz");

			// Set output directory
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
				addPlanStrategyBinding("FineTuningPlanner").toProvider(FineTuningReplanner.class);
			}
		});

		// replace scoring function
		controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
			@Override
			public ScoringFunction createNewScoringFunction(Person person) {
				ChargingBehaviourScoringParameters chargingBehaviourScoringParameters = new ChargingBehaviourScoringParameters.Builder(scenario).build();
				SumScoringFunction sumScoringFunction = new SumScoringFunction();
				
				//sumScoringFunction.addScoringFunction(new ChargingBehaviourScoring(chargingBehaviourScoringParameters, person));
				sumScoringFunction.addScoringFunction(new FineTuningChargingBehaviorScoring(chargingBehaviourScoringParameters, person));

				return sumScoringFunction;
			}
		});

		long start = System.currentTimeMillis();

		controler.run();
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		System.out.println(timeElapsed);
	}
}
