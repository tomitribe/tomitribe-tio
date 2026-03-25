package org.tomitribe.util.reflect;

import java.util.Arrays;
import java.util.Iterator;

public class StackTraceElements {
   private StackTraceElements() {
   }

   public static StackTraceElement getCurrentMethod() {
      Iterator<StackTraceElement> stackTrace = seek(StackTraceElements.class, "getCurrentMethod");
      return (StackTraceElement)stackTrace.next();
   }

   public static StackTraceElement getCallingMethod() {
      Iterator<StackTraceElement> stackTrace = seek(StackTraceElements.class, "getCallingMethod");
      stackTrace.next();
      return (StackTraceElement)stackTrace.next();
   }

   private static Iterator<StackTraceElement> seek(Class<StackTraceElements> clazz, String method) {
      for (StackTraceElement next : Arrays.asList(new Exception().fillInStackTrace().getStackTrace())) {
         if (next.getClassName().equals(clazz.getName()) && next.getMethodName().equals(method)) {
            break;
         }
      }

      Object stackTrace;
      return (Iterator<StackTraceElement>)stackTrace;
   }

   public static Class<?> asClass(StackTraceElement stackTraceElement) throws ClassNotFoundException {
      return asClass(stackTraceElement, Thread.currentThread().getContextClassLoader());
   }

   public static Class<?> asClass(StackTraceElement stackTraceElement, ClassLoader classLoader) throws ClassNotFoundException {
      return classLoader.loadClass(stackTraceElement.getClassName());
   }
}
