/*
File originally created, published and licensed by contributors of the org.matsim.* project.
Please consider the original license notice below.
This is a modified version of the original source code!

Modified 2020 by Lennart Adenaw, Technical University Munich, Chair of Automotive Technology
email	:	lennart.adenaw@tum.de
*/

/* ORIGINAL LICENSE
 *  *********************************************************************** *
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

package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricFleet;
import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import org.matsim.contrib.util.histogram.UniformHistogram;
import org.matsim.contrib.util.timeprofile.TimeProfileCharts;
import org.matsim.contrib.util.timeprofile.TimeProfileCharts.ChartType;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector.ProfileCalculator;
import org.matsim.contrib.util.timeprofile.TimeProfiles;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;

import java.awt.*;

public class SocHistogramTimeProfileCollectorProvider implements Provider<MobsimListener> {
    private final ElectricFleet evFleet;
    private final MatsimServices matsimServices;

    @Inject
    public SocHistogramTimeProfileCollectorProvider(ElectricFleet evFleet, MatsimServices matsimServices) {
        this.evFleet = evFleet;
        this.matsimServices = matsimServices;
    }

    @Override
    public MobsimListener get() {
        ProfileCalculator calc = createSocHistogramCalculator(evFleet);
        TimeProfileCollector collector = new TimeProfileCollector(calc, 300, "soc_histogram_time_profiles",
                matsimServices);
        collector.setChartTypes(ChartType.StackedArea);
        collector.setChartCustomizer((chart, chartType) -> {
            TimeProfileCharts.changeSeriesColors(chart,
                    new Color(0, 0, 0), // 0
                    new Color(245, 0, 0), // 0+
                    new Color(255, 0, 0), // 0.1+
                    new Color(255, 63, 0), // 0.2+
                    new Color(255, 127, 0), // 0.3+
                    new Color(255, 191, 0), // 0.4+
                    new Color(255, 255, 0), // 0.5+
                    new Color(191, 255, 0), // 0.6+
                    new Color(127, 255, 0), // 0.7+
                    new Color(63, 255, 0), // 0.8+
                    new Color(0, 245, 0) // 0.9+
            );
        });
        return collector;
    }

    public static ProfileCalculator createSocHistogramCalculator(final ElectricFleet evFleet) {
        String[] header = {"0", "0+", "0.1+", "0.2+", "0.3+", "0.4+", "0.5+", "0.6+", "0.7+", "0.8+", "0.9+"};
        return TimeProfiles.createProfileCalculator(header, () -> {
            UniformHistogram histogram = new UniformHistogram(0.1, header.length - 1);
            long emptyBatteries = 0;
            for (ElectricVehicle ev : evFleet.getElectricVehicles().values()) {
                double soc = ev.getBattery().getSoc() / ev.getBattery().getCapacity();
                if (soc == 0) {
                    emptyBatteries++;
                } else {
                    histogram.addValue(soc);
                }
            }

            Long[] values = new Long[header.length];
            values[0] = emptyBatteries;
            for (int b = 1; b < header.length; b++) {
                values[b] = histogram.getCount(b - 1);
            }
            return values;
        });
    }
}
