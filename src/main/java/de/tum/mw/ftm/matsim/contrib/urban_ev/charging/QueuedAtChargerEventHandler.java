package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import org.matsim.core.events.handler.EventHandler;

public interface QueuedAtChargerEventHandler extends EventHandler {
	void handleEvent(QueuedAtChargerEvent event);
}
