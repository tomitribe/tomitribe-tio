package org.tomitribe.util.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

public class Parameter implements AnnotatedElement {
   private final Annotation[] annotations;
   private final Class<?> type;
   private final Type genericType;
   private final int index;

   public Parameter(Annotation[] annotations, Class<?> type, Type genericType, int index) {
      this.annotations = annotations;
      this.type = type;
      this.genericType = genericType;
      this.index = index;
   }

   public int getIndex() {
      return this.index;
   }

   public Class<?> getType() {
      return this.type;
   }

   public Type getGenericType() {
      return this.genericType;
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
      return this.getAnnotation(annotationClass) != null;
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
      for (Annotation annotation : this.annotations) {
         if (annotationClass.equals(annotation.annotationType())) {
            return (T)annotation;
         }
      }

      return null;
   }

   public Annotation[] getAnnotations() {
      return this.annotations;
   }

   public Annotation[] getDeclaredAnnotations() {
      return this.getAnnotations();
   }
}
