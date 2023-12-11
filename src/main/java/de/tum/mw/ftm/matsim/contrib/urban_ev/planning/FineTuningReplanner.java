package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.selectors.ExpBetaPlanSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class FineTuningReplanner implements Provider<PlanStrategy> {

    private Scenario scenario;

    @Inject
    public FineTuningReplanner(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public PlanStrategy get() {
        double logitScaleFactor = 1.0;
        PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new ExpBetaPlanSelector<>(logitScaleFactor));
        
        FineTuningPlannerStrategy fineTuningPlannerStrategy = new FineTuningPlannerStrategy(scenario);

        builder.addStrategyModule(fineTuningPlannerStrategy);

        return builder.build();
    }

}
