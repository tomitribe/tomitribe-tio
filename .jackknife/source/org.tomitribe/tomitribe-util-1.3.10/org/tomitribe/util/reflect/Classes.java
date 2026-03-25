package org.tomitribe.util.reflect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classes {
   private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap();
   private static final HashMap<String, Class> PRIMITIVES = new HashMap();

   private Classes() {
   }

   public static Class forName(String string, ClassLoader classLoader) throws ClassNotFoundException {
      int arrayDimentions;
      for (arrayDimentions = 0; string.endsWith("[]"); arrayDimentions++) {
         string = string.substring(0, string.length() - 2);
      }

      Class clazz = (Class)PRIMITIVES.get(string);
      if (clazz == null) {
         clazz = Class.forName(string, true, classLoader);
      }

      return arrayDimentions == 0 ? clazz : Array.newInstance(clazz, new int[arrayDimentions]).getClass();
   }

   public static String packageName(Class clazz) {
      return packageName(clazz.getName());
   }

   public static String packageName(String clazzName) {
      int i = clazzName.lastIndexOf(46);
      return i > 0 ? clazzName.substring(0, i) : "";
   }

   public static String simpleName(Class clazz) {
      return clazz.getSimpleName();
   }

   public static String simpleName(String clazzName) {
      int i = clazzName.lastIndexOf(46);
      return i > 0 ? clazzName.substring(i + 1) : clazzName;
   }

   public static List<String> getSimpleNames(Class... classes) {
      List<String> list = new ArrayList();

      for (Class aClass : classes) {
         list.add(aClass.getSimpleName());
      }

      return list;
   }

   public static Class<?> deprimitivize(Class<?> fieldType) {
      return fieldType.isPrimitive() ? (Class)PRIMITIVE_WRAPPERS.get(fieldType) : fieldType;
   }

   public static List<Class<?>> ancestors(Class clazz) {
      ArrayList<Class<?>> ancestors = new ArrayList();

      while (clazz != null && !clazz.equals(Object.class)) {
         ancestors.add(clazz);
         clazz = clazz.getSuperclass();
      }

      return ancestors;
   }

   static {
      PRIMITIVES.put("boolean", boolean.class);
      PRIMITIVES.put("byte", byte.class);
      PRIMITIVES.put("char", char.class);
      PRIMITIVES.put("short", short.class);
      PRIMITIVES.put("int", int.class);
      PRIMITIVES.put("long", long.class);
      PRIMITIVES.put("float", float.class);
      PRIMITIVES.put("double", double.class);
      PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
      PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
      PRIMITIVE_WRAPPERS.put(char.class, Character.class);
      PRIMITIVE_WRAPPERS.put(double.class, Double.class);
      PRIMITIVE_WRAPPERS.put(float.class, Float.class);
      PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
      PRIMITIVE_WRAPPERS.put(long.class, Long.class);
      PRIMITIVE_WRAPPERS.put(short.class, Short.class);
   }
}
