# find-duplicate-classes

Recursively scan a directory of JAR files to find duplicate classes.

```
wget https://github.com/bcoughlan/find-duplicate-classes/raw/main/DuplicateClasses.jar
java -jar DuplicateClasses.jar /path/to/jars
```

# Limitations

- Doesn't process JARs within JARs.
- Doesn't account for JPMS modules.

PRs welcome!
