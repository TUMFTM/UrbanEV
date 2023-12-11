package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.tum.mw.ftm.matsim.contrib.urban_ev.charging.ChargingLogic;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.ChargingInfrastructure;
import org.matsim.contrib.util.timeprofile.TimeProfileCharts;
import org.matsim.contrib.util.timeprofile.TimeProfileCharts.ChartType;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector.ProfileCalculator;
import org.matsim.contrib.util.timeprofile.TimeProfiles;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;

import java.awt.*;

public class ChargerTypeOccupancyTimeProfileCollectorProvider implements Provider<MobsimListener> {
	private final ChargingInfrastructure chargingInfrastructure;
	private final MatsimServices matsimServices;

	@Inject
	public ChargerTypeOccupancyTimeProfileCollectorProvider(ChargingInfrastructure chargingInfrastructure,
															MatsimServices matsimServices) {
		this.chargingInfrastructure = chargingInfrastructure;
		this.matsimServices = matsimServices;
	}

	@Override
	public MobsimListener get() {
		ProfileCalculator calc = createChargerOccupancyCalculator(chargingInfrastructure);
		TimeProfileCollector collector = new TimeProfileCollector(calc, 300, "charger_type_occupancy_time_profiles",
				matsimServices);
		collector.setChartTypes(ChartType.Line, ChartType.StackedArea);
		collector.setChartCustomizer((chart, chartType) -> {
			TimeProfileCharts.changeSeriesColors(chart,
					new Color(0, 255, 0), // public
					new Color(255, 0, 0), // home
					new Color(0, 0, 255), // work
					new Color(0, 0, 0) // dc
			);
		});
		return collector;
	}

	public static ProfileCalculator createChargerOccupancyCalculator(
			final ChargingInfrastructure chargingInfrastructure) {
		String[] header = { "public", "home", "work", "dc"};
		return TimeProfiles.createProfileCalculator(header, () -> {
			int publicPlugged = 0;
			int homePlugged = 0;
			int workPlugged = 0;
			int dcPlugged = 0;
			for (Charger c : chargingInfrastructure.getChargers().values()) {
				ChargingLogic logic = c.getLogic();
				String chargerId = c.getId().toString();
				String chargerType = c.getChargerType();

				int plugged = logic.getPluggedVehicles().size();
				if (chargerId.contains("work")) {
					workPlugged += plugged;
				} else if (chargerId.contains("home")) {
					homePlugged += plugged;
				} else if (chargerType.contains("dc")) {
					dcPlugged += plugged;
				} else {
					publicPlugged += plugged;
				}
			}
			return new Integer[] { publicPlugged, homePlugged, workPlugged, dcPlugged};
		});
	}
}
