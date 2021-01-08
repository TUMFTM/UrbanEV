package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import org.matsim.core.events.handler.EventHandler;

public interface UnpluggingEventHandler extends EventHandler {
    void handleEvent(UnpluggingEvent event);

}
