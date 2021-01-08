/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE
 *
 * *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package de.tum.mw.ftm.matsim.contrib.urban_ev.discharging;

import com.google.inject.Inject;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicleType;
import org.matsim.api.core.v01.network.Link;

/**
 * Enabled diffenrent vehicle types
 * Parametrised for the Nissan Leaf. All values in SI units.
 * See:
 * Ohde, B., Ślaski, G., Maciejewski, M. (2016). Statistical analysis of real-world urban driving cycles for modelling
 * energy consumption of electric vehicles. Journal of Mechanical and Transport Engineering, 68.
 * <p>
 * http://fwmt.put.poznan.pl/imgWYSIWYG/agill/File/2_68_2016/jmte_2016_68_2_03_ohde_b_slaski_g.pdf
 * TODO Add (dis-)charging efficiency relative to SOC, temperature, etc...
 */
public class TypeSpecificOhdeSlaskiDriveEnergyConsumption implements DriveEnergyConsumption {

	// constants
	private final double g = 9.81; // g [m/s^2]
	private final double rho = 1.184; // [kg/m^3] air density at standard temperature and pressure

	// vehicle specific data
	private double w; // vehicle width [m]
	private double h; // vehicle height [m]
	private double m_s; // vehicle mass + extra mass [kg]
	private double cw; //  // aerodynamic drag coefficient cw [-]
	private double ft; // rolling drag coefficient[-]
	private double cb; // inertia resistance coefficient [-]
	private double spr; // drive train efficiency [-]

	// acceleration approximation in: a1 * ln(v / 1 [m/s]) + a2
	private final double a1 = -0.267;// [m/s^2]
	private final double a2 = 0.99819;// [m/s^2]

	// precomputed values
	private final int MAX_AVG_SPEED = 80;
	private final int SPEED_STEPS_PER_UNIT = 10;
	private final double ZERO_SPEED = 0.01;
	private final double[] POWER;

	@Inject
	public TypeSpecificOhdeSlaskiDriveEnergyConsumption(ElectricVehicle electricVehicle) {
		ElectricVehicleType type = electricVehicle.getVehicleType();
		m_s = type.getMass() + 100;
		w = type.getWidth();
		h = type.getHeight();
		cw = type.getAerodynamicDragCoefficient();
		ft = type.getRollingDragCoefficient();
		cb = type.getInertiaResistanceCoefficient();
		spr = type.getDriveTrainEfficiency();

		POWER = new double[MAX_AVG_SPEED * SPEED_STEPS_PER_UNIT];
		POWER[0] = calcPower(ZERO_SPEED);
		for (int i = 1; i < POWER.length; i++) {
			POWER[i] = calcPower((double)i / SPEED_STEPS_PER_UNIT);
		}
	}

	// v - avg speed [m/s]
	// POWER - avg POWER [W]
	private double calcPower(double v) {
		return v * (ft * m_s * g + 0.5 * cw * w * h * rho * v * v + cb * (a1 * Math.log(v) + a2) * m_s) / spr;
	}

	@Override
	public double calcEnergyConsumption(Link link, double travelTime, double linkEnterTime) {
		if (travelTime == 0) {
			return 0;
		}

		double avgSpeed = link.getLength() / travelTime;
		int idx = (int)Math.round(avgSpeed * SPEED_STEPS_PER_UNIT);
		return POWER[idx] * travelTime;
	}
}
