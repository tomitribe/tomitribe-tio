package org.tomitribe.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Archive {
   private final Map<String, String> manifest = new HashMap();
   private final Map<String, Supplier<byte[]>> entries = new HashMap();

   public static Archive archive() {
      return new Archive();
   }

   public Archive manifest(String key, Object value) {
      this.manifest.put(key, value.toString());
      return this;
   }

   public Archive manifest(String key, Class value) {
      this.manifest.put(key, value.getName());
      return this;
   }

   public Archive add(String name, byte[] bytes) {
      this.entries.put(name, (Supplier)() -> bytes);
      return this;
   }

   public Archive add(String name, Supplier<byte[]> content) {
      this.entries.put(name, content);
      return this;
   }

   public Archive add(String name, String content) {
      return this.add(name, content::getBytes);
   }

   public Archive add(String name, File content) {
      return content.isDirectory() ? this.addDir(name, content) : this.add(name, (Supplier<byte[]>)(() -> readBytes(content)));
   }

   public Archive add(String name, Archive archive) {
      this.manifest.putAll(archive.manifest);

      for (Entry<String, Supplier<byte[]>> entry : archive.entries.entrySet()) {
         this.entries.put(name + "/" + (String)entry.getKey(), entry.getValue());
      }

      return this;
   }

   public static byte[] readBytes(File content) {
      try {
         return IO.readBytes(content);
      } catch (IOException var2) {
         throw new IllegalStateException(var2);
      }
   }

   public static byte[] readBytes(URL content) {
      try {
         return IO.readBytes(content);
      } catch (IOException var2) {
         throw new IllegalStateException(var2);
      }
   }

   public Archive add(String name, URL content) throws IOException {
      return this.add(name, IO.readBytes(content));
   }

   public Archive add(Class<?> clazz) {
      try {
         String name = clazz.getName().replace('.', '/') + ".class";
         URL resource = this.getClass().getClassLoader().getResource(name);
         if (resource == null) {
            throw new IllegalStateException("Cannot find class file for " + clazz.getName());
         } else {
            this.add(name, resource);
            if (!clazz.isAnonymousClass() && clazz.getDeclaringClass() != null) {
               this.add(clazz.getDeclaringClass());
            }

            Stream.of(clazz.getDeclaredClasses()).filter(Class::isAnonymousClass).forEach(this::add);
            return this;
         }
      } catch (IOException var4) {
         throw new IllegalStateException(var4);
      }
   }

   public Archive addDir(File dir) {
      return this.addDir(null, dir);
   }

   private Archive addDir(String path, File dir) {
      for (File file : dir.listFiles()) {
         String childPath = path != null ? path + "/" + file.getName() : file.getName();
         if (file.isFile()) {
            this.entries.put(childPath, (Supplier)() -> readBytes(file));
         } else {
            this.addDir(childPath, file);
         }
      }

      return this;
   }

   public Archive addJar(File file) {
      try {
         JarFile jarFile = new JarFile(file);
         Enumeration<JarEntry> entries = jarFile.entries();

         while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry)entries.nextElement();
            byte[] bytes = IO.readBytes(jarFile.getInputStream(entry));
            this.entries.put(entry.getName(), (Supplier)() -> bytes);
         }

         return this;
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      }
   }

   public File toJar() throws IOException {
      File file = File.createTempFile("archive-", ".jar");
      file.deleteOnExit();
      return this.toJar(file);
   }

   public File toJar(File file) throws IOException {
      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

      for (Entry<String, Supplier<byte[]>> entry : this.entries().entrySet()) {
         out.putNextEntry(new ZipEntry((String)entry.getKey()));
         out.write((byte[])((Supplier)entry.getValue()).get());
      }

      out.close();
      return file;
   }

   public File asJar() {
      try {
         return this.toJar();
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   public File toDir() throws IOException {
      File classpath = Files.tmpdir();
      this.toDir(classpath);
      return classpath;
   }

   public void toDir(File dir) throws IOException {
      Files.exists(dir);
      Files.dir(dir);
      Files.writable(dir);

      for (Entry<String, Supplier<byte[]>> entry : this.entries().entrySet()) {
         String key = ((String)entry.getKey()).replace('/', File.separatorChar);
         File file = new File(dir, key);
         Files.mkparent(file);

         try {
            IO.copy((byte[])((Supplier)entry.getValue()).get(), file);
         } catch (Exception var7) {
            throw new IllegalStateException("Cannot write entry " + (String)entry.getKey(), var7);
         }
      }
   }

   public File asDir() {
      try {
         return this.toDir();
      } catch (IOException var2) {
         throw new RuntimeException(var2);
      }
   }

   private HashMap<String, Supplier<byte[]>> entries() {
      HashMap<String, Supplier<byte[]>> entries = new HashMap(this.entries);
      if (this.manifest.size() > 0) {
         entries.put("META-INF/MANIFEST.MF", this.buildManifest()::getBytes);
      }

      return entries;
   }

   private String buildManifest() {
      return Join.join("\r\n", entry -> (String)entry.getKey() + ": " + (String)entry.getValue(), this.manifest.entrySet());
   }
}
