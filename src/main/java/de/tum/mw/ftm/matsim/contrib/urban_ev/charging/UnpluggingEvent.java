package de.tum.mw.ftm.matsim.contrib.urban_ev.charging;

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;

import java.util.Map;

public class UnpluggingEvent extends Event {
	public static final String EVENT_TYPE = "unplugging";
	public static final String ATTRIBUTE_CHARGER = "charger";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String ATTRIBUTE_PLUGGEDDURATION = "connection_duration";

	private final Id<Charger> chargerId;
	private final Id<ElectricVehicle> vehicleId;
	private final Double pluggedInDuration;

	public UnpluggingEvent(double time, Id<Charger> chargerId, Id<ElectricVehicle> vehicleId, double pluggedInDuration) {
		super(time);
		this.chargerId = chargerId;
		this.vehicleId = vehicleId;
		this.pluggedInDuration = pluggedInDuration;
	}

	public double getPluggedInDuration() {
		return pluggedInDuration;
	}

	public Id<Charger> getChargerId() {
		return chargerId;
	}

	public Id<ElectricVehicle> getVehicleId() {
		return vehicleId;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_CHARGER, chargerId.toString());
		attr.put(ATTRIBUTE_VEHICLE, vehicleId.toString());
		attr.put(ATTRIBUTE_PLUGGEDDURATION, String.valueOf(pluggedInDuration));
		return attr;
	}
}
