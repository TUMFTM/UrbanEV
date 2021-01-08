package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoring;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A class to collect all the data about the scoring of charging activities
 *
 * @author Lennart Adenaw on 09.09.2020
 */

public class ChargingBehaviorScoresCollector {

    private static final ChargingBehaviorScoresCollector OBJ = new ChargingBehaviorScoresCollector();
    private HashMap<ChargingBehaviourScoring.ScoreComponents, ArrayList<Double>> chargingBehaviorScoringComponents = new HashMap<>();
    private HashMap<ChargingBehaviourScoring.ScoreComponents, ArrayList<Id<Person>>> scoringPersons = new HashMap<>();

    public static ChargingBehaviorScoresCollector getInstance(){
        return OBJ;
    }

    private ChargingBehaviorScoresCollector() {
        // Initialize charging component storage
        Arrays.stream(ChargingBehaviourScoring.ScoreComponents.values()).forEach(scoringComponent -> {
            // Initialize history containers
            chargingBehaviorScoringComponents.put(scoringComponent, new ArrayList<Double>());
            scoringPersons.put(scoringComponent, new ArrayList<Id<Person>>());
        });
    }

    public void addScoringComponentValue(ChargingBehaviourScoring.ScoreComponents component, double value)
    {
        chargingBehaviorScoringComponents.get(component).add(value);
    }

    public void addScoringPerson(ChargingBehaviourScoring.ScoreComponents component, Id<Person> personId){

        if(!scoringPersons.get(component).contains(personId))
        {
            scoringPersons.get(component).add(personId);
        }
    }

    public HashMap<ChargingBehaviourScoring.ScoreComponents, ArrayList<Double>> getChargingBehaviorScoringComponents()
    {
        return chargingBehaviorScoringComponents;
    }

    public HashMap<ChargingBehaviourScoring.ScoreComponents, ArrayList<Id<Person>>> getScoringPersons() {
        return scoringPersons;
    }

    public double getNumberOfScoringPersonsForComponent(ChargingBehaviourScoring.ScoreComponents scoreComponent){
        return scoringPersons.get(scoreComponent).size();
    }

    public double getComponentSum(ChargingBehaviourScoring.ScoreComponents scoreComponent){
        return chargingBehaviorScoringComponents.get(scoreComponent).stream().mapToDouble(a -> a).sum();
    }

    public double getComponentMean(ChargingBehaviourScoring.ScoreComponents scoreComponent){
        return chargingBehaviorScoringComponents.get(scoreComponent).stream().mapToDouble(a -> a).sum()/getNumberOfScoringPersonsForComponent(scoreComponent);
    }

    public void reset(){
        chargingBehaviorScoringComponents.keySet().forEach(scoringComponent -> {
            chargingBehaviorScoringComponents.get(scoringComponent).clear();
        });

        scoringPersons.keySet().forEach(scoringComponent -> {
            scoringPersons.get(scoringComponent).clear();
        });
    }

}
