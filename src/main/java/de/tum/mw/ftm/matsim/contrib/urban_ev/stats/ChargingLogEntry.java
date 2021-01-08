package de.tum.mw.ftm.matsim.contrib.urban_ev.stats;

import de.tum.mw.ftm.matsim.contrib.urban_ev.fleet.ElectricVehicle;
import de.tum.mw.ftm.matsim.contrib.urban_ev.infrastructure.Charger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.core.utils.misc.Time;

/**
 * A container to hold data regarding charging activities
 *
 * @author Lennart Adenaw on 08.09.2020
 */
public class ChargingLogEntry {

    static private final int SECS_PER_DAY = 24*60*60;

    private Charger charger;
    private boolean chargerSet = false;

    private Id<ElectricVehicle> electricVehicleId;

    private double startTime;
    private int startDay;
    private boolean startTimeSet = false;

    private double endTime;
    private int endDay;
    private boolean endTimeSet = false;

    private double startSOC;
    private boolean startSOCSet = false;

    private double startSOC_J;
    private boolean startSOC_JSet;

    private double endSOC;
    private boolean endSOCSet = false;

    private double endSOC_J;
    private boolean endSOC_JSet;

    private double unplugTime;
    private int unplugDay;
    private boolean unplugTimeSet = false;

    private double pluggedDuration;
    private boolean pluggedDurationSet = false;

    private double chargingDuration;
    private boolean chargingDurationSet = false;

    private double chargingRatio;
    private boolean chargingRatioSet = false;

    private double walkingDistance;
    private boolean walkingDistanceSet = false;

    private double transmittedEnergy_J;
    private boolean transmittedEnergy_JSet;

    public ChargingLogEntry(Id<ElectricVehicle> vehicle) {

        this.electricVehicleId = vehicle;

    }

    public boolean complete(){
        if(     startTimeSet&&
                endTimeSet&&
                startSOCSet&&
                endSOCSet&&
                walkingDistanceSet&&
                chargingDurationSet&&
                unplugTimeSet&&
                pluggedDurationSet&&
                chargingRatioSet &&
                chargerSet &&
                transmittedEnergy_JSet &&
                startSOC_JSet &&
                endSOC_JSet
        ){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean valid(){

        if(endTime<startTime || endSOC_J<startSOC_J || walkingDistance<0 || endTime-startTime!=chargingDuration || unplugTime<endTime || pluggedDuration!=unplugTime-startTime || transmittedEnergy_J!=endSOC_J-startSOC_J)
        {
            return false;
        }
        else return true;

    }

    public double getStartSOC_J() {
        return startSOC_J;
    }

    public void setStartSOC_J(double startSOC_J) {
        this.startSOC_J = startSOC_J;
        this.startSOC_JSet = true;
    }

    public double getEndSOC_J() {
        return endSOC_J;
    }

    public void setEndSOC_J(double endSOC_J) {
        this.endSOC_J = endSOC_J;
        this.endSOC_JSet = true;
    }

    public double getTransmittedEnergy_J() {
        return transmittedEnergy_J;
    }

    public void setTransmittedEnergy_J(double transmittedEnergy_J) {
        this.transmittedEnergy_J = transmittedEnergy_J;
        this.transmittedEnergy_JSet = true;
    }

    public Id<ElectricVehicle> getElectricVehicleId() {
        return electricVehicleId;
    }

    public double getChargingDuration() {
        return chargingDuration;
    }

    public void setChargingDuration(double duration) {
        this.chargingDuration = duration;
        this.chargingDurationSet = true;
    }

    public Charger getCharger() {
        return charger;
    }

    public void setCharger(Charger charger) {
        this.charger = charger;
        this.chargerSet = true;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
        this.startDay = (int) (startTime/(24*60*60))+1;
        this.startTimeSet = true;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
        this.endDay = (int) (endTime/(24*60*60))+1;
        this.endTimeSet = true;
    }

    public void setUnplugTime(double unplugTime) {
        this.unplugTime = unplugTime;
        this.unplugDay = (int) (unplugTime/(24*60*60))+1;
        this.unplugTimeSet = true;
    }

    public double getPluggedDuration() {
        return pluggedDuration;
    }

    public void setPluggedDuration(double pluggedDuration) {
        this.pluggedDuration = pluggedDuration;
        this.pluggedDurationSet = true;
    }

    public double getStartSOC() {
        return startSOC;
    }

    public void setStartSOC(double startSOC) {
        this.startSOC = startSOC;
        this.startSOCSet = true;
    }

    public double getEndSOC() {
        return endSOC;
    }

    public void setEndSOC(double endSOC) {
        this.endSOC = endSOC;
        this.endSOCSet = true;
    }

    public double getChargingRatio() {
        return chargingRatio;
    }

    public void setChargingRatio(double chargingRatio) {
        this.chargingRatio = chargingRatio;
        this.chargingRatioSet=true;
    }

    public double getWalkingDistance() {
        return walkingDistance;
    }

    public void setWalkingDistance(double walkingDistance) {
        this.walkingDistance = walkingDistance;
        this.walkingDistanceSet = true;
    }

    public String toString(){
        return  this.charger.getId().toString()
                + ";"
                +this.charger.getCoord().getX()
                + ";"
                +this.charger.getCoord().getY()
                + ";"
                +this.electricVehicleId.toString()
                + ";"
                +Time.writeTime(this.startTime)
                + ";"
                +Time.writeTime(this.startTime%SECS_PER_DAY)
                + ";"
                +Double.toString(this.startTime)
                + ";"
                +Integer.toString((int) this.startTime/SECS_PER_DAY+1)
                + ";"
                +Time.writeTime(this.endTime)
                + ";"
                +Time.writeTime(this.endTime%SECS_PER_DAY)
                + ";"
                +Double.toString(this.endTime)
                + ";"
                +Integer.toString((int) this.endTime/SECS_PER_DAY+1)
                + ";"
                +Double.toString(this.chargingDuration)
                + ";"
                +Time.writeTime(this.unplugTime)
                + ";"
                +Time.writeTime(this.unplugTime%SECS_PER_DAY)
                + ";"
                +Double.toString(this.unplugTime)
                + ";"
                +Integer.toString((int) this.unplugTime/SECS_PER_DAY+1)
                + ";"
                +Double.toString(this.pluggedDuration)
                + ";"
                +Double.toString(Math.round(this.chargingRatio*1000.0)/1000.0)
                + ";"
                +Double.toString(Math.round(this.startSOC*1000.0)/1000.0)
                + ";"
                +Double.toString(Math.round(EvUnits.J_to_kWh(this.startSOC_J)*1000.0)/1000.0)
                + ";"
                +Double.toString(Math.round(this.endSOC*1000.0)/1000.0)
                + ";"
                +Double.toString(Math.round(EvUnits.J_to_kWh(this.endSOC_J)*1000.0)/1000.0)
                + ";"
                +Double.toString(Math.round(EvUnits.J_to_kWh(this.transmittedEnergy_J)*1000.0)/1000.0)
                + ";"
                + Double.toString(Math.round(this.getWalkingDistance()*1000.0)/1000.0);
    }

    public boolean isStartTimeSet() {
        return startTimeSet;
    }

    public boolean isEndTimeSet() {
        return endTimeSet;
    }

    public boolean isStartSOCSet() {
        return startSOCSet;
    }

    public boolean isEndSOCSet() {
        return endSOCSet;
    }

    public boolean isWalkingDistanceSet() {
        return walkingDistanceSet;
    }

    public boolean isChargingDurationSet() {
        return chargingDurationSet;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public double getUnplugTime() {
        return unplugTime;
    }

    public int getUnplugDay() {
        return unplugDay;
    }

    public boolean isUnplugTimeSet() {
        return unplugTimeSet;
    }
}
