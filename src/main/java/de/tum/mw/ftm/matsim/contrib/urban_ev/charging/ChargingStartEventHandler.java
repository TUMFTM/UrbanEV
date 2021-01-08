/*
File originally created, published and licensed by contributors of the org.matsim.* project.
The original file did not include a specific license notice. The corresponding project was published under GPL v2.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import org.matsim.core.events.handler.EventHandler;

public interface ChargingStartEventHandler extends EventHandler {
    void handleEvent(ChargingStartEvent event);

}
