package org.tomitribe.util.reflect;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SetAccessible implements PrivilegedAction {
   private final AccessibleObject object;

   public SetAccessible(AccessibleObject object) {
      this.object = object;
   }

   public Object run() {
      this.object.setAccessible(true);
      return this.object;
   }

   public static <T extends AccessibleObject> T on(T object) {
      return (T)AccessController.doPrivileged(new SetAccessible(object));
   }
}
