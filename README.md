# UrbanEV

UrbanEV is a simulation framework for urban electromobility based on the MATSim transport simulation framework 
(https://github.com/matsim-org) and its electric vehicle contribution (https://github.com/matsim-org/matsim-libs/tree/master/contribs/ev).

## Project Structure
Directories are structured as follows:
* `src` for sources
* `scenarios` for MATSim scenarios, i.e. MATSim input and output data.
  * One subdirectory for each scenario:
    * `scenarios/munich` (examplary main scenario with different population sizes).
  * Each scenario needs to comprise the following files:
    * config file
    * network file
    * population file
    * chargers file
    * electric vehicles file
  * The output files will usually be created one level deeper, e.g. `scenarios/munich/1000/output/...`.

## Requirements

The following requirements are to be met to build and execute matsim-urban-ev:

* maven
* OpenJDK 11
```console
sudo apt-get install openjdk-11-jdk
```  

## Dependencies

All dependencies are managed by maven. Therefore, dependencies are automatically installed during deployment.

## Run from console

1. clone repo into directory
```console
git clone https://github.com/TUMFTM/urbanev
```  
2. make jar file to run
```console
sudo mvn clean install
```  
3. run (arg: config file path):
```console
sudo java -Xms16g -Xmx16g -jar target/urban_ev-0.1-jar-with-dependencies.jar scenarios/munich/1000/config.xml
```

## Contributing and Support

For contributing to the code please contact:  

Lennart Adenaw  
Chair of Automotive Technology  
Technical University of Munich  
  
mail: lennart.adenaw@tum.de

## Versioning

V0.1 

## Authors

Lennart Adenaw, Julius Drosten, Fabian Schuhmann

## License

This project is licensed under the GPL License - see the LICENSE.md file for details. The project is built upon contributions by the MATSim community which are themselves licensed under the GPL License. Files that were directly adapted from such projects contain modification notices and the original license texts. 

## Associated Article

Please also check the associated article available in the World Electric Vehicle Journal:

Adenaw, L.; Lienkamp, M. Multi-Criteria, Co-Evolutionary Charging Behavior: An Agent-Based Simulation of Urban Electromobility. *World Electr. Veh. J.* **2021**, *12*, 18. https://doi.org/10.3390/wevj12010018 
