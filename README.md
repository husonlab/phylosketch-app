# PhyloSketch App

This is going to be a reimplementation of my [PhyloSketch desktop program](https://uni-tuebingen.de/fakultaeten/mathematisch-naturwissenschaftliche-fakultaet/fachbereiche/informatik/lehrstuehle/algorithms-in-bioinformatics/software/phylosketch/) that will target iOS and Android.
This is my first attempt to write an app for mobile devices.

The app allows the user to interactively construct a phylogenetic tree or network and then save it to Newick format. Also, it will allow the user to enter a string in Newick format and obtain a visualization of the corresponding rooted phylogenetic tree or network, which then can be edited. The app will display some of the mathematical properties of the tree or network.

The basic framework for this code was generated using
the [Gluon Intellij plugin](https://plugins.jetbrains.com/plugin/7864-gluon).

This readme is not intended for the end-user of the app, but rather outlines issues associated with implementing a Java+JavaFX using the Gluon framework that targets iOS and Android.

## Basic Requirements

A list of the basic requirements can be found online in
the [Gluon documentation](https://docs.gluonhq.com/#_requirements).

### JLODA library required

This project requires the jloda library to be present, available [here](https://github.com/husonlab/jloda2). As jloda is
not available via maven, you need to build jloda.jar from source and then place it in your local maven repository.

## Quick instructions for working with the code in Intellij

You can run the project from within Intellij as usual. This is the best way to do basic development and debugging. To
test different aspects of the code running as a mobile app, you can type one of the following maven commands into the
Intellij terminal window. The commands that generate a native image for a mobile device take much longer to complete.

### Run the program on JVM/HotSpot:

    mvn gluonfx:run

### Run the program as a native image:

    mvn gluonfx:build gluonfx:nativerun

This doesn't work for me on MacOS X, I get this exception:

    *** Assertion failure in -[_NSTrackingAreaAKViewHelper removeTrackingRect:], _NSTrackingAreaAKManager.m:1602

### Run the program on an iOS simulator:

First, you need to launch the iOS simulator from the Tools menu of XCode. Then the following maven command will install
and run the app in the simulator:

    mvn -Pios-sim gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

Adding code for printing breaks this.

### Run the program as a native iOS image:

Before you can run on a native iOS image on an iOS device, there are several configuration steps that one must go
through, involving setting up a corresponding xCode project and setting up "provisioning",
described [here](https://docs.gluonhq.com/#platforms_ios). It may be that you require an Apple Developer account for
this to work.

    mvn -Pios gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

### Run the program as a native Android image:

Unfortunately, I have not got this to work yet...

    mvn -Pandroid gluonfx:build gluonfx:package gluonfx:install gluonfx:nativerun

### Use this command to list values of maven properties:

    mvn help:evaluate -Dexpression=project.properties

## Important advice for when programming:

When adding controls to an FXML file, remember to add the corresponding classes to the reflectionList in the pom.xml
file. If you don't, then the program might fail with an exception indicating that "type coercion failed", or some other
problem.

## Selected features

This is a list of all the features that were selected when creating the project using the Gluon plugin:

### JavaFX Modules

 - javafx-base
 - javafx-graphics
 - javafx-controls

### Gluon Features

 - Glisten: build platform independent user interfaces
 - Attach display
 - Attach lifecycle
 - Attach statusbar
 - Attach storage
