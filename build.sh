#!/bin/bash

set -euo pipefail

javac -source 1.8 -target 1.8 DuplicateClasses.java
rm -rf DuplicateClasses.jar
jar cvfe DuplicateClasses.jar DuplicateClasses *.class
