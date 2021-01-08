package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEvent;
import de.tum.mw.ftm.matsim.contrib.urban_ev.scoring.ChargingBehaviourScoringEventHandler;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChangeChargingBehaviourModule implements PlanStrategyModule, ChargingBehaviourScoringEventHandler {

    private static final String CHARGING_IDENTIFIER = " charging";
    private static final String CHARGING_FAILED_IDENTIFIER = " charging failed";

    private Random random = new Random();
    private Scenario scenario;
    private Network network;
    private Population population;
    private UrbanEVConfigGroup evCfg;
    private int maxNumberSimultaneousPlanChanges;
    private Double timeAdjustmentProbability;
    private int maxTimeFlexibility;

    ChangeChargingBehaviourModule(Scenario scenario) {
        this.scenario = scenario;
        this.network = this.scenario.getNetwork();
        this.population = this.scenario.getPopulation();
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
        this.maxNumberSimultaneousPlanChanges = evCfg.getMaxNumberSimultaneousPlanChanges();
        this.timeAdjustmentProbability = evCfg.getTimeAdjustmentProbability();
        this.maxTimeFlexibility = evCfg.getMaxTimeFlexibility();
    }

    @Override
    public void finishReplanning() {
    }

    @Override
    public void handlePlan(Plan plan) {
        int numberOfChanges = 1 + random.nextInt(maxNumberSimultaneousPlanChanges);


        for (int c = 0; c < numberOfChanges; c++ ) {
            List<PlanElement> planElements = plan.getPlanElements();
            int max = planElements.size();

            // get activity ids of activities with and without charging
            ArrayList<Integer> successfulChargingActIds = new ArrayList<>();
            ArrayList<Integer> failedChargingActIds = new ArrayList<>();
            ArrayList<Integer> noChargingActIds = new ArrayList<>();

            // loop starts at 2 because car should never be charging at start of simulation
            for (int i = 2; i < max; i++) {
                PlanElement pe = planElements.get(i);
                if (pe instanceof Activity) {
                    Activity act = (Activity) pe;
                    if (act.getType().endsWith(CHARGING_IDENTIFIER)) {
                        successfulChargingActIds.add(i);
                    } else if (act.getType().endsWith(CHARGING_FAILED_IDENTIFIER)) {
                        // remove and possibly change activity or time
                        failedChargingActIds.add(i);
                        act.setType(act.getType().replace(CHARGING_FAILED_IDENTIFIER, ""));
                    } else {
                        noChargingActIds.add(i);
                    }
                }
            }

            // with some probability try changing start time of failed charging activity (end time of previous activity)
            if (failedChargingActIds.size() > 0 && random.nextDouble() < timeAdjustmentProbability) {
                changeChargingActivityTime(planElements, failedChargingActIds);
            } else {
                // number of charging attempts that were successful
                int nSuccessfulCharging = successfulChargingActIds.size();
                // number of failed charging attempts
                int nFailedCharging = failedChargingActIds.size();
                // number of activities without charging attempt
                int nNoCharging = noChargingActIds.size();
                // sum of activities with successful attempts and activities without charging attempts
                int nTotal = nSuccessfulCharging + nNoCharging;

                // assign weights to different strategies based on successful and failed attempts
                double wChangeFailed = (nFailedCharging == 0 || nNoCharging == 0) ? 0 : 2;
                double wChangeSuccessful = (nSuccessfulCharging == 0 || nNoCharging == 0) ? 0 : 1;
                double wAdd = (double) nNoCharging / nTotal;
                double wRemove = (double) nSuccessfulCharging / nTotal;

                // decide which strategy to use: add/remove/change
                // Todo: Beautify and simplify this!
                double sumOfWeights = wAdd + wRemove + wChangeSuccessful + wChangeFailed;
                double w = sumOfWeights * random.nextDouble();
                w -= wChangeFailed;
                if (w <= 0) {
                    changeChargingActivity(planElements, failedChargingActIds, noChargingActIds);
                } else {
                    w -= wChangeSuccessful;
                    if (w <= 0) {
                        changeChargingActivity(planElements, successfulChargingActIds, noChargingActIds);
                    } else {
                        w -= wAdd;
                        if (w <= 0) {
                            addChargingActivity(planElements, noChargingActIds);
                        } else {
                            removeChargingActivity(planElements, successfulChargingActIds);
                        }
                    }
                }
            }
        }
    }

    private void changeChargingActivityTime(List<PlanElement> planElements, ArrayList<Integer> failedChargingActIds) {
        // select random failed charging activity and try changing end time of previous activity
        int n = failedChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = failedChargingActIds.get(randInt);
            if (actId >= 2) {
                Activity selectedActivity = (Activity) planElements.get(actId);
                Leg previousLeg = (Leg) planElements.get(actId - 1);
                Activity previousActivity = (Activity) planElements.get(actId - 2);
                double timeDifference = random.nextDouble() * maxTimeFlexibility; // 0 to 10 minutes
                double earliestPossibleTime = 0;
                if (actId >= 4) {
                    earliestPossibleTime = ((Activity) planElements.get(actId - 4)).getEndTime().seconds();
                }
                if (previousActivity.getEndTime().seconds() - timeDifference > earliestPossibleTime) {
                    previousActivity.setEndTime(previousActivity.getEndTime().seconds() - timeDifference);
                    previousLeg.setDepartureTime(previousLeg.getDepartureTime().seconds() - timeDifference);
                    selectedActivity.setType(selectedActivity.getType() + CHARGING_IDENTIFIER);
                }
            }
        }
    }

    private void addChargingActivity(List<PlanElement> planElements, ArrayList<Integer> noChargingActIds) {
        // select random activity without charging and change to activity with charging
        int n = noChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = noChargingActIds.get(randInt);
            Activity selectedActivity = (Activity) planElements.get(actId);
            selectedActivity.setType(selectedActivity.getType() + CHARGING_IDENTIFIER);
        }
    }

    private void removeChargingActivity(List<PlanElement> planElements, ArrayList<Integer> successfulChargingActIds) {
        // select random activity with charging and change to activity without charging
        int n = successfulChargingActIds.size();
        if (n > 0) {
            int randInt = random.nextInt(n);
            int actId = successfulChargingActIds.get(randInt);
            Activity selectedActivity = (Activity) planElements.get(actId);
            selectedActivity.setType(selectedActivity.getType().replace(CHARGING_IDENTIFIER, ""));
        }
    }

    private void changeChargingActivity(List<PlanElement> planElements,
                                ArrayList<Integer> chargingActIds,
                                ArrayList<Integer> noChargingActIds) {
        // select random activity with charging and change to activity without charging
        int chargingActId = chargingActIds.get(random.nextInt(chargingActIds.size()));
        Activity selectedActivity = (Activity) planElements.get(chargingActId);
        selectedActivity.setType(selectedActivity.getType().replace(CHARGING_IDENTIFIER, ""));

        // select activity without charging close to original activity using gaussian distribution and change to activity with charging
        double gaussId = 0.0;
        while (gaussId < 1 || gaussId > planElements.size()) {
            gaussId = 5 * random.nextGaussian() + chargingActId;
        }
        double dMin = planElements.size();
        int closestNoChargingActId = 0;
        for (int noChargingActId : noChargingActIds) {
            double d = Math.abs(gaussId - noChargingActId);
            if (d < dMin) {
                dMin = d;
                closestNoChargingActId = noChargingActId;
            }
        }
        Activity closestNoChargingActivity = (Activity) planElements.get(closestNoChargingActId);
        closestNoChargingActivity.setType(closestNoChargingActivity.getType() + CHARGING_IDENTIFIER);
    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    @Override
    public void handleEvent(ChargingBehaviourScoringEvent event) {
        double startSoc = event.getStartSoc();
        double soc = event.getSoc();
        boolean isLastAct = event.getActivityType().contains("end");
        // Make sure agents with a criticalSOC or with a bad end-soc get replanned for sure
        if (soc == 0 || (isLastAct && Math.abs(soc - startSoc) > random.nextDouble())) {
            // Add all critical agents to the criticalSOC subpopulation such that they get replanned
            population.getPersons().get(event.getPersonId()).getAttributes().putAttribute("subpopulation", "criticalSOC");
        }
        else{
            // Remove all non-critical agents from the criticalSOC subpopulation such that they get replanned with the default probability
            population.getPersons().get(event.getPersonId()).getAttributes().putAttribute("subpopulation", "nonCriticalSOC");
        }
    }

    @Override
    public void reset(int iteration) {
    }
}
