package org.tomitribe.util.collect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.SetAccessible;

public class ObjectMap extends AbstractMap<String, Object> {
   private final Object object;
   private Map<String, Entry<String, Object>> attributes;
   private Set<Entry<String, Object>> entries;

   public ObjectMap(Object object) {
      this(object.getClass(), object);
   }

   public ObjectMap(Class clazz) {
      this(clazz, null);
   }

   public ObjectMap(Class<?> clazz, Object object) {
      this.object = object;
      this.attributes = new HashMap();

      for (Field field : clazz.getFields()) {
         ObjectMap.FieldEntry entry = new ObjectMap.FieldEntry(field);
         this.attributes.put(entry.getKey(), entry);
      }

      for (Method getter : clazz.getMethods()) {
         if (this.isValidGetter(getter)) {
            String name = getter.getName().replaceFirst("(get|is|find)", "set");
            Method setter = this.getOptionalMethod(clazz, name, getter.getReturnType());
            ObjectMap.MethodEntry entry = new ObjectMap.MethodEntry(name, getter, setter);
            this.attributes.put(entry.getKey(), entry);
         }
      }

      this.entries = Collections.unmodifiableSet(new HashSet(this.attributes.values()));
   }

   private boolean isValidGetter(Method m) {
      if (Modifier.isAbstract(m.getModifiers())) {
         return false;
      } else if (void.class.equals(m.getReturnType())) {
         return false;
      } else if (m.getParameterTypes().length != 0) {
         return false;
      } else if (m.getName().startsWith("get") || m.getName().startsWith("find")) {
         return true;
      } else if (!m.getName().startsWith("is")) {
         return false;
      } else {
         return m.getReturnType().equals(Boolean.class) ? true : m.getReturnType().equals(boolean.class);
      }
   }

   private Method getOptionalMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
      try {
         return clazz.getMethod(name, parameterTypes);
      } catch (NoSuchMethodException var5) {
         return null;
      }
   }

   public Object get(Object key) {
      Entry<String, Object> entry = (Entry<String, Object>)this.attributes.get(key);
      return entry == null ? null : entry.getValue();
   }

   public Object put(String key, Object value) {
      Entry<String, Object> entry = (Entry<String, Object>)this.attributes.get(key);
      return entry == null ? null : entry.setValue(value);
   }

   public boolean containsKey(Object key) {
      return this.attributes.containsKey(key);
   }

   public Object remove(Object key) {
      throw new UnsupportedOperationException();
   }

   public Set<Entry<String, Object>> entrySet() {
      return this.entries;
   }

   public class FieldEntry implements ObjectMap.Member {
      private final Field field;

      public FieldEntry(Field field) {
         this.field = field;
      }

      public String getKey() {
         return this.field.getName();
      }

      public Object getValue() {
         try {
            return this.field.get(ObjectMap.this.object);
         } catch (IllegalAccessException var2) {
            throw new IllegalStateException(var2);
         }
      }

      public Object setValue(Object value) {
         try {
            Object replaced = this.getValue();
            value = Converter.convert(value, this.field.getType(), this.getKey());
            this.field.set(ObjectMap.this.object, value);
            return replaced;
         } catch (IllegalAccessException var3) {
            throw new IllegalArgumentException(var3);
         }
      }

      @Override
      public Class<?> getType() {
         return this.field.getType();
      }

      @Override
      public boolean isReadOnly() {
         return false;
      }
   }

   public interface Member extends Entry<String, Object> {
      Class<?> getType();

      boolean isReadOnly();
   }

   public class MethodEntry implements ObjectMap.Member {
      private final String key;
      private final Method getter;
      private final Method setter;

      public MethodEntry(String methodName, Method getter, Method setter) {
         StringBuilder name = new StringBuilder(methodName);
         name.delete(0, 3);
         name.setCharAt(0, Character.toLowerCase(name.charAt(0)));
         this.key = name.toString();
         this.getter = getter;
         this.setter = setter;
      }

      protected Object invoke(Method method, Object... args) {
         SetAccessible.on(method);

         try {
            return method.invoke(ObjectMap.this.object, args);
         } catch (InvocationTargetException var4) {
            throw new RuntimeException(var4.getCause());
         } catch (Exception var5) {
            throw new IllegalStateException(String.format("Key: %s, Method: %s", this.key, method.toString()), var5);
         }
      }

      public String getKey() {
         return this.key;
      }

      public Object getValue() {
         return this.invoke(this.getter);
      }

      public Object setValue(Object value) {
         if (this.setter == null) {
            throw new IllegalArgumentException(String.format("'%s' is read-only", this.key));
         } else {
            Object original = this.getValue();
            value = Converter.convert(value, this.setter.getParameterTypes()[0], this.getKey());
            this.invoke(this.setter, value);
            return original;
         }
      }

      @Override
      public Class<?> getType() {
         return this.getter.getReturnType();
      }

      @Override
      public boolean isReadOnly() {
         return this.setter != null;
      }
   }
}
