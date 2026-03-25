package org.tomitribe.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Options {
   private final Options parent;
   private final Properties properties;

   public Options(Properties properties) {
      this(properties, new Options.NullOptions());
   }

   public Options(Properties properties, Options parent) {
      this.parent = parent;
      this.properties = properties;
   }

   public Options getParent() {
      return this.parent;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public void setLogger(Options.Log logger) {
      this.parent.setLogger(logger);
   }

   public Options.Log getLogger() {
      return this.parent.getLogger();
   }

   public boolean has(String property) {
      return this.properties.containsKey(property) || this.parent.has(property);
   }

   public String get(String property, String defaultValue) {
      String value = this.properties.getProperty(property);
      return value != null ? this.log(property, value) : this.parent.get(property, defaultValue);
   }

   public <T> T get(String property, T defaultValue) {
      if (defaultValue == null) {
         throw new NullPointerException("defaultValue");
      } else {
         String value = this.properties.getProperty(property);
         if (value != null && !"".equals(value)) {
            try {
               Class<?> type = defaultValue.getClass();
               Constructor<?> constructor = type.getConstructor(String.class);
               T t = (T)constructor.newInstance(value);
               return this.log(property, t);
            } catch (Exception var7) {
               var7.printStackTrace();
               this.warn(property, value, var7);
               return this.parent.get(property, defaultValue);
            }
         } else {
            return this.parent.get(property, defaultValue);
         }
      }
   }

   public int get(String property, int defaultValue) {
      String value = this.properties.getProperty(property);
      if (value != null && !"".equals(value)) {
         try {
            return this.log(property, Integer.parseInt(value));
         } catch (NumberFormatException var5) {
            this.warn(property, value, var5);
            return this.parent.get(property, defaultValue);
         }
      } else {
         return this.parent.get(property, defaultValue);
      }
   }

   public long get(String property, long defaultValue) {
      String value = this.properties.getProperty(property);
      if (value != null && !"".equals(value)) {
         try {
            return this.log(property, Long.parseLong(value));
         } catch (NumberFormatException var6) {
            this.warn(property, value, var6);
            return this.parent.get(property, defaultValue);
         }
      } else {
         return this.parent.get(property, defaultValue);
      }
   }

   public boolean get(String property, boolean defaultValue) {
      String value = this.properties.getProperty(property);
      if (value != null && !"".equals(value)) {
         try {
            return this.log(property, Boolean.parseBoolean(value));
         } catch (NumberFormatException var5) {
            this.warn(property, value, var5);
            return this.parent.get(property, defaultValue);
         }
      } else {
         return this.parent.get(property, defaultValue);
      }
   }

   public Class<?> get(String property, Class<?> defaultValue) {
      String className = this.properties.getProperty(property);
      if (className == null) {
         return this.parent.get(property, defaultValue);
      } else {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

         try {
            return this.log(property, classLoader.loadClass(className));
         } catch (Exception var6) {
            this.getLogger().warning("Could not load " + property + " : " + className, var6);
            return this.parent.get(property, defaultValue);
         }
      }
   }

   public <T extends Enum<T>> T get(String property, T defaultValue) {
      String value = this.properties.getProperty(property);
      if (value == null || "".equals(value)) {
         return this.parent.get(property, defaultValue);
      } else if (defaultValue == null) {
         throw new IllegalArgumentException("Must supply a default for property " + property);
      } else {
         Class<T> enumType = defaultValue.getClass();

         try {
            return this.log(property, valueOf(enumType, value.toUpperCase()));
         } catch (IllegalArgumentException var6) {
            this.warn(property, value);
            return this.parent.get(property, defaultValue);
         }
      }
   }

   public <T extends Enum<T>> Set<T> getAll(String property, T... defaultValue) {
      EnumSet<T> defaults = EnumSet.copyOf(Arrays.asList(defaultValue));
      return this.getAll(property, defaults);
   }

   public <T extends Enum<T>> Set<T> getAll(String property, Set<T> defaultValue) {
      Class<T> enumType;
      try {
         T t = (T)defaultValue.iterator().next();
         enumType = t.getClass();
      } catch (Exception var5) {
         throw new IllegalArgumentException("Must supply a default for property " + property);
      }

      return this.getAll(property, defaultValue, enumType);
   }

   public <T extends Enum<T>> Set<T> getAll(String property, Class<T> enumType) {
      return this.getAll(property, Collections.EMPTY_SET, enumType);
   }

   protected <T extends Enum<T>> Set<T> getAll(String property, Set<T> defaultValue, Class<T> enumType) {
      String value = this.properties.getProperty(property);
      if (value == null || "".equals(value)) {
         return this.parent.getAll(property, defaultValue, enumType);
      } else if ("all".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
         this.log(property, value);
         return EnumSet.allOf(enumType);
      } else if (!"none".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
         try {
            String[] values = value.split(",");
            EnumSet<T> set = EnumSet.noneOf(enumType);

            for (String s : values) {
               s = s.trim();
               set.add(valueOf(enumType, s.toUpperCase()));
            }

            return this.logAll(property, set);
         } catch (IllegalArgumentException var11) {
            this.warn(property, value);
            return this.parent.getAll(property, defaultValue, enumType);
         }
      } else {
         this.log(property, value);
         return EnumSet.noneOf(enumType);
      }
   }

   public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
      Map<String, T> map = new HashMap();

      for (T t : (Enum[])enumType.getEnumConstants()) {
         map.put(t.name().toUpperCase(), t);
      }

      T value = (T)map.get(name.toUpperCase());
      if (value == null || "".equals(value)) {
         Enum.valueOf(enumType, name);
      }

      return value;
   }

   protected void warn(String property, String value) {
      this.getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"");
   }

   protected void warn(String property, String value, Exception e) {
      this.getLogger().warning("Cannot parse supplied value \"" + value + "\" for option \"" + property + "\"", e);
   }

   protected <V> V log(String property, V value) {
      if (!this.getLogger().isInfoEnabled()) {
         return value;
      } else {
         if (value instanceof Class) {
            Class clazz = (Class)value;
            this.getLogger().info("Using '" + property + "=" + clazz.getName() + "'");
         } else {
            this.getLogger().info("Using '" + property + "=" + value + "'");
         }

         return value;
      }
   }

   public <T extends Enum<T>> Set<T> logAll(String property, Set<T> value) {
      if (!this.getLogger().isInfoEnabled()) {
         return value;
      } else {
         this.getLogger().info("Using '" + property + "=" + join(", ", lowercase(value)) + "'");
         return value;
      }
   }

   protected static <T extends Enum<T>> String[] lowercase(T... items) {
      String[] values = new String[items.length];

      for (int i = 0; i < items.length; i++) {
         values[i] = items[i].name().toLowerCase();
      }

      return values;
   }

   protected static <T extends Enum<T>> String[] lowercase(Collection<T> items) {
      String[] values = new String[items.size()];
      int i = 0;

      for (T item : items) {
         values[i++] = item.name().toLowerCase();
      }

      return values;
   }

   protected static <V extends Enum<V>> String possibleValues(V v) {
      Class<? extends Enum> enumType = v.getClass();
      return possibleValues(enumType);
   }

   protected static String possibleValues(Class<? extends Enum> enumType) {
      return join(", ", lowercase((Enum[])enumType.getEnumConstants()));
   }

   public static String join(String delimiter, Object... collection) {
      StringBuilder sb = new StringBuilder();

      for (Object obj : collection) {
         sb.append(obj).append(delimiter);
      }

      if (collection.length > 0) {
         sb.delete(sb.length() - delimiter.length(), sb.length());
      }

      return sb.toString();
   }

   public interface Log {
      boolean isDebugEnabled();

      boolean isInfoEnabled();

      boolean isWarningEnabled();

      void warning(String var1, Throwable var2);

      void warning(String var1);

      void debug(String var1, Throwable var2);

      void debug(String var1);

      void info(String var1, Throwable var2);

      void info(String var1);
   }

   public static class NullLog implements Options.Log {
      @Override
      public boolean isDebugEnabled() {
         return false;
      }

      @Override
      public boolean isInfoEnabled() {
         return false;
      }

      @Override
      public boolean isWarningEnabled() {
         return false;
      }

      @Override
      public void warning(String message, Throwable t) {
      }

      @Override
      public void warning(String message) {
      }

      @Override
      public void debug(String message, Throwable t) {
      }

      @Override
      public void debug(String message) {
      }

      @Override
      public void info(String message, Throwable t) {
      }

      @Override
      public void info(String message) {
      }
   }

   protected static class NullOptions extends Options {
      private Options.Log logger = new Options.NullLog();

      public NullOptions() {
         super(null, null);
      }

      @Override
      public Options.Log getLogger() {
         return this.logger;
      }

      @Override
      public void setLogger(Options.Log logger) {
         this.logger = logger;
      }

      @Override
      public boolean has(String property) {
         return false;
      }

      @Override
      public <T> T get(String property, T defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public int get(String property, int defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public long get(String property, long defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public boolean get(String property, boolean defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public <T extends Enum<T>> T get(String property, T defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public <T extends Enum<T>> Set<T> getAll(String property, T... defaultValue) {
         return EnumSet.copyOf(Arrays.asList(defaultValue));
      }

      @Override
      protected <T extends Enum<T>> Set<T> getAll(String property, Set<T> defaults, Class<T> enumType) {
         if (this.getLogger().isDebugEnabled()) {
            String possibleValues = "  Possible values are: " + possibleValues(enumType);
            possibleValues = possibleValues + " or NONE or ALL";
            String defaultValues;
            if (defaults.size() == 0) {
               defaultValues = "NONE";
            } else if (defaults.size() == ((Enum[])enumType.getEnumConstants()).length) {
               defaultValues = "ALL";
            } else {
               defaultValues = join(", ", lowercase(defaults));
            }

            this.getLogger().debug("Using default '" + property + "=" + defaultValues + "'" + possibleValues);
         }

         return defaults;
      }

      @Override
      public String get(String property, String defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      public Class<?> get(String property, Class<?> defaultValue) {
         return this.log(property, defaultValue);
      }

      @Override
      protected <V> V log(String property, V value) {
         if (this.getLogger().isDebugEnabled()) {
            if (value instanceof Enum) {
               Enum anEnum = (Enum)value;
               this.getLogger()
                  .debug("Using default '" + property + "=" + anEnum.name().toLowerCase() + "'.  Possible values are: " + possibleValues((V)anEnum));
            } else if (value instanceof Class) {
               Class clazz = (Class)value;
               this.getLogger().debug("Using default '" + property + "=" + clazz.getName() + "'");
            } else if (value != null) {
               this.logger.debug("Using default '" + property + "=" + value + "'");
            }
         }

         return value;
      }
   }
}
