package org.tomitribe.util.editor;

import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Converter {
   private Converter() {
   }

   public static Object convertString(String value, Type targetType, String name) {
      if (Class.class.isInstance(targetType)) {
         return convert(value, (Class<?>)Class.class.cast(targetType), name);
      } else {
         if (ParameterizedType.class.isInstance(targetType)) {
            ParameterizedType parameterizedType = (ParameterizedType)ParameterizedType.class.cast(targetType);
            Type raw = parameterizedType.getRawType();
            if (!Class.class.isInstance(raw)) {
               throw new IllegalArgumentException("not supported parameterized type: " + targetType);
            }

            Class<?> rawClass = (Class<?>)Class.class.cast(raw);
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(rawClass)) {
               Class<?> argType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[0]);
               String[] split = value.split(" *, *");
               Collection values;
               if (Collection.class != raw && List.class != raw) {
                  if (Set.class != raw) {
                     throw new IllegalArgumentException(targetType + " collection type not supported");
                  }

                  values = (Collection)(SortedSet.class == raw ? new TreeSet() : new HashSet(split.length));
               } else {
                  values = new ArrayList(split.length);
               }

               for (String val : split) {
                  values.add(convert(val, argType, name));
               }

               return values;
            }

            if (Map.class.isAssignableFrom(rawClass)) {
               Map map;
               if (SortedMap.class == raw) {
                  map = new TreeMap();
               } else {
                  map = new HashMap();
               }

               Properties p = new Properties();

               try {
                  p.load(new ByteArrayInputStream(value.getBytes()));
               } catch (IOException var14) {
               }

               Class<?> keyType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[0]);
               Class<?> valueType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[1]);

               for (String k : p.stringPropertyNames()) {
                  map.put(convert(k, keyType, name), convert(p.getProperty(k), valueType, name));
               }

               return map;
            }
         }

         throw new IllegalArgumentException("not supported type: " + targetType);
      }
   }

   private static Class<?> toClass(Type type) {
      try {
         return (Class<?>)Class.class.cast(type);
      } catch (Exception var2) {
         throw new IllegalArgumentException(type + " not supported");
      }
   }

   public static Object convert(Object value, Class<?> targetType, String name) {
      if (value == null) {
         return targetType.equals(boolean.class) ? false : null;
      } else {
         Class<?> actualType = value.getClass();
         if (targetType.isPrimitive()) {
            targetType = boxPrimitive(targetType);
         }

         if (targetType.isAssignableFrom(actualType)) {
            return value;
         } else if (Number.class.isAssignableFrom(actualType) && Number.class.isAssignableFrom(targetType)) {
            return value;
         } else if (!(value instanceof String)) {
            String message = String.format("Expected type '%s' for '%s'. Found '%s'", targetType.getName(), name, actualType.getName());
            throw new IllegalArgumentException(message);
         } else {
            String stringValue = (String)value;

            try {
               Class.forName(targetType.getName(), true, targetType.getClassLoader());
            } catch (ClassNotFoundException var7) {
            }

            PropertyEditor editor = Editors.get(targetType);
            if (editor == null) {
               Object result = create(targetType, stringValue);
               if (result != null) {
                  return result;
               }
            }

            if (editor == null) {
               String message = String.format("Cannot convert to '%s' for '%s'. No PropertyEditor", targetType.getName(), name);
               throw new IllegalArgumentException(message);
            } else {
               editor.setAsText(stringValue);
               return editor.getValue();
            }
         }
      }
   }

   private static Object create(Class<?> type, String value) {
      if (Enum.class.isAssignableFrom(type)) {
         Class<? extends Enum> enumType = (Class<? extends Enum>)type;

         try {
            return Enum.valueOf(enumType, value);
         } catch (IllegalArgumentException var10) {
            try {
               return Enum.valueOf(enumType, value.toUpperCase());
            } catch (IllegalArgumentException var9) {
               return Enum.valueOf(type, value.toLowerCase());
            }
         }
      } else {
         try {
            Constructor<?> constructor = type.getConstructor(String.class);
            return constructor.newInstance(value);
         } catch (NoSuchMethodException var11) {
            for (Method method : type.getMethods()) {
               if (Modifier.isStatic(method.getModifiers())
                  && Modifier.isPublic(method.getModifiers())
                  && method.getReturnType().equals(type)
                  && method.getParameterTypes().length == 1
                  && method.getParameterTypes()[0].equals(String.class)) {
                  try {
                     return method.invoke(null, value);
                  } catch (Exception var8) {
                     String message = String.format("Cannot convert string '%s' to %s.", value, type);
                     throw new IllegalStateException(message, var8);
                  }
               }
            }

            return null;
         } catch (Exception var12) {
            String message = String.format("Cannot convert string '%s' to %s.", value, type);
            throw new IllegalArgumentException(message, var12);
         }
      }
   }

   private static Class<?> boxPrimitive(Class<?> targetType) {
      if (targetType == byte.class) {
         return Byte.class;
      } else if (targetType == char.class) {
         return Character.class;
      } else if (targetType == short.class) {
         return Short.class;
      } else if (targetType == int.class) {
         return Integer.class;
      } else if (targetType == long.class) {
         return Long.class;
      } else if (targetType == float.class) {
         return Float.class;
      } else if (targetType == double.class) {
         return Double.class;
      } else {
         return targetType == boolean.class ? Boolean.class : targetType;
      }
   }
}
