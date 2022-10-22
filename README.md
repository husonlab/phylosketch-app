# PhyloSketch

This is a re-implementation of my PhyloSketch desktop program that will target iOS and Android. This is my first attempt to write such an app.

The program allows the user to interactively construct a phylogenetic tree or network and then save it to Newick format.

The basic framework for this code was generated using
the [Gluon Intellij plugin](https://plugins.jetbrains.com/plugin/7864-gluon).

## Basic Requirements

A list of the basic requirements can be found online in the [Gluon documentation](https://docs.gluonhq.com/#_requirements).

## Quick instructions

### Run the sample on JVM/HotSpot:

    mvn gluonfx:run

### Run the sample as a native image:

    mvn gluonfx:build gluonfx:nativerun

### Run the sample on an iOS simulator:

    mvn -Pios-sim gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

### Run the sample as a native iOS image:

Before you can run on a native iOS image on an iOS device, there are several configuration steps that one must go
through, involving xCode and "provisioning", described [here](https://docs.gluonhq.com/#platforms_ios).

    mvn -Pios gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

### Run the sample as a native android image:

    mvn -Pandroid gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

## Important:

When adding controls to an FXML file, remember to add the corresponding classes to the reflectionList in the pom.xml
file. If you don't, then the program might fail with an exception indicating that "type coercion failed", or some other
problem.

## Selected features

This is a list of all the features that were selected when creating the sample:

### JavaFX 18.0.2 Modules

 - javafx-base
 - javafx-graphics
 - javafx-controls

### Gluon Features

 - Glisten: build platform independent user interfaces
 - Attach display
 - Attach lifecycle
 - Attach statusbar
 - Attach storage
