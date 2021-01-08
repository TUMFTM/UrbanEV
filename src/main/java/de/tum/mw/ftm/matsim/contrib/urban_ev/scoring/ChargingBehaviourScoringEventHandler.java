package de.tum.mw.ftm.matsim.contrib.urban_ev.scoring;

import org.matsim.core.events.handler.EventHandler;

public interface ChargingBehaviourScoringEventHandler extends EventHandler {
    void handleEvent(ChargingBehaviourScoringEvent event);

}