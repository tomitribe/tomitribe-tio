package org.tomitribe.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JarLocation {
   private JarLocation() {
   }

   public static File get() {
      return jarLocation(JarLocation.class);
   }

   public static File jarLocation(Class clazz) {
      try {
         String classFileName = clazz.getName().replace(".", "/") + ".class";
         ClassLoader loader = clazz.getClassLoader();
         URL url;
         if (loader != null) {
            url = loader.getResource(classFileName);
         } else {
            url = clazz.getResource(classFileName);
         }

         if (url == null) {
            throw new IllegalStateException("classloader.getResource(classFileName) returned a null URL");
         } else if ("jar".equals(url.getProtocol())) {
            String spec = url.getFile();
            int separator = spec.indexOf(33);
            if (separator == -1) {
               throw new MalformedURLException("no ! found in jar url spec:" + spec);
            } else {
               url = new URL(spec.substring(0, separator++));
               return new File(decode(url.getFile()));
            }
         } else if ("file".equals(url.getProtocol())) {
            return toFile(classFileName, url);
         } else {
            throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
         }
      } catch (RuntimeException var6) {
         throw var6;
      } catch (Exception var7) {
         throw new IllegalStateException(var7);
      }
   }

   private static File toFile(String classFileName, URL url) {
      String path = url.getFile();
      path = path.substring(0, path.length() - classFileName.length());
      return new File(decode(path));
   }

   public static String decode(String fileName) {
      if (fileName.indexOf(37) == -1) {
         return fileName;
      } else {
         StringBuilder result = new StringBuilder(fileName.length());
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         int i = 0;

         label42:
         while (i < fileName.length()) {
            char c = fileName.charAt(i);
            if (c == '%') {
               out.reset();

               while (i + 2 < fileName.length()) {
                  int d1 = Character.digit(fileName.charAt(i + 1), 16);
                  int d2 = Character.digit(fileName.charAt(i + 2), 16);
                  if (d1 == -1 || d2 == -1) {
                     throw new IllegalArgumentException("Invalid % sequence (" + fileName.substring(i, i + 3) + ") at: " + i);
                  }

                  out.write((byte)((d1 << 4) + d2));
                  i += 3;
                  if (i >= fileName.length() || fileName.charAt(i) != '%') {
                     result.append(out.toString());
                     continue label42;
                  }
               }

               throw new IllegalArgumentException("Incomplete % sequence at: " + i);
            } else {
               result.append(c);
               i++;
            }
         }

         return result.toString();
      }
   }
}
