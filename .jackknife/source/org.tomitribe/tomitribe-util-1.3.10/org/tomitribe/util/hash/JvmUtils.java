package org.tomitribe.util.hash;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import sun.misc.Unsafe;

final class JvmUtils {
   static final Unsafe unsafe;
   static final MethodHandle newByteBuffer;

   private static void assertArrayIndexScale(String name, int actualIndexScale, int expectedIndexScale) {
      if (actualIndexScale != expectedIndexScale) {
         throw new IllegalStateException(name + " array index scale must be " + expectedIndexScale + ", but is " + actualIndexScale);
      }
   }

   private JvmUtils() {
   }

   static {
      try {
         Field field = Unsafe.class.getDeclaredField("theUnsafe");
         field.setAccessible(true);
         unsafe = (Unsafe)field.get(null);
         if (unsafe == null) {
            throw new RuntimeException("Unsafe access not available");
         } else {
            assertArrayIndexScale("Boolean", Unsafe.ARRAY_BOOLEAN_INDEX_SCALE, 1);
            assertArrayIndexScale("Byte", Unsafe.ARRAY_BYTE_INDEX_SCALE, 1);
            assertArrayIndexScale("Short", Unsafe.ARRAY_SHORT_INDEX_SCALE, 2);
            assertArrayIndexScale("Int", Unsafe.ARRAY_INT_INDEX_SCALE, 4);
            assertArrayIndexScale("Long", Unsafe.ARRAY_LONG_INDEX_SCALE, 8);
            assertArrayIndexScale("Float", Unsafe.ARRAY_FLOAT_INDEX_SCALE, 4);
            assertArrayIndexScale("Double", Unsafe.ARRAY_DOUBLE_INDEX_SCALE, 8);
            Class<?> directByteBufferClass = ClassLoader.getSystemClassLoader().loadClass("java.nio.DirectByteBuffer");
            Constructor<?> constructor = directByteBufferClass.getDeclaredConstructor(long.class, int.class, Object.class);
            constructor.setAccessible(true);
            newByteBuffer = MethodHandles.lookup()
               .unreflectConstructor(constructor)
               .asType(MethodType.methodType(ByteBuffer.class, long.class, int.class, Object.class));
         }
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }
}
