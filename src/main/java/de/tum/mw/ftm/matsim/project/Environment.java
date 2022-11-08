package de.tum.mw.ftm.matsim.project;


import org.apache.log4j.Logger;

class Environment {

    private static final Logger log = Logger.getLogger(RunMATSimUrbanEV.class);
    private Environment() {
    }

    public static String getMatsimInputPath() {
        return getEnvVar("MATSIM_INPUT");
    }

    public static String getMatsimOutputPath() {
        return getEnvVar("MATSIM_OUTPUT");
    }

    public static String getMatsimVersion() {
        return getEnvVar("MATSIM_VERSION");
    }

    private static String getEnvVar(String name) {
        String value = System.getenv(name);
        //log.info("Getting environment variable {}: {}.", name, value);
        return value;
    }
}
