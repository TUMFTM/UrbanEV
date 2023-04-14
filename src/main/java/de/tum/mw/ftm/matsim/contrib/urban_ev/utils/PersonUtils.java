package de.tum.mw.ftm.matsim.contrib.urban_ev.utils;

import org.matsim.api.core.v01.population.Person;

public class PersonUtils {
    
    public static final String CRITICAL_SOC_IDENTIFIER = "criticalSOC";
    public static final String NON_CRITICAL_SOC_IDENTIFIER = "nonCriticalSOC";
    public static final String HOME_CHARGER_POWER_ATTR = "homeChargerPower";
    public static final String WORK_CHARGER_POWER_ATTR = "workChargerPower";
    public static final String SUBPOPULATION_ATTR = "subpopulation";

    public static boolean hasAttr(Person person, String attr)
    {
        return person.getAttributes().getAttribute(attr) != null;
    }

    public static Object getAttr(Person person, String attr)
    {
        return person.getAttributes().getAttribute(attr);
    }

    public static void setAttr(Person person, String attr, Object value)
    {
        person.getAttributes().putAttribute(attr, value);
    }

    public static boolean isCritical(Person person)
    {
        return getAttr(person, SUBPOPULATION_ATTR).toString().equals(CRITICAL_SOC_IDENTIFIER);
    }

    public static void setCritical(Person person)
    {
        setAttr(person, SUBPOPULATION_ATTR, CRITICAL_SOC_IDENTIFIER);
    }

    public static void setNonCritical(Person person)
    {
        setAttr(person, SUBPOPULATION_ATTR, NON_CRITICAL_SOC_IDENTIFIER);
    }

    public static double getHomeChargerPower(Person person)
    {
        double homeChargerPower = hasAttr(person, HOME_CHARGER_POWER_ATTR) ? ((Double) getAttr(person, HOME_CHARGER_POWER_ATTR)).doubleValue() : 0.0;
        return homeChargerPower;
    }

    public static double getWorkChargerPower(Person person)
    {
        double workChargerPower = hasAttr(person, WORK_CHARGER_POWER_ATTR) ? ((Double) getAttr(person, WORK_CHARGER_POWER_ATTR)).doubleValue() : 0.0;
        return workChargerPower;
    }

    public static boolean hasHomeCharger(Person person)
    {
        return getHomeChargerPower(person) > 0.0;
    }

    public static boolean hasWorkCharger(Person person)
    {
        return getWorkChargerPower(person) > 0.0;
    }

    public static boolean hasPrivateCharger(Person person)
    {
        return hasHomeCharger(person)||hasWorkCharger(person);
    }

}
