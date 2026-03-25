package org.tomitribe.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zips {
   private Zips() {
   }

   public static void unzip(File zipFile, File destination) throws IOException {
      unzip(zipFile, destination, false);
   }

   public static void unzip(File zipFile, File destination, boolean noparent) throws IOException {
      Files.dir(destination);
      Files.writable(destination);
      Files.file(zipFile);
      Files.readable(zipFile);
      InputStream read = IO.read(zipFile);

      try {
         unzip(read, destination, noparent);
      } finally {
         IO.close(read);
      }
   }

   public static void unzip(InputStream read, File destination, boolean noparent) throws IOException {
      try {
         ZipInputStream e = new ZipInputStream(read);

         ZipEntry entry;
         while ((entry = e.getNextEntry()) != null) {
            String path = entry.getName();
            if (noparent) {
               path = path.replaceFirst("^[^/]+/", "");
            }

            File file = new File(destination, path);
            if (entry.isDirectory()) {
               Files.mkdir(file);
            } else {
               Files.mkdir(file.getParentFile());
               IO.copy(e, file);
               long lastModified = entry.getTime();
               if (lastModified > 0L) {
                  file.setLastModified(lastModified);
               }
            }
         }

         e.close();
      } catch (IOException var91) {
         throw new IOException("Unable to unzip " + read, var91);
      }
   }
}
