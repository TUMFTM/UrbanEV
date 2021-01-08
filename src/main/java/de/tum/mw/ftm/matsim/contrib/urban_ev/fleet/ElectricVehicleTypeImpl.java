package de.tum.mw.ftm.matsim.contrib.urban_ev.fleet;

import org.matsim.api.core.v01.Id;

import java.util.Objects;

public class ElectricVehicleTypeImpl implements ElectricVehicleType {
    private final Id<ElectricVehicleType> id;
    private final String name;
    private final double consumption;
    private final double maxChargingRate;
    private final double mass;
    private final double width;
    private final double height;
    private final double length;
    private final double aerodynamicDragCoefficient;
    private final double rollingDragCoefficient;
    private final double inertiaResistanceCoefficient;
    private final double driveTrainEfficiency;


    private ElectricVehicleTypeImpl(ElectricVehicleTypeImpl.Builder builder) {
        id = Objects.requireNonNull(builder.id);
        name = Objects.requireNonNull(builder.name);
        consumption = Objects.requireNonNull(builder.consumption);
        maxChargingRate = Objects.requireNonNull(builder.maxChargingRate);
        mass = Objects.requireNonNull(builder.mass);
        width = Objects.requireNonNull(builder.width);
        height = Objects.requireNonNull(builder.height);
        length = Objects.requireNonNull(builder.length);
        aerodynamicDragCoefficient = Objects.requireNonNull(builder.aerodynamicDragCoefficient);
        rollingDragCoefficient = Objects.requireNonNull(builder.rollingDragCoefficient);
        inertiaResistanceCoefficient = Objects.requireNonNull(builder.inertiaResistanceCoefficient);
        driveTrainEfficiency = Objects.requireNonNull(builder.driveTrainEfficiency);
    }

    public static ElectricVehicleTypeImpl.Builder newBuilder() {
        return new ElectricVehicleTypeImpl.Builder();
    }

    public static ElectricVehicleTypeImpl.Builder newBuilder(ElectricVehicleTypeImpl copy) {
        ElectricVehicleTypeImpl.Builder builder = new ElectricVehicleTypeImpl.Builder();
        builder.id = copy.getId();
        builder.name = copy.getName();
        builder.consumption = copy.getConsumption();
        builder.maxChargingRate = copy.getMaxChargingRate();
        builder.mass = copy.getMass();
        builder.width = copy.getWidth();
        builder.height = copy.getHeight();
        builder.length = copy.getLength();
        builder.aerodynamicDragCoefficient = copy.getAerodynamicDragCoefficient();
        builder.rollingDragCoefficient = copy.getRollingDragCoefficient();
        builder.inertiaResistanceCoefficient = copy.getInertiaResistanceCoefficient();
        builder.driveTrainEfficiency = copy.getDriveTrainEfficiency();
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
    public double getMass() {
        return mass;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double getAerodynamicDragCoefficient() {
        return aerodynamicDragCoefficient;
    }

    @Override
    public double getRollingDragCoefficient() {
        return rollingDragCoefficient;
    }

    @Override
    public double getInertiaResistanceCoefficient() {
        return inertiaResistanceCoefficient;
    }

    @Override
    public double getDriveTrainEfficiency() {
        return driveTrainEfficiency;
    }

    public static final class Builder {
        private Id<ElectricVehicleType> id;
        private String name;
        private Double consumption;
        private Double maxChargingRate;
        private Double mass;
        private Double width;
        private Double height;
        private Double length;
        private Double aerodynamicDragCoefficient;
        private Double rollingDragCoefficient;
        private Double inertiaResistanceCoefficient;
        private Double driveTrainEfficiency;

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

        public ElectricVehicleTypeImpl.Builder mass(double val) {
            mass = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder width(double val) {
            width = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder height(double val) {
            height = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder length(double val) {
            length = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder aerodynamicDragCoefficient(double val) {
            aerodynamicDragCoefficient = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder rollingDragCoefficient(double val) {
            rollingDragCoefficient = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder inertiaResistanceCoefficient(double val) {
            inertiaResistanceCoefficient = val;
            return this;
        }

        public ElectricVehicleTypeImpl.Builder driveTrainEfficiency(double val) {
            driveTrainEfficiency = val;
            return this;
        }

        public ElectricVehicleTypeImpl build() {
            return new ElectricVehicleTypeImpl(this);
        }
    }
}
