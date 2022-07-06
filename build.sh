#!/bin/bash

set -euo pipefail

javac DuplicateClasses.java
rm -rf DuplicateClasses.jar
jar cvfe DuplicateClasses.jar DuplicateClasses *.class