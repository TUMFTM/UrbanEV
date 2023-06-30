package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ScoreComponents;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.util.ArrayList;

/**
 * A class to collect all the data about the scoring of charging activities
 *
 * @author Lennart Adenaw on 09.09.2020
 */

public class ChargingBehaviorScoresCollector {

    private static final ChargingBehaviorScoresCollector OBJ = new ChargingBehaviorScoresCollector();

    private ArrayList<ChargingScoreLogEntry> scoringHistory = new ArrayList<>();

    public static ChargingBehaviorScoresCollector getInstance(){
        return OBJ;
    }

    private ChargingBehaviorScoresCollector() {
    }

    public void collect(
        Id<Person> personId,
        double time,
        ScoreComponents scoreComponent,
        double value
        )
    {
        scoringHistory.add(new ChargingScoreLogEntry(personId, time, scoreComponent, value));
    }

    public ArrayList<ChargingScoreLogEntry> getScoringHistory()
    {
        return scoringHistory;
    }

    public void reset(){
        scoringHistory.clear();
    }

}
