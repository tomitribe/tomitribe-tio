package org.tomitribe.util.hash;

import sun.misc.Unsafe;

public final class SizeOf {
   public static final byte SIZE_OF_BYTE = 1;
   public static final byte SIZE_OF_SHORT = 2;
   public static final byte SIZE_OF_INT = 4;
   public static final byte SIZE_OF_LONG = 8;
   public static final byte SIZE_OF_FLOAT = 4;
   public static final byte SIZE_OF_DOUBLE = 8;

   public static long sizeOf(boolean[] array) {
      return array == null ? 0L : Unsafe.ARRAY_BOOLEAN_BASE_OFFSET + (long)Unsafe.ARRAY_BOOLEAN_INDEX_SCALE * array.length;
   }

   public static long sizeOf(byte[] array) {
      return array == null ? 0L : Unsafe.ARRAY_BYTE_BASE_OFFSET + (long)Unsafe.ARRAY_BYTE_INDEX_SCALE * array.length;
   }

   public static long sizeOf(short[] array) {
      return array == null ? 0L : Unsafe.ARRAY_SHORT_BASE_OFFSET + (long)Unsafe.ARRAY_SHORT_INDEX_SCALE * array.length;
   }

   public static long sizeOf(char[] array) {
      return array == null ? 0L : Unsafe.ARRAY_CHAR_BASE_OFFSET + (long)Unsafe.ARRAY_CHAR_INDEX_SCALE * array.length;
   }

   public static long sizeOf(int[] array) {
      return array == null ? 0L : Unsafe.ARRAY_INT_BASE_OFFSET + (long)Unsafe.ARRAY_INT_INDEX_SCALE * array.length;
   }

   public static long sizeOf(long[] array) {
      return array == null ? 0L : Unsafe.ARRAY_LONG_BASE_OFFSET + (long)Unsafe.ARRAY_LONG_INDEX_SCALE * array.length;
   }

   public static long sizeOf(float[] array) {
      return array == null ? 0L : Unsafe.ARRAY_FLOAT_BASE_OFFSET + (long)Unsafe.ARRAY_FLOAT_INDEX_SCALE * array.length;
   }

   public static long sizeOf(double[] array) {
      return array == null ? 0L : Unsafe.ARRAY_DOUBLE_BASE_OFFSET + (long)Unsafe.ARRAY_DOUBLE_INDEX_SCALE * array.length;
   }

   public static long sizeOf(Object[] array) {
      return array == null ? 0L : Unsafe.ARRAY_OBJECT_BASE_OFFSET + (long)Unsafe.ARRAY_OBJECT_INDEX_SCALE * array.length;
   }

   public static long sizeOfBooleanArray(int length) {
      return Unsafe.ARRAY_BOOLEAN_BASE_OFFSET + (long)Unsafe.ARRAY_BOOLEAN_INDEX_SCALE * length;
   }

   public static long sizeOfByteArray(int length) {
      return Unsafe.ARRAY_BYTE_BASE_OFFSET + (long)Unsafe.ARRAY_BYTE_INDEX_SCALE * length;
   }

   public static long sizeOfShortArray(int length) {
      return Unsafe.ARRAY_SHORT_BASE_OFFSET + (long)Unsafe.ARRAY_SHORT_INDEX_SCALE * length;
   }

   public static long sizeOfCharArray(int length) {
      return Unsafe.ARRAY_CHAR_BASE_OFFSET + (long)Unsafe.ARRAY_CHAR_INDEX_SCALE * length;
   }

   public static long sizeOfIntArray(int length) {
      return Unsafe.ARRAY_INT_BASE_OFFSET + (long)Unsafe.ARRAY_INT_INDEX_SCALE * length;
   }

   public static long sizeOfLongArray(int length) {
      return Unsafe.ARRAY_LONG_BASE_OFFSET + (long)Unsafe.ARRAY_LONG_INDEX_SCALE * length;
   }

   public static long sizeOfFloatArray(int length) {
      return Unsafe.ARRAY_FLOAT_BASE_OFFSET + (long)Unsafe.ARRAY_FLOAT_INDEX_SCALE * length;
   }

   public static long sizeOfDoubleArray(int length) {
      return Unsafe.ARRAY_DOUBLE_BASE_OFFSET + (long)Unsafe.ARRAY_DOUBLE_INDEX_SCALE * length;
   }

   public static long sizeOfObjectArray(int length) {
      return Unsafe.ARRAY_OBJECT_BASE_OFFSET + (long)Unsafe.ARRAY_OBJECT_INDEX_SCALE * length;
   }

   private SizeOf() {
   }
}
