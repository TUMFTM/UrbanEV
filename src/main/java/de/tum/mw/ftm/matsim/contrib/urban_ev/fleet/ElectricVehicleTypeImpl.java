package de.tum.mw.ftm.matsim.contrib.urban_ev.fleet;

import org.matsim.api.core.v01.Id;

import java.util.Objects;

public class ElectricVehicleTypeImpl implements ElectricVehicleType {
    private final Id<ElectricVehicleType> id;
    private final String name;
    private final double batteryCapacity;
    private final double consumption;
    private final double maxChargingRate;

    private ElectricVehicleTypeImpl(ElectricVehicleTypeImpl.Builder builder) {
        id = Objects.requireNonNull(builder.id);
        name = Objects.requireNonNull(builder.name);
        batteryCapacity = Objects.requireNonNull(builder.batteryCapacity);
        consumption = Objects.requireNonNull(builder.consumption);
        maxChargingRate = Objects.requireNonNull(builder.maxChargingRate);
    }

    public static ElectricVehicleTypeImpl.Builder newBuilder() {
        return new ElectricVehicleTypeImpl.Builder();
    }

    public static ElectricVehicleTypeImpl.Builder newBuilder(ElectricVehicleTypeImpl copy) {
        ElectricVehicleTypeImpl.Builder builder = new ElectricVehicleTypeImpl.Builder();
        builder.id = copy.getId();
        builder.name = copy.getName();
        builder.batteryCapacity = copy.getBatteryCapacity();
        builder.consumption = copy.getConsumption();
        builder.maxChargingRate = copy.getMaxChargingRate();
        return builder;
    }

    @Override
    public Id<ElectricVehicleType> getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getConsumption() {
        return consumption;
    }

    @Override
    public double getMaxChargingRate() {
        return maxChargingRate;
    }

    @Override
    public double getBatteryCapacity(){
        return batteryCapacity;
    }

    public static final class Builder {
        private Id<ElectricVehicleType> id;
        private String name;
        private Double batteryCapacity;
        private Double consumption;
        private Double maxChargingRate;

        private Builder() {
        }

        public ElectricVehicleTypeImpl.Builder id(Id<ElectricVehicleType> val) {
            id = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder name(String val) {
            name = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder consumption(double val) {
            consumption = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder maxChargingRate(double val) {
            maxChargingRate = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder batteryCapacity(double val){
            batteryCapacity = val;
            return this;
        }

        public ElectricVehicleTypeImpl build() {
            return new ElectricVehicleTypeImpl(this);
        }
    }
}
