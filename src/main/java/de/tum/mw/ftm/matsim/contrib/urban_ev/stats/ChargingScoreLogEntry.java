package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ScoreComponents;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public class ChargingScoreLogEntry {
    private final Id<Person> personId;
    private final double time; 
    private final ScoreComponents scoreComponent;
    private final double value;

    public ChargingScoreLogEntry(
        Id<Person> personId,
        double time,
        ScoreComponents scoreComponent,
        double value){

            this.personId = personId;
            this.time = time; 
            this.scoreComponent = scoreComponent;
            this.value = value;

    }

    public Id<Person> getPersonId()
    {
        return personId;
    }

    public double getTime()
    {
        return time;
    }

    public ScoreComponents getScoreComponent()
    {
        return scoreComponent;
    }

    public double getValue()
    {
        return value;
    }
}
