package de.tum.mw.ftm.matsim.contrib.urban_ev.planning;

import de.tum.mw.ftm.matsim.contrib.urban_ev.config.UrbanEVConfigGroup;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PersonUtils;
import de.tum.mw.ftm.matsim.contrib.urban_ev.utils.PlanUtils;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.gbl.MatsimRandom;
import de.tum.mw.ftm.matsim.contrib.urban_ev.routing.EvNetworkRoutingProvider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.routing.EvNetworkRoutingModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import javax.inject.Provider;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.config.Config;
import org.matsim.core.population.PopulationUtils;

public class ChangeChargingBehaviourModule implements PlanStrategyModule {

    private UrbanEVConfigGroup evCfg;
    private final Provider<TripRouter> tripRouterProvider;
    private ActivityFacilities facilities;
    // private final ActivityFacilities facilities;

    private enum ChargingStrategyChange {
        REMOVEWORK_ADDHOME,
        REMOVEHOME_ADDWORK,
        REMOVEWORK_ADDWORK,
        REMOVEHOME_ADDHOME,
        ADDHOME,
        ADDWORK,
        ADDOTHER,
        REMOVEHOME,
        REMOVEWORK,
        REMOVEOTHER,
        REMOVEOTHER_ADDOTHER
    }
      

    ChangeChargingBehaviourModule(Scenario scenario,Provider<TripRouter> tripRouterProvider) {
        this.evCfg = (UrbanEVConfigGroup) scenario.getConfig().getModules().get("urban_ev");
        this.tripRouterProvider = tripRouterProvider;
        this.facilities = scenario.getActivityFacilities();
        //EvNetworkRoutingProvider routerProvider = new EvNetworkRoutingProvider(TransportMode.car);
        //RoutingModule router = routerProvider.get(this.evCfg);
    }

    @Override
    public void finishReplanning() {
    }

    @Override
    public void handlePlan(Plan plan) {
        
        // retrieve relevant person characteristics
        Person person = plan.getPerson();
        TripRouter tripRouter = this.tripRouterProvider.get(); 
        //tripRouter.getRoutingModule("dc_charging").calcRoute(null, null, 0, person);
        // derived person characteristics
        boolean personHasHomeCharger = PersonUtils.hasHomeCharger(person);
        boolean personHasWorkCharger = PersonUtils.hasWorkCharger(person);
        boolean personHasPrivateCharger = PersonUtils.hasPrivateCharger(person);
        boolean personCriticalSOC = PersonUtils.isCritical(person);
        
        
        //plan = removefastcharging(plan);

        List<Leg> legs = plan.getPlanElements().stream().filter(f -> f instanceof Leg).map(pe -> (Leg) pe).collect(Collectors.toList());
        // person plan analysis
        List<Activity> activities = PlanUtils.getActivities(plan);
        List<Activity> nonStartOrEndActs = PlanUtils.getNonIniEndActivities(activities);

        List<Activity> homeActs = PlanUtils.getHomeActivities(nonStartOrEndActs);
        List<Activity> workActs = PlanUtils.getWorkActivities(nonStartOrEndActs);
        List<Activity> otherActs = nonStartOrEndActs.stream().filter(a -> !homeActs.contains(a) & !workActs.contains(a)).collect(Collectors.toList());

        // Apply plan changes

        
        // first, analyze current charging behavior
        List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);
        List<Activity> noChargingActs = PlanUtils.getNonChargingActivities(nonStartOrEndActs);

        List<Activity> failedChargingActs = PlanUtils.getFailedChargingActivities(allChargingActs);
        List<Activity> successfulChargingActs = PlanUtils.getSuccessfulChargingActivities(allChargingActs);

        List<Activity> homeActsWithCharging = PlanUtils.getChargingActivities(homeActs);
        List<Activity> workActsWithCharging = PlanUtils.getChargingActivities(workActs);
        List<Activity> otherActsWithCharging = PlanUtils.getChargingActivities(otherActs);

        List<Activity> homeActsWithoutCharging = PlanUtils.getNonChargingActivities(homeActs);
        List<Activity> workActsWithoutCharging = PlanUtils.getNonChargingActivities(workActs);
        List<Activity> otherActsWithoutCharging = PlanUtils.getNonChargingActivities(otherActs);
        //
        
        // Remove failed charging activities
        failedChargingActs.forEach((act) -> {PlanUtils.unsetFailed(act);});

        if(personCriticalSOC){

            // If the person has a critical soc, add a charging activity
  /*           if(personHasHomeCharger & !homeActsWithoutCharging.isEmpty()){
                // critical soc and home charger
                PlanUtils.addRandomChargingActivity(homeActsWithoutCharging);
            }
            else if(personHasWorkCharger & !workActsWithoutCharging.isEmpty()){
                // critical soc and work, but no home charger 
                PlanUtils.addRandomChargingActivity(workActsWithoutCharging);
            }
            else if(!homeActsWithoutCharging.isEmpty()){
                // if the person has to charge publicly, charging close to home is still preferred
                PlanUtils.addRandomChargingActivity(homeActsWithoutCharging);
            }
            else if(!workActsWithoutCharging.isEmpty()){
                // if the person has to charge publicly but can not charge close to home, charging close to work is preferred
                PlanUtils.addRandomChargingActivity(workActsWithoutCharging);
            }
            else if(!otherActsWithoutCharging.isEmpty()){
                // critical soc, but person can not charge close to home or work
                PlanUtils.addRandomChargingActivity(otherActsWithoutCharging); // Add charging activity to any activity without charging
            }
            else{ */
                plan = insertfastcharging(plan,tripRouter,getRandomInt(legs.size()));
            //}
        }
        else{

            // non-critical soc: add, change, or remove charging activities
            // constraint: always make sure that people who are flagged as taking part in opportunity charging will continue to do so
            if(personHasPrivateCharger)
            {

                ArrayList<ChargingStrategyChange> viableChanges = new ArrayList<ChargingStrategyChange>();

                // Person has both, home and work charger
                // All options (case dependend): 
                // REMOVEWORK_ADDHOME: remove work, add home charging (charge at home instead of at work)
                // REMOVEHOME_ADDWORK: remove home, add work charging (charge at work instead of at home)
                // REMOVEWORK_ADDWORK: remove work, add work charging (charge some other day at work)
                // REMOVEHOME_ADDHOME: remove home, add home charging (charge some other day at home)
                // ADDHOME: add home charging
                // ADDWORK: add work charging
                // ADDOTHER: add other charging
                // REMOVEHOME: remove home charging
                // REMOVEWORK: remove work charging
                // REMOVEOTHER: remove other charging (if possible)
                // REMOVEOTHER_ADDOTHER: remove other, add other charging (charge at some other non-home/non-work activity)

                // These are the generally available options for people who own a private charger
                if(!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDHOME);
                if(!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDWORK);
                if(!otherActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.ADDOTHER);

                if(!homeActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME);
                if(!workActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK);
                if(!otherActsWithCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEOTHER);

                if(!otherActsWithCharging.isEmpty()&&!otherActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEOTHER_ADDOTHER);

                // Person has a private charger at home or work
                if(personHasHomeCharger&&personHasWorkCharger){
                    
                    // Person has both, home and work charger                 
                    if(!workActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDHOME);
                    if(!homeActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDWORK);
                    if(!workActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDWORK);
                    if(!homeActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDHOME);

                }
                else if(personHasHomeCharger&&!personHasWorkCharger){
                    // Person has a home charger, but not a work charger
                    if(!homeActsWithCharging.isEmpty()&&!homeActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEHOME_ADDHOME);
                }
                else{
                    // Person has a work charger, but not a home charger                       
                    if(!workActsWithCharging.isEmpty()&&!workActsWithoutCharging.isEmpty()) viableChanges.add(ChargingStrategyChange.REMOVEWORK_ADDWORK);
                }

                if(!viableChanges.isEmpty()) // If there are any viable actions...
                {
                    ChargingStrategyChange randomAction = viableChanges.get(PlanUtils.getRandomInt(viableChanges.size())); // ...select a random action ...

                    switch(randomAction) { // ...and execute it. If there are no viable actions the person should still be fine, because they are "uncritical" -> do not change anything
                        case REMOVEWORK_ADDHOME:
                            PlanUtils.changeRandomChargingActivity(workActsWithCharging, homeActsWithoutCharging);
                            break;
                        case REMOVEHOME_ADDWORK:
                            // Remove home, add work charging
                            PlanUtils.changeRandomChargingActivity(homeActsWithCharging, workActsWithoutCharging);
                            break;
                        case REMOVEWORK_ADDWORK:
                            // Remove work, add work
                            PlanUtils.changeRandomChargingActivity(workActsWithCharging, workActsWithoutCharging);
                            break;
                        case REMOVEHOME_ADDHOME:
                            // Remove home, add home
                            PlanUtils.changeRandomChargingActivity(homeActsWithCharging, homeActsWithoutCharging);
                            break;
                        case ADDHOME: 
                            // Add home
                            PlanUtils.addRandomChargingActivity(homeActsWithoutCharging);
                            break;
                        case ADDWORK: 
                            // Add work
                            PlanUtils.addRandomChargingActivity(workActsWithoutCharging);
                            break;
                        case ADDOTHER: 
                            // Add other
                            PlanUtils.addRandomChargingActivity(otherActsWithoutCharging);
                            break;
                        case REMOVEHOME:
                            // Remove home
                            PlanUtils.removeRandomChargingActivity(homeActsWithCharging);
                            break;
                        case REMOVEWORK: 
                            // Remove work
                            PlanUtils.removeRandomChargingActivity(workActsWithCharging);
                            break;
                        case REMOVEOTHER: 
                            // Remove other, add other
                            PlanUtils.removeRandomChargingActivity(otherActsWithCharging);
                            break;
                        case REMOVEOTHER_ADDOTHER: 
                            // Remove other, add other
                            PlanUtils.changeRandomChargingActivity(otherActsWithCharging, otherActsWithoutCharging);
                            break;
                    }
                }
                

            }
            else
            {
                // Person has no private charger and is entirely reliant on public chargers
                // -> Randomly change, remove, or add a charging activity with equal probability


                switch(getRandomInt(5)) {
                    case 0:
                        if(!noChargingActs.isEmpty()&&!allChargingActs.isEmpty()){
                            PlanUtils.changeRandomChargingActivity(allChargingActs, noChargingActs);
                        }
                        break;
                    case 1:
                        if(!successfulChargingActs.isEmpty()){
                            PlanUtils.removeRandomChargingActivity(allChargingActs);
                        }
                        break;
                    case 2:
                        if(!noChargingActs.isEmpty()){
                            PlanUtils.addRandomChargingActivity(noChargingActs);
                        }
                        break;
                    case 3:
                        plan = insertfastcharging(plan,tripRouter,getRandomInt(legs.size()));
                        break;
                    case 4:
                        plan = randomlyremovefastcharging(plan);
                        break;
                        
                }
            }

        }

        // Assume that the person belongs to the non-critical group after replanning
        PersonUtils.setNonCritical(person);

        person.setSelectedPlan(plan);

    }

    private void addRandomChargingActivity(List<Activity> potential_add_acts) {
        // select random activity without charging and change to activity with charging
        if (!potential_add_acts.isEmpty()) {
            PlanUtils.setCharging(getRandomActivity(potential_add_acts));
        }
    }

    private void removeRandomChargingActivity(List<Activity> potential_remove_acts) {
        // select random activity with charging and change to activity without charging
        if (!potential_remove_acts.isEmpty()) {
            PlanUtils.unsetCharging((getRandomActivity(potential_remove_acts)));
        }
    }

    private void changeRandomChargingActivity(
                                List<Activity> chargingActs,
                                List<Activity> noChargingActs) {
        // Change by subsequently removing and adding charging activities
        if (!chargingActs.isEmpty() && !noChargingActs.isEmpty()) {
            removeRandomChargingActivity(chargingActs);
            addRandomChargingActivity(noChargingActs);
        }
    }

    private int getRandomInt(int max)
    {
        Random random = MatsimRandom.getLocalInstance();
        //random.setSeed(System.currentTimeMillis());
        return random.nextInt(max);
    }

    private Activity getRandomActivity(List<Activity> activities)
    {
        
        return activities.get(getRandomInt(activities.size()));
    }

    private Plan insertfastcharging(Plan plan,TripRouter tripRouter, int position){

        final List<Trip> trips = TripStructureUtils.getTrips( plan );
    
        Trip oldTrip = trips.get(position);
                //final String routingMode = TripStructureUtils.identifyMainMode( oldTrip.getTripElements() );
                //logger.debug( "about to call TripRouter with routingMode=" + routingMode ) ;
        final List<? extends PlanElement> newTrip =
            tripRouter.calcRoute(
                            "dc_charging",
                              FacilitiesUtils.toFacility( oldTrip.getOriginActivity(), facilities ),
                              FacilitiesUtils.toFacility( oldTrip.getDestinationActivity(), facilities ),
                              calcEndOfActivity(oldTrip.getOriginActivity(), plan , tripRouter.getConfig()),
                                plan.getPerson() );
                //putVehicleFromOldTripIntoNewTripIfMeaningful(oldTrip, newTrip);
        TripRouter.insertTrip(
                        plan, 
                        oldTrip.getOriginActivity(),
                        newTrip,
                        oldTrip.getDestinationActivity());
            
        
        return plan;

    }

    private Plan randomlyremovefastcharging(Plan plan){

        List<Activity> activities = PlanUtils.getActivities(plan);
        
        int index = 0;
        int count = 0;
        int number = 0;
        int number2 = 0;
        // count activities
        for (Activity oldActivities : activities) {

            if (oldActivities.getType() =="car fast charging")
            {
            // Plan neu routen 
            count +=1;
            }
        }
        if (count > 0){
            number = getRandomInt(count) + 1;
            for (Activity oldActivities : activities) {

                if (oldActivities.getType() =="car fast charging")
                {
                number2 += 1;
                if (number2 == number){
                    PopulationUtils.removeActivity(plan, index*2);
                }
                // Plan neu routen
                }
                index += 1;

            }  
        }
 
        return plan;

    }
    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

        }
        public static double calcEndOfActivity(
            final Activity activity,
            final Plan plan,
            final Config config ) {
        // yyyy similar method in PopulationUtils.  TripRouter.calcEndOfPlanElement in fact uses it.  However, this seems doubly inefficient; calling the
        // method in PopulationUtils directly would probably be faster.  kai, jul'19

        if (activity.getEndTime().isDefined())
            return activity.getEndTime().seconds();

        // no sufficient information in the activity...
        // do it the long way.
        // XXX This is inefficient! Using a cache for each plan may be an option
        // (knowing that plan elements are iterated in proper sequence,
        // no need to re-examine the parts of the plan already known)
        double now = 0;

        for (PlanElement pe : plan.getPlanElements()) {
            now = TripRouter.calcEndOfPlanElement(now, pe, config);
            if (pe == activity) return now;
        }

        throw new RuntimeException( "activity "+activity+" not found in "+plan.getPlanElements() );
    }
}
