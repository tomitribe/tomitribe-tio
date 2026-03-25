package org.tomitribe.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class IO {
   private IO() {
   }

   public static Properties readProperties(URL resource) throws IOException {
      return readProperties(resource, new Properties());
   }

   public static Properties readProperties(URL resource, Properties properties) throws IOException {
      return readProperties(read(resource), properties);
   }

   public static Properties readProperties(File resource) throws IOException {
      return readProperties(resource, new Properties());
   }

   public static Properties readProperties(File resource, Properties properties) throws IOException {
      return readProperties(read(resource), properties);
   }

   public static Properties readProperties(InputStream in, Properties properties) throws IOException {
      if (in == null) {
         throw new NullPointerException("InputStream is null");
      } else if (properties == null) {
         throw new NullPointerException("Properties is null");
      } else {
         try {
            properties.load(in);
         } finally {
            close(in);
         }

         return properties;
      }
   }

   public static String readString(URL url) throws IOException {
      InputStream in = url.openStream();

      String var3;
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         var3 = reader.readLine();
      } finally {
         close(in);
      }

      return var3;
   }

   public static String readString(File file) throws IOException {
      FileReader in = new FileReader(file);

      String var3;
      try {
         BufferedReader reader = new BufferedReader(in);
         var3 = reader.readLine();
      } finally {
         close(in);
      }

      return var3;
   }

   public static byte[] readBytes(File file) throws IOException {
      InputStream in = read(file);

      byte[] var2;
      try {
         var2 = readBytes(in);
      } finally {
         close(in);
      }

      return var2;
   }

   public static byte[] readBytes(URL url) throws IOException {
      InputStream in = read(url);

      byte[] var2;
      try {
         var2 = readBytes(in);
      } finally {
         close(in);
      }

      return var2;
   }

   public static byte[] readBytes(InputStream in) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      copy(in, out);
      return out.toByteArray();
   }

   public static String slurp(File file) throws IOException {
      return slurp(read(file));
   }

   public static String slurp(URL url) throws IOException {
      return slurp(url.openStream());
   }

   public static String slurp(InputStream in) throws IOException {
      return new String(readBytes(in));
   }

   public static void writeString(File file, String string) throws IOException {
      FileWriter out = new FileWriter(file);

      try {
         BufferedWriter bufferedWriter = new BufferedWriter(out);

         try {
            bufferedWriter.write(string);
            bufferedWriter.newLine();
         } finally {
            close(bufferedWriter);
         }
      } finally {
         close(out);
      }
   }

   public static void copy(File from, File to) throws IOException {
      if (!from.isDirectory()) {
         FileOutputStream fos = new FileOutputStream(to);

         try {
            copy(from, fos);
         } finally {
            close(fos);
         }
      } else {
         copyDirectory(from, to);
      }
   }

   public static void copyDirectory(File srcDir, File destDir) throws IOException {
      if (srcDir == null) {
         throw new NullPointerException("Source must not be null");
      } else if (destDir == null) {
         throw new NullPointerException("Destination must not be null");
      } else if (!srcDir.exists()) {
         throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
      } else if (!srcDir.isDirectory()) {
         throw new IOException("Source '" + srcDir + "' exists but is not a directory");
      } else if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
         throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
      } else {
         List<String> exclusionList = null;
         if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            File[] srcFiles = srcDir.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
               exclusionList = new ArrayList(srcFiles.length);

               for (File srcFile : srcFiles) {
                  File copiedFile = new File(destDir, srcFile.getName());
                  exclusionList.add(copiedFile.getCanonicalPath());
               }
            }
         }

         doCopyDirectory(srcDir, destDir, exclusionList);
      }
   }

   private static void doCopyDirectory(File srcDir, File destDir, List<String> exclusionList) throws IOException {
      File[] files = srcDir.listFiles();
      if (files == null) {
         throw new IOException("Failed to list contents of " + srcDir);
      } else {
         if (destDir.exists()) {
            if (!destDir.isDirectory()) {
               throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
         } else if (!destDir.mkdirs()) {
            throw new IOException("Destination '" + destDir + "' directory cannot be created");
         }

         if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
         } else {
            for (File file : files) {
               File copiedFile = new File(destDir, file.getName());
               if (exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
                  if (file.isDirectory()) {
                     doCopyDirectory(file, copiedFile, exclusionList);
                  } else {
                     copy(file, copiedFile);
                  }
               }
            }
         }
      }
   }

   public static void copy(File from, OutputStream to) throws IOException {
      InputStream read = read(from);

      try {
         copy(read, to);
      } finally {
         close(read);
      }
   }

   public static void copy(URL from, OutputStream to) throws IOException {
      InputStream read = read(from);

      try {
         copy(read, to);
      } finally {
         close(read);
      }
   }

   public static void copy(InputStream from, File to) throws IOException {
      OutputStream write = write(to);

      try {
         copy(from, write);
      } finally {
         close(write);
      }
   }

   public static void copy(URL from, File to) throws IOException {
      OutputStream write = write(to);

      try {
         copy(from, write);
      } finally {
         close(write);
      }
   }

   public static void copy(InputStream from, File to, boolean append) throws IOException {
      OutputStream write = write(to, append);

      try {
         copy(from, write);
      } finally {
         close(write);
      }
   }

   public static void copy(InputStream from, OutputStream to) throws IOException {
      byte[] buffer = new byte[1024];

      int length;
      while ((length = from.read(buffer)) != -1) {
         to.write(buffer, 0, length);
      }

      to.flush();
   }

   public static void copy(byte[] from, File to) throws IOException {
      copy(new ByteArrayInputStream(from), to);
   }

   public static void copy(byte[] from, OutputStream to) throws IOException {
      copy(new ByteArrayInputStream(from), to);
   }

   public static ZipOutputStream zip(File file) throws IOException {
      OutputStream write = write(file);
      return new ZipOutputStream(write);
   }

   public static ZipInputStream unzip(File file) throws IOException {
      InputStream read = read(file);
      return new ZipInputStream(read);
   }

   public static void close(Closeable closeable) {
      if (closeable != null) {
         try {
            if (Flushable.class.isInstance(closeable)) {
               ((Flushable)closeable).flush();
            }
         } catch (Throwable var3) {
         }

         try {
            closeable.close();
         } catch (Throwable var2) {
         }
      }
   }

   public static boolean delete(File file) {
      if (file == null) {
         return false;
      } else if (!file.delete()) {
         Logger.getLogger(IO.class.getName()).log(Level.WARNING, "Delete failed on: " + file.getAbsolutePath());
         return false;
      } else {
         return true;
      }
   }

   public static OutputStream write(File destination) throws FileNotFoundException {
      OutputStream out = new FileOutputStream(destination);
      return new BufferedOutputStream(out, 32768);
   }

   public static OutputStream write(File destination, boolean append) throws FileNotFoundException {
      OutputStream out = new FileOutputStream(destination, append);
      return new BufferedOutputStream(out, 32768);
   }

   public static PrintStream print(File destination, boolean append) throws FileNotFoundException {
      return print(write(destination, append));
   }

   public static PrintStream print(File destination) throws FileNotFoundException {
      return print(write(destination));
   }

   public static PrintStream print(OutputStream out) {
      return new PrintStream(out);
   }

   public static InputStream read(File source) throws FileNotFoundException {
      InputStream in = new FileInputStream(source);
      return new BufferedInputStream(in, 32768);
   }

   public static InputStream read(String content) {
      return read(content.getBytes());
   }

   public static InputStream read(String content, String encoding) throws UnsupportedEncodingException {
      return read(content.getBytes(encoding));
   }

   public static InputStream read(byte[] content) {
      return new ByteArrayInputStream(content);
   }

   public static InputStream read(URL url) throws IOException {
      return url.openStream();
   }

   public static Iterable<String> readLines(File file) throws FileNotFoundException {
      return readLines(read(file));
   }

   public static Iterable<String> readLines(InputStream inputStream) {
      return readLines(new BufferedReader(new InputStreamReader(inputStream)));
   }

   public static Iterable<String> readLines(BufferedReader reader) {
      return new IO.BufferedReaderIterable(reader);
   }

   public static void copyNIO(InputStream in, OutputStream out) throws IOException {
      ReadableByteChannel ic = Channels.newChannel(in);
      WritableByteChannel oc = Channels.newChannel(out);

      try {
         copy(ic, oc);
      } finally {
         ic.close();
         oc.close();
      }
   }

   public static void copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

      while (in.read(buffer) != -1) {
         buffer.flip();
         out.write(buffer);
         buffer.compact();
      }

      buffer.flip();

      while (buffer.hasRemaining()) {
         out.write(buffer);
      }
   }

   private static class BufferedReaderIterable implements Iterable<String> {
      private final BufferedReader reader;

      private BufferedReaderIterable(BufferedReader reader) {
         this.reader = reader;
      }

      public Iterator<String> iterator() {
         return new IO.BufferedReaderIterable.BufferedReaderIterator();
      }

      private class BufferedReaderIterator implements Iterator<String> {
         private String line;

         private BufferedReaderIterator() {
         }

         public boolean hasNext() {
            try {
               boolean hasNext = (this.line = BufferedReaderIterable.this.reader.readLine()) != null;
               if (!hasNext) {
                  IO.close(BufferedReaderIterable.this.reader);
               }

               return hasNext;
            } catch (IOException var2) {
               throw new IllegalStateException(var2);
            }
         }

         public String next() {
            return this.line;
         }

         public void remove() {
            throw new UnsupportedOperationException("remove not supported");
         }
      }
   }
}
