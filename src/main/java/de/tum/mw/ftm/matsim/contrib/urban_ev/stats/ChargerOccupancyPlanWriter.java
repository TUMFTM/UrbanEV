package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.misc.Time;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Writes via-readable charger occupancy plan
 * Activity Type is set between 0 and 1 and indicates charger occupancy
 *
 * charger_occupancy_absolute.xy.gz can also be imported to via...
 */

public class ChargerOccupancyPlanWriter extends MatsimXmlWriter {
	private final Map<Charger, ChargerOccupancyHistoryCollector.OccupancyHistory> occupancyHistories;

	public ChargerOccupancyPlanWriter(Map<Charger, ChargerOccupancyHistoryCollector.OccupancyHistory> occupancyHistories) {
		this.occupancyHistories = occupancyHistories;
	}

	public void write(String file) {
		openFile(file);
		writeDoctype("population", "http://www.matsim.org/files/dtd/population_v6.dtd");
		writeStartTag("population", Collections.<Tuple<String, String>>emptyList());
		occupancyHistories.forEach(this::writeChargerOccupancy);
		writeEndTag("population");
		close();
	}

	public void writeChargerOccupancy(Charger charger, ChargerOccupancyHistoryCollector.OccupancyHistory history) {

		List<Tuple<String, String>> person = Arrays.asList(Tuple.of("id", charger.getId().toString()));
		writeStartTag("person", person);

		// attributes
		List<Tuple<String, String>> attributes = Arrays.asList();
		writeStartTag("attributes", attributes);

		List<Tuple<String, String>> attribute = Arrays.asList(
				Tuple.of("name", "plug_count"),
				Tuple.of("class", "java.lang.Integer"));
		writeStartTag("attribute", attribute);
		writeContent(Integer.toString(charger.getPlugCount()), false);
		writeEndTag("attribute");

		writeEndTag("attributes");
		// attributes end

		// plan
		List<Tuple<String, String>> plan = Arrays.asList();
		writeStartTag("plan", plan);

		String x = Double.toString(charger.getCoord().getX());
		String y = Double.toString(charger.getCoord().getY());
		int plugCount = charger.getPlugCount();
		int pluggedVehicles = 0;
		String type = String.format("%.1g", (double)pluggedVehicles/plugCount);
		String startTime = "00:00:00";

		for (Map.Entry<Double, Integer> entry : history.getPluggedVehicleCounts().entrySet()) {
			String endTime = Time.writeTime(entry.getKey());

			List<Tuple<String, String>> act = Arrays.asList(
					Tuple.of("type", type),
					Tuple.of("x", x),
					Tuple.of("y", y),
					Tuple.of("start_time", startTime),
					Tuple.of("end_time", endTime));
			writeStartTag("activity", act, true);

			List<Tuple<String, String>> leg = Arrays.asList(Tuple.of("mode", "car"));
			writeStartTag("leg", leg, true);

			startTime = endTime;
			pluggedVehicles = entry.getValue();
			type = String.format("%.1g", (double)pluggedVehicles/plugCount);
		}

		List<Tuple<String, String>> act = Arrays.asList(
				Tuple.of("type", Integer.toString(pluggedVehicles)),
				Tuple.of("x", x),
				Tuple.of("y", y));
		writeStartTag("activity", act, true);

		writeEndTag("plan");
		// plan end

		writeEndTag("person");

	}
}
