package org.tomitribe.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Reflection {
   private Reflection() {
   }

   public static Iterable<Method> methods(Class<?> clazz) {
      return new ArrayList(Arrays.asList(clazz.getMethods()));
   }

   public static Iterable<Parameter> params(final Method method) {
      return new Iterable<Parameter>() {
         public Iterator<Parameter> iterator() {
            return new Iterator<Parameter>() {
               private int index = 0;

               public boolean hasNext() {
                  return this.index < method.getParameterTypes().length;
               }

               public Parameter next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     return new Parameter(
                        method.getParameterAnnotations()[this.index],
                        method.getParameterTypes()[this.index],
                        method.getGenericParameterTypes()[this.index],
                        this.index++
                     );
                  }
               }

               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public static Iterable<Parameter> params(final Constructor constructor) {
      return new Iterable<Parameter>() {
         public Iterator<Parameter> iterator() {
            return new Iterator<Parameter>() {
               private int index = 0;

               public boolean hasNext() {
                  return this.index < constructor.getParameterTypes().length;
               }

               public Parameter next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     return new Parameter(
                        constructor.getParameterAnnotations()[this.index],
                        constructor.getParameterTypes()[this.index],
                        constructor.getGenericParameterTypes()[this.index],
                        this.index++
                     );
                  }
               }

               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }
}
