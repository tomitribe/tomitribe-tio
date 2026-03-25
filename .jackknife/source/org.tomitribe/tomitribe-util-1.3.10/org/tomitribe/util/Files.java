package org.tomitribe.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.tomitribe.util.collect.AbstractIterator;
import org.tomitribe.util.collect.FilteredIterator;

public class Files {
   private static final Files.DeleteOnExit DELETE_ON_EXIT = new Files.DeleteOnExit();
   public static final FileFilter ALL = new FileFilter() {
      public boolean accept(File file) {
         return true;
      }
   };

   private Files() {
   }

   public static File file(String... parts) {
      File dir = null;

      for (String part : parts) {
         if (dir == null) {
            dir = new File(part);
         } else {
            dir = new File(dir, part);
         }
      }

      return dir;
   }

   public static File file(File dir, String... parts) {
      for (String part : parts) {
         dir = new File(dir, part);
      }

      return dir;
   }

   public static List<File> collect(File dir) {
      return collect(dir, ALL);
   }

   public static List<File> collect(File dir, String regex) {
      return collect(dir, Pattern.compile(regex));
   }

   public static List<File> collect(File dir, Pattern pattern) {
      return collect(dir, new Files.PatternFileFilter(pattern));
   }

   public static boolean visit(File dir, Files.Visitor visitor) {
      return visit(dir, ALL, visitor);
   }

   public static boolean visit(File dir, String regex, Files.Visitor visitor) {
      return visit(dir, Pattern.compile(regex), visitor);
   }

   public static boolean visit(File dir, Pattern pattern, final Files.Visitor visitor) {
      final Files.PatternFileFilter patternFileFilter = new Files.PatternFileFilter(pattern);
      return visit(dir, new FileFilter() {
         public boolean accept(File file) {
            return true;
         }
      }, new Files.Visitor() {
         @Override
         public boolean visit(File file) {
            if (file.isFile() && patternFileFilter.accept(file)) {
               visitor.visit(file);
            }

            return true;
         }
      });
   }

   public static Iterable<File> iterate(File dir) {
      return iterate(dir, ALL);
   }

   public static Iterable<File> iterate(File dir, String regex) {
      return iterate(dir, Pattern.compile(regex));
   }

   public static Iterable<File> iterate(File dir, Pattern pattern) {
      return iterate(dir, new Files.PatternFileFilter(pattern));
   }

   public static List<File> collect(File dir, FileFilter filter) {
      List<File> accepted = new ArrayList();
      File[] files = dir.listFiles();
      if (files != null) {
         for (File file : files) {
            if (filter.accept(file)) {
               accepted.add(file);
            }

            accepted.addAll(collect(file, filter));
         }
      }

      return accepted;
   }

   public static Iterable<File> iterate(final File dir, final FileFilter filter) {
      return new Iterable<File>() {
         public Iterator<File> iterator() {
            return new FilteredIterator<>(new Files.RecursiveFileIterator(dir), new FilteredIterator.Filter<File>() {
               public boolean accept(File file) {
                  return filter.accept(file);
               }
            });
         }
      };
   }

   public static boolean visit(File dir, FileFilter filter, Files.Visitor visitor) {
      File[] files = dir.listFiles();
      if (files != null) {
         for (File file : files) {
            if (!filter.accept(file)) {
               return false;
            }

            if (!visitor.visit(file)) {
               return false;
            }

            if (!visit(file, filter, visitor)) {
               return false;
            }
         }
      }

      return true;
   }

   public static void exists(File file, String s) {
      if (!file.exists()) {
         throw new IllegalStateException(s + " does not exist: " + file.getAbsolutePath());
      }
   }

   public static void exists(File file) {
      exists(file, "File");
   }

   public static void dir(File file) {
      if (!file.isDirectory()) {
         throw new IllegalStateException("Not a directory: " + file.getAbsolutePath());
      }
   }

   public static void file(File file) {
      if (!file.isFile()) {
         throw new IllegalStateException("Not a file: " + file.getAbsolutePath());
      }
   }

   public static void writable(File file) {
      if (!file.canWrite()) {
         throw new IllegalStateException("Not writable: " + file.getAbsolutePath());
      }
   }

   public static void readable(File file) {
      if (!file.canRead()) {
         throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
      }
   }

   public static File rename(File from, File to) {
      if (!from.renameTo(to)) {
         throw new IllegalStateException("Could not rename " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
      } else {
         return to;
      }
   }

   public static void remove(File file) {
      if (file != null) {
         if (file.exists()) {
            if (file.isDirectory()) {
               delete(file);
            } else if (!file.delete()) {
               throw new IllegalStateException("Could not delete file: " + file.getAbsolutePath());
            }
         }
      }
   }

   private static void delete(File file) {
      SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
         public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            java.nio.file.Files.delete(dir);
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            java.nio.file.Files.delete(file);
            return FileVisitResult.CONTINUE;
         }
      };

      try {
         java.nio.file.Files.walkFileTree(file.toPath(), visitor);
      } catch (IOException var3) {
         throw new IllegalStateException("Could not delete directory: " + file.getAbsolutePath(), var3);
      }
   }

   public static void mkdir(File file) {
      if (file.exists()) {
         dir(file);
      } else if (!file.mkdir()) {
         throw new RuntimeException("Cannot mkdir: " + file.getAbsolutePath());
      }
   }

   public static File tmpdir() {
      try {
         File file = File.createTempFile("temp", "dir");
         if (!file.delete()) {
            throw new IllegalStateException("Cannot make temp dir.  Delete failed");
         } else {
            mkdir(file);
            DELETE_ON_EXIT.clean(file);
            return file;
         }
      } catch (IOException var1) {
         throw new RuntimeException(var1);
      }
   }

   public static void mkparent(File file) {
      mkdirs(file.getParentFile());
   }

   public static File mkparent(File dir, String... parts) {
      File file = file(dir, parts);
      mkparent(file);
      return file;
   }

   public static File mkdirs(File dir, String... parts) {
      File file = file(dir, parts);
      if (!file.exists()) {
         if (!file.mkdirs()) {
            throw new RuntimeException("Cannot mkdirs: " + file.getAbsolutePath());
         }
      } else {
         dir(file);
      }

      return file;
   }

   public static File resolve(File absolutePath, File path) {
      if (path == null) {
         throw new IllegalArgumentException("path is null");
      } else if (path.isAbsolute()) {
         return path;
      } else if (absolutePath == null) {
         throw new IllegalArgumentException("absolutePath is null");
      } else {
         absolute(absolutePath);
         return new File(absolutePath, path.getPath());
      }
   }

   public static void absolute(File path) {
      if (!path.isAbsolute()) {
         throw new IllegalArgumentException("absolutePath is not absolute: " + path.getPath());
      }
   }

   public static String format(double size) {
      if (size < 1024.0) {
         return String.format("%.0f B", size);
      } else if ((size = size / 1024.0) < 1024.0) {
         return String.format("%.0f KB", size);
      } else if ((size = size / 1024.0) < 1024.0) {
         return String.format("%.0f MB", size);
      } else if ((size = size / 1024.0) < 1024.0) {
         return String.format("%.1f GB", size);
      } else {
         double var5;
         return (var5 = size / 1024.0) < 1024.0 ? String.format("%.1f TB", var5) : "unknown";
      }
   }

   public static class DeleteOnExit {
      private final List<File> files = new ArrayList();

      public DeleteOnExit() {
         Runtime.getRuntime().addShutdownHook(new Thread(this::clean));
      }

      public File clean(File file) {
         this.files.add(file);
         return file;
      }

      public void clean() {
         this.files.stream().forEach(this::delete);
      }

      private void delete(File file) {
         try {
            java.nio.file.Files.walkFileTree(file.toPath(), new Files.DeleteOnExit.RecursiveDelete());
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      }

      private static class RecursiveDelete implements FileVisitor<Path> {
         private RecursiveDelete() {
         }

         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            java.nio.file.Files.deleteIfExists(file);
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            java.nio.file.Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
         }
      }
   }

   private static class FileIterator extends AbstractIterator<File> {
      private final File[] files;
      private int index;

      private FileIterator(File dir) {
         Files.dir(dir);
         this.files = dir.listFiles();
         this.index = 0;
      }

      protected File advance() throws NoSuchElementException {
         return this.index >= this.files.length ? null : this.files[this.index++];
      }
   }

   private static class PatternFileFilter implements FileFilter {
      private final Pattern pattern;

      public PatternFileFilter(Pattern pattern) {
         this.pattern = pattern;
      }

      public boolean accept(File file) {
         return this.pattern.matcher(file.getAbsolutePath()).matches();
      }
   }

   private static class RecursiveFileIterator extends AbstractIterator<File> {
      private final LinkedList<Files.FileIterator> stack = new LinkedList();

      public RecursiveFileIterator(File base) {
         this.stack.add(new Files.FileIterator(base));
      }

      protected File advance() throws NoSuchElementException {
         Files.FileIterator current = (Files.FileIterator)this.stack.element();

         try {
            File file = current.advance();
            if (file == null) {
               this.stack.pop();
               return this.advance();
            } else {
               if (file.isDirectory()) {
                  this.stack.push(new Files.FileIterator(file));
               }

               return file;
            }
         } catch (NoSuchElementException var3) {
            this.stack.pop();
            return this.advance();
         }
      }
   }

   public interface Visitor {
      boolean visit(File var1);
   }
}
