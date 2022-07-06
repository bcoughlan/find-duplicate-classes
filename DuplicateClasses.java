import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class DuplicateClasses {

  public static class JarEntry {
    public final String jarPath;
    public final String className;
    public final long size;

    public JarEntry(String jarPath, String className, long size) {
      this.jarPath = jarPath;
      this.className = className;
      this.size = size;
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: DuplicateClasses <path to JAR files>");
      System.exit(1);
    }
    run(args[0]);
  }

  public static void run(String jarsDir) {
    Map<String, List<JarEntry>> classToJars = getJarEntriesByClassName(jarsDir);
    Collection<List<JarEntry>> byJarPaths = groupByJarPaths(classToJars);

    for (List<JarEntry> entry : byJarPaths) {
      printEntry(jarsDir, entry);
    }

    System.out.println("\n");
    System.out.println("Found " + classToJars.values().stream().filter(e -> e.size() > 1).count() + " duplicated classes");
  }

  private static Map<String, List<JarEntry>> getJarEntriesByClassName(String jarsDir) {
    Map<String, List<JarEntry>> classToJars = new TreeMap<>();
    List<File> jars = findJars(new File(jarsDir));
    for (File jar : jars) {
      try (ZipFile zipFile = new ZipFile(jar)) {
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = (ZipEntry) entries.nextElement();

          String name = entry.getName();
          if (name.endsWith(".class") && !name.equals("module-info.class") && !name.startsWith("META-INF/")) {
            String className = entry.getName().replaceAll("/", ".");
            final String classNameTrimmed = className.substring(0, className.length() - ".class".length());
            classToJars.computeIfAbsent(classNameTrimmed, k -> new ArrayList<>())
                .add(new DuplicateClasses.JarEntry(jar.getAbsolutePath(), classNameTrimmed, entry.getSize()));
          }
        }
      } catch (Exception e) {
        System.err.println("Error reading " + jar.getAbsolutePath());
        e.printStackTrace();
      }
    }
    return classToJars;
  }

  private static void printEntry(String path, List<JarEntry> entry) {
    Set<String> jarPaths = new TreeSet<>();
    for (JarEntry jarEntry : entry) {
      jarPaths.add(jarEntry.jarPath);
    }

    Set<String> classes = new TreeSet<>();
    for (JarEntry jarEntry : entry) {
      classes.add(jarEntry.className);
    }

    System.out.println("\n============================================================");
    System.out.println("Duplicate classes in JARs:");
    for (String jarPath : jarPaths) {
      if (Paths.get(path).isAbsolute()) {
        System.out.println("  " + jarPath);
      } else {
        System.out.println("  " + new File("./").toURI().relativize(new File(jarPath).toURI()));
      }
    }

    System.out.println("Classes:");
    for (String className : classes) {
      System.out.println("  " + className + (allSizesAreEqual(className, entry) ? "" : " (* different sizes)"));
    }
  }

  private static Collection<List<JarEntry>> groupByJarPaths(Map<String, List<JarEntry>> classToJars) {
    Map<String, List<JarEntry>> byJarPaths = new TreeMap<>();
    for (List<JarEntry> entry : classToJars.values()) {
      if (entry.size() > 1) {
        String jarPaths = entry.stream().map(e -> e.jarPath).sorted().collect(Collectors.joining(","));
        byJarPaths.computeIfAbsent(jarPaths, k -> new ArrayList<>()).addAll(entry);
      }
    }
    return byJarPaths.values();
  }

  private static boolean allSizesAreEqual(String className, List<JarEntry> entry) {
    Long size = null;
    for (JarEntry jarEntry : entry) {
      if (jarEntry.className.equals(className)) {
        if (size == null) {
          size = jarEntry.size;
        } else if (!size.equals(jarEntry.size)) {
          return false;
        }
      }
    }
    return true;
  }

  private static List<File> findJars(File file) {
    List<File> jars = new ArrayList<>();
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        jars.addAll(findJars(f));
      }
    } else if (file.getName().endsWith(".jar")) {
      jars.add(file);
    }
    return jars;
  }
}