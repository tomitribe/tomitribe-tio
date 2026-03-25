package org.tomitribe.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Generics {
   private Generics() {
   }

   public static Type getType(Field field) {
      return getTypeParameters(field.getType(), field.getGenericType())[0];
   }

   public static Type getType(Parameter parameter) {
      return getTypeParameters(parameter.getType(), parameter.getGenericType())[0];
   }

   public static Type getReturnType(Method method) {
      return getTypeParameters(method.getReturnType(), method.getGenericReturnType())[0];
   }

   public static Type[] getTypeParameters(Class genericClass, Type type) {
      if (type instanceof Class) {
         Class rawClass = (Class)type;
         if (genericClass.equals(type)) {
            return null;
         } else {
            for (Type intf : rawClass.getGenericInterfaces()) {
               Type[] collectionType = getTypeParameters(genericClass, intf);
               if (collectionType != null) {
                  return collectionType;
               }
            }

            return getTypeParameters(genericClass, rawClass.getGenericSuperclass());
         }
      } else if (type instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType)type;
         Type rawType = parameterizedType.getRawType();
         if (genericClass.equals(rawType)) {
            return parameterizedType.getActualTypeArguments();
         } else {
            Type[] collectionTypes = getTypeParameters(genericClass, rawType);
            if (collectionTypes != null) {
               for (int i = 0; i < collectionTypes.length; i++) {
                  if (collectionTypes[i] instanceof TypeVariable) {
                     TypeVariable typeVariable = (TypeVariable)collectionTypes[i];
                     TypeVariable[] rawTypeParams = ((Class)rawType).getTypeParameters();

                     for (int j = 0; j < rawTypeParams.length; j++) {
                        if (typeVariable.getName().equals(rawTypeParams[j].getName())) {
                           collectionTypes[i] = parameterizedType.getActualTypeArguments()[j];
                        }
                     }
                  }
               }
            }

            return collectionTypes;
         }
      } else {
         return null;
      }
   }
}
