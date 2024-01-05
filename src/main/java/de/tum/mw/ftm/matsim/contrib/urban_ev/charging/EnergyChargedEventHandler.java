package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import org.matsim.core.events.handler.EventHandler;

/**
 * @author Michal Maciejewski (michalm)
 */
public interface EnergyChargedEventHandler extends EventHandler {
	void handleEvent(EnergyChargedEvent event);
}
