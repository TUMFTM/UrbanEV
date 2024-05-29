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
import java.util.Random;
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
        
        Double number_of_charging_acts = Double.parseDouble(person.getAttributes().getAttribute("number_of_chargings").toString());
        
        //eigentlich benötigen wir hier eine Liste mit charging acts 

        //tripRouter.getRoutingModule("dc_charging").calcRoute(null, null, 0, person);
        // derived person characteristics
        //boolean personHasHomeCharger = PersonUtils.hasHomeCharger(person);
        //boolean personHasWorkCharger = PersonUtils.hasWorkCharger(person);
        //boolean personHasPrivateCharger = PersonUtils.hasPrivateCharger(person);
        //boolean personCriticalSOC = true; //PersonUtils.isCritical(person);
        
        
        //plan = removefastcharging(plan);

        //List<Leg> legs = plan.getPlanElements().stream().filter(f -> f instanceof Leg).map(pe -> (Leg) pe).collect(Collectors.toList());
        // person plan analysis
        List<Activity> activities = PlanUtils.getActivities(plan);
        List<Activity> nonStartOrEndActs = PlanUtils.getNonIniEndActivities(activities);

        //List<Activity> homeActs = PlanUtils.getHomeActivities(nonStartOrEndActs);
        //List<Activity> workActs = PlanUtils.getWorkActivities(nonStartOrEndActs);
        //List<Activity> otherActs = nonStartOrEndActs.stream().filter(a -> !homeActs.contains(a) & !workActs.contains(a)).collect(Collectors.toList());

        // Apply plan changes

        
        // first, analyze current charging behavior
        List<Activity> allChargingActs = PlanUtils.getChargingActivities(nonStartOrEndActs);
        Integer number_current_charging_acts = allChargingActs.size();
        //List<Activity> noChargingActs = PlanUtils.getNonChargingActivities(nonStartOrEndActs);

        List<Integer> nonfastCharginglegs = PlanUtils.get_non_fast_charging_legs(plan, 0, null);
        List<Integer> fastChargingactivites = PlanUtils.get_fast_charging_activities(plan, 0, null);

        //Random random = new Random();
        if (number_current_charging_acts < number_of_charging_acts){
            while (number_current_charging_acts < number_of_charging_acts) {
                nonfastCharginglegs = PlanUtils.get_non_fast_charging_legs(plan, 0, null);
                int randomIndex = getRandomInt(nonfastCharginglegs.size());
                Integer index = nonfastCharginglegs.get(randomIndex);
                plan = insertfastcharging(plan,tripRouter, index);
                number_current_charging_acts = number_current_charging_acts + 1;
            }
        }
        else{
            if(number_of_charging_acts > 0 && fastChargingactivites.size() > 0){
                int randomIndex = getRandomInt(fastChargingactivites.size());
                Integer index = fastChargingactivites.get(randomIndex);
                plan = removefastcharging(plan,tripRouter, index);
                nonfastCharginglegs = PlanUtils.get_non_fast_charging_legs(plan, 0, null);
                randomIndex = getRandomInt(nonfastCharginglegs.size());
                index = nonfastCharginglegs.get(randomIndex);
                plan = insertfastcharging(plan,tripRouter, index);
            }
        }
        person.setSelectedPlan(plan);

    }


    private int getRandomInt(int max)
    {
        Random random = MatsimRandom.getLocalInstance();
        //random.setSeed(System.currentTimeMillis());
        return random.nextInt(max);
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
        Double old_distance = 0.0;
        // put the infor

        if (oldTrip.getLegsOnly().get(0).getTravelTime().isDefined()){
            old_distance = oldTrip.getLegsOnly().get(0).getTravelTime().seconds();
        }
        else{
            old_distance = 0.0;
        }
        
        if( newTrip.size() == 3){
            Leg first_leg = (Leg) newTrip.get(0);
            Leg second_leg = (Leg) newTrip.get(2);
            //Double new_Distance = first_leg.getRoute().getDistance() + second_leg.getRoute().getDistance();
            Double new_Distance = first_leg.getRoute().getTravelTime().seconds() + second_leg.getRoute().getTravelTime().seconds();
            
            Double detour = 0.0;

            if (!oldTrip.getLegsOnly().get(0).getTravelTime().isDefined()){
                detour = 0.0;
            }else{
                detour = new_Distance - old_distance;  
            }

            newTrip.get(1).getAttributes().putAttribute("detour", detour);

            TripRouter.insertTrip(
                            plan, 
                            oldTrip.getOriginActivity(),
                            newTrip,
                            oldTrip.getDestinationActivity());
        }

        
        return plan;

    }

    private Plan removefastcharging(Plan plan,TripRouter tripRouter, int position){

        final List<Trip> trips = TripStructureUtils.getTrips( plan );
        Trip to_charger = trips.get(position-1);
        Trip after_charger = trips.get(position);
                //final String routingMode = TripStructureUtils.identifyMainMode( oldTrip.getTripElements() );
                //logger.debug( "about to call TripRouter with routingMode=" + routingMode ) ;
        final List<? extends PlanElement> newTrip =
            tripRouter.calcRoute(
                            "car",
                              FacilitiesUtils.toFacility( to_charger.getOriginActivity(), facilities ),
                              FacilitiesUtils.toFacility( after_charger.getDestinationActivity(), facilities ),
                              calcEndOfActivity(to_charger.getOriginActivity(), plan , tripRouter.getConfig()),
                                plan.getPerson() );

        TripRouter.insertTrip(
                            plan, 
                            to_charger.getOriginActivity(),
                            newTrip,
                            after_charger.getDestinationActivity());
        
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
