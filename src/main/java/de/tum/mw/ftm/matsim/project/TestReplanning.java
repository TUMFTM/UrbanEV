package de.tum.mw.ftm.matsim.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.ev.EvConfigGroup;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.NetworkConfigGroup;

import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PersonUtils;

public class TestReplanning {

    public static void writeStringToFile(String str, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(str);
        writer.close();
    }

    public static void createDirsIfNotExists(String path)
    {
        // Create missing dirs
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
    }

    public static void main(String[] args) {
        
        // Options
        int replanningIterations = 50; // number of replanning iterations
        String sourceDir = args[0];
        String replanningOutputDirectory = sourceDir+"replanning_tests/"; // directory to write output population files
        String chargersFileName = "chargers.xml";
        String evehiclesFileName = "evehicles.xml";
        String vehicleTypesFileName = "vehicletypes_one_type.xml";
        String networkFileName = "munich-v1.0-network.xml.gz";

        // Create missing dirs
        createDirsIfNotExists(replanningOutputDirectory);

        // Parse configs
        ConfigGroup[] configGroups = new ConfigGroup[]{new EvConfigGroup(), new UrbanEVConfigGroup()};
		Config config = ConfigUtils.loadConfig(sourceDir+"config.xml", configGroups);
        PlansConfigGroup plansConfigGroup = (PlansConfigGroup) config.getModules().get("plans");
        ControlerConfigGroup controlerConfigGroup = (ControlerConfigGroup) config.getModules().get("controler");
        UrbanEVConfigGroup urbanEVConfigGroup = (UrbanEVConfigGroup) config.getModules().get("urban_ev");
        EvConfigGroup evConfigGroup = (EvConfigGroup) config.getModules().get("ev");
        NetworkConfigGroup networkConfigGroup = (NetworkConfigGroup) config.getModules().get("network");

        // Load scenario and origin population
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler(scenario);
        Population population = controler.getScenario().getPopulation();

        // Set config paths and basic options
        evConfigGroup.setChargersFile(sourceDir + chargersFileName);
        evConfigGroup.setVehiclesFile(sourceDir + evehiclesFileName);
        urbanEVConfigGroup.setVehicleTypesFile(sourceDir + vehicleTypesFileName);
        networkConfigGroup.setInputFile(sourceDir + networkFileName);
        controlerConfigGroup.setFirstIteration(0);
        controlerConfigGroup.setLastIteration(0);

        // Set everyone to critical / non-critical
        //population.getPersons().values().forEach(person->{PersonUtils.setNonCritical(person);});
        population.getPersons().values().forEach(person->{PersonUtils.setCritical(person);});
        
        // Perform replanning iterations
        //SimpleDebugReplanner replanModule = new SimpleDebugReplanner(scenario);
        DebugReplanner replanModule = new DebugReplanner(scenario);
        
        // Util
        Random random = new Random();

        // Replanning
        for (int i = 1; i <= replanningIterations; i++) {

            // Create a folder for each iteration step
            String replanningSubdir = replanningOutputDirectory+i+"/";
            createDirsIfNotExists(replanningSubdir);

            for (Person person : population.getPersons().values()){
    
                //double critical_prop = PersonUtils.hasPrivateCharger(person) ? 0 : 0.05*(1-((i-1)/replanningIterations));
                double critical_prop = PersonUtils.hasPrivateCharger(person) ? 0 : 1;

                if(random.nextDouble()<=critical_prop && i<25)
                {
                    PersonUtils.setCritical(person);
                }
                else
                {
                    PersonUtils.setNonCritical(person);
                }

                // Copy parent plan and replan
                Plan replan_plan =  person.createCopyOfSelectedPlanAndMakeSelected();
                replanModule.handlePlan(replan_plan);
                replanModule.finishReplanning();
                    
                // Delete all non-selected plans to not bloat population files
                Plan selectedPlan = person.getSelectedPlan();
                person.getPlans().stream().filter(p -> p!=selectedPlan).collect(Collectors.toList()).forEach(p -> person.removePlan(p));
            }

            // Write population file for the current iteration
            String populationOutputFileName = replanningSubdir + "population.xml";
            PopulationWriter populationWriter = new PopulationWriter(population);
            populationWriter.write(populationOutputFileName);

            plansConfigGroup.setInputFile(populationOutputFileName);

            // Write config 
            String configOutputFile = replanningSubdir+"config.xml";
            String outputDirectory = replanningSubdir+"output";

            controlerConfigGroup.setOutputDirectory(outputDirectory);
            ConfigUtils.writeConfig(config, configOutputFile);
        }

        System.out.println("Replanning complete.");

    }
}
