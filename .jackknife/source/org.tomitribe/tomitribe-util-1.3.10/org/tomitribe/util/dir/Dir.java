package org.tomitribe.util.dir;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.tomitribe.util.reflect.Generics;

public interface Dir {
   File dir();

   Dir dir(String var1);

   File mkdir();

   File mkdirs();

   File get();

   File parent();

   File file();

   File file(String var1);

   Stream<File> walk();

   Stream<File> walk(int var1);

   Stream<File> files();

   Stream<File> files(int var1);

   void delete();

   static <T> T of(Class<T> clazz, File file) {
      return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new Dir.DirHandler(file));
   }

   public static class DirHandler implements InvocationHandler {
      private final File dir;

      public DirHandler(File dir) {
         this.dir = dir;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         if (method.isDefault()) {
            return invokeDefault(proxy, method, args);
         } else {
            if (method.getDeclaringClass().equals(Object.class)) {
               if (method.getName().equals("toString")) {
                  return this.toString();
               }

               if (method.getName().equals("equals")) {
                  return this.equals(proxy, args);
               }

               if (method.getName().equals("hashCode")) {
                  return this.hashCode();
               }
            }

            if (method.getDeclaringClass().equals(Dir.class)) {
               if (method.getName().equals("dir")) {
                  return this.dir(args);
               } else if (method.getName().equals("get")) {
                  return this.dir;
               } else if (method.getName().equals("parent")) {
                  return this.dir.getParentFile();
               } else if (method.getName().equals("mkdir")) {
                  return this.mkdir();
               } else if (method.getName().equals("mkdirs")) {
                  return this.mkdirs();
               } else if (method.getName().equals("delete")) {
                  return this.delete();
               } else if (method.getName().equals("file")) {
                  return this.file(args);
               } else if (method.getName().equals("walk")) {
                  return this.walk(args);
               } else if (method.getName().equals("files")) {
                  return this.walk(args).filter(File::isFile);
               } else {
                  throw new IllegalStateException("Unknown method " + method);
               }
            } else {
               File file = new File(this.dir, this.name(method));
               Function<File, File> action = this.action(method);
               Class<?> returnType = method.getReturnType();
               if (returnType.isArray()) {
                  return this.returnArray(method);
               } else if (Stream.class.equals(returnType) && args == null) {
                  return this.returnStream(method);
               } else if (File.class.equals(returnType) && args == null) {
                  return this.returnFile(method, (File)action.apply(file));
               } else if (returnType.isInterface() && args != null && args.length == 1 && args[0] instanceof String) {
                  return Dir.of(returnType, (File)action.apply(new File(this.dir, (String)args[0])));
               } else if (returnType.isInterface() && args == null) {
                  return Dir.of(returnType, (File)action.apply(file));
               } else {
                  throw new UnsupportedOperationException(method.toGenericString());
               }
            }
         }
      }

      private Object dir(Object[] args) {
         return args != null && args.length != 0 ? Dir.of(Dir.class, this.file(args)) : this.dir;
      }

      private Stream<File> walk(Object[] args) {
         return args != null && args.length != 0 ? walk(this.dir, (Integer)args[0]) : walk(this.dir, -1);
      }

      private boolean equals(Object proxy, Object[] args) {
         if (args.length != 1) {
            return false;
         } else if (args[0] == null) {
            return false;
         } else if (!proxy.getClass().isAssignableFrom(args[0].getClass())) {
            return false;
         } else {
            InvocationHandler handler = Proxy.getInvocationHandler(args[0]);
            return this.equals(handler);
         }
      }

      private File file(Object[] args) {
         if (args.length != 1) {
            throw new IllegalArgumentException("Expected String argument.  Found args length: " + args.length);
         } else if (args[0] == null) {
            throw new IllegalArgumentException("Expected String argument.  Found null");
         } else if (!String.class.equals(args[0].getClass())) {
            throw new IllegalArgumentException("Expected String argument.  Found " + args[0].getClass());
         } else {
            return new File(this.dir, args[0].toString());
         }
      }

      private static Object invokeDefault(Object proxy, Method method, Object[] args) throws Throwable {
         float version = Float.parseFloat(System.getProperty("java.class.version"));
         if (version <= 52.0F) {
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            Class<?> clazz = method.getDeclaringClass();
            return ((Lookup)constructor.newInstance(clazz)).in(clazz).unreflectSpecial(method, clazz).bindTo(proxy).invokeWithArguments(args);
         } else {
            return MethodHandles.lookup()
               .findSpecial(
                  method.getDeclaringClass(), method.getName(), MethodType.methodType(method.getReturnType(), new Class[0]), method.getDeclaringClass()
               )
               .bindTo(proxy)
               .invokeWithArguments(args);
         }
      }

      private Object returnStream(Method method) {
         Class returnType = (Class)Generics.getReturnType(method);
         Predicate<File> filter = this.getFilter(method);
         if (returnType.isInterface()) {
            return stream(this.dir, method).filter(filter).map(child -> Dir.of(returnType, child));
         } else if (File.class.equals(returnType)) {
            return stream(this.dir, method).filter(filter);
         } else {
            throw new UnsupportedOperationException(method.toGenericString());
         }
      }

      private Object returnFile(Method method, File file) throws FileNotFoundException {
         if (this.exceptions(method).contains(FileNotFoundException.class) && !file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
         } else {
            return file;
         }
      }

      private Object returnArray(Method method) {
         Predicate<File> filter = this.getFilter(method);
         Class<?> arrayType = method.getReturnType().getComponentType();
         if (File.class.equals(arrayType)) {
            return stream(this.dir, method).filter(filter).toArray(File[]::new);
         } else if (arrayType.isInterface()) {
            Object[] src = stream(this.dir, method).filter(filter).map(child -> Dir.of(arrayType, child)).toArray();
            Object[] dest = (Object[])Array.newInstance(arrayType, src.length);
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
         } else {
            throw new UnsupportedOperationException(method.toGenericString());
         }
      }

      private static Stream<File> stream(File dir, Method method) {
         Walk walk = (Walk)method.getAnnotation(Walk.class);
         return walk != null ? walk(walk, dir) : Stream.of(dir.listFiles());
      }

      private static Stream<File> walk(Walk walk, File dir) {
         return walk(dir, walk.maxDepth());
      }

      private static Stream<File> walk(File dir, int depth) {
         try {
            return depth != -1 ? Files.walk(dir.toPath(), depth, new FileVisitOption[0]).map(Path::toFile) : Files.walk(dir.toPath()).map(Path::toFile);
         } catch (IOException var3) {
            throw new IllegalStateException(var3);
         }
      }

      private Predicate<File> getFilter(Method method) {
         Filter filter = (Filter)method.getAnnotation(Filter.class);
         if (filter == null) {
            return pathname -> true;
         } else {
            Class<? extends FileFilter> clazz = filter.value();

            try {
               FileFilter fileFilter = (FileFilter)clazz.newInstance();
               return fileFilter::accept;
            } catch (Exception var5) {
               throw new IllegalStateException("Unable to instantiate filter " + clazz, var5);
            }
         }
      }

      private File mkdir() {
         org.tomitribe.util.Files.mkdir(this.dir);
         return this.dir;
      }

      private Void mkdirs() {
         org.tomitribe.util.Files.mkdirs(this.dir);
         return null;
      }

      private Void delete() {
         org.tomitribe.util.Files.remove(this.dir);
         return null;
      }

      private String name(Method method) {
         return method.isAnnotationPresent(Name.class) ? ((Name)method.getAnnotation(Name.class)).value() : method.getName();
      }

      public List<Class<?>> exceptions(Method method) {
         Class<?>[] exceptionTypes = method.getExceptionTypes();
         return Arrays.asList(exceptionTypes);
      }

      public Function<File, File> action(Method method) {
         if (method.isAnnotationPresent(Mkdir.class)) {
            return this.mkdir(method);
         } else {
            return method.isAnnotationPresent(Mkdirs.class) ? this.mkdirs(method) : this.noop(method);
         }
      }

      public Function<File, File> mkdir(Method method) {
         return file -> {
            try {
               org.tomitribe.util.Files.mkdir(file);
               return file;
            } catch (Exception var3) {
               throw new Dir.MkdirFailedException(method, file, var3);
            }
         };
      }

      public Function<File, File> mkdirs(Method method) {
         return file -> {
            try {
               org.tomitribe.util.Files.mkdirs(file);
               return file;
            } catch (Exception var3) {
               throw new Dir.MkdirsFailedException(method, file, var3);
            }
         };
      }

      public Function<File, File> noop(Method method) {
         return file -> file;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Dir.DirHandler that = (Dir.DirHandler)o;
            return this.dir.equals(that.dir);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.dir});
      }

      public String toString() {
         return this.dir.getAbsolutePath();
      }
   }

   public static class MkdirFailedException extends RuntimeException {
      public MkdirFailedException(Method method, File dir, Throwable t) {
         super(String.format("@Mkdir failed%n method: %s%n path: %s", method, dir.getAbsolutePath()), t);
      }
   }

   public static class MkdirsFailedException extends RuntimeException {
      public MkdirsFailedException(Method method, File dir, Throwable t) {
         super(String.format("@Mkdirs failed%n method: %s%n path: %s", method, dir.getAbsolutePath()), t);
      }
   }
}
