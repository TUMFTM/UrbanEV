/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE

 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure;

import com.google.common.collect.ImmutableMap;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingLogic;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ChargingInfrastructures {
	public static ChargingInfrastructure createChargingInfrastructure(
			ChargingInfrastructureSpecification infrastructureSpecification, Network network,
			ChargingLogic.Factory chargingLogicFactory) {
		ImmutableMap<Id<Charger>, Charger> chargers = infrastructureSpecification.getChargerSpecifications()
				.values()
				.stream()
				.map(s -> ChargerImpl.create(s, NetworkUtils.getNearestLink(network, s.getCoord()), chargingLogicFactory))
				.collect(ImmutableMap.toImmutableMap(Charger::getId, ch -> ch));
		return () -> chargers;
	}
}
