package org.tomitribe.util;

import java.io.File;

public class Mvn {
   private Mvn() {
   }

   public static File mvn(String coordinates) {
      String[] parts = coordinates.split(":");
      if (parts.length == 5) {
         String group = parts[0];
         String artifact = parts[1];
         String packaging = parts[2];
         String classifier = parts[3];
         String version = parts[4];
         return mvn(group, artifact, version, packaging, classifier);
      } else if (parts.length == 4) {
         String group = parts[0];
         String artifact = parts[1];
         String packaging = parts[2];
         String version = parts[3];
         return mvn(group, artifact, version, packaging);
      } else {
         throw new IllegalArgumentException("Unsupported coordinates (GAV): " + coordinates);
      }
   }

   public static File mvn(String group, String artifact, String version, String packaging) {
      File repository = repository();
      File archive = Files.file(repository, group.replace('.', '/'), artifact, version, String.format("%s-%s.%s", artifact, version, packaging));
      Files.exists(archive);
      Files.file(archive);
      Files.readable(archive);
      return archive;
   }

   public static File mvn(String group, String artifact, String version, String packaging, String classifier) {
      File repository = repository();
      File archive = Files.file(repository, group.replace('.', '/'), artifact, version, String.format("%s-%s-%s.%s", artifact, version, classifier, packaging));
      Files.exists(archive);
      Files.file(archive);
      Files.readable(archive);
      return archive;
   }

   private static File repository() {
      File file = JarLocation.jarLocation(Mvn.class);

      while ((file = file.getParentFile()) != null) {
         if (file.getName().equals("org")) {
            return file.getParentFile();
         }

         if (file.getName().equals("repository")) {
            return file;
         }
      }

      throw new IllegalStateException("Unable to find maven.repo.local directory");
   }
}
