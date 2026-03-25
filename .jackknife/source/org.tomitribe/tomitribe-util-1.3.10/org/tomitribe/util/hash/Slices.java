package org.tomitribe.util.hash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.tomitribe.util.IO;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

public final class Slices {
   public static final Slice EMPTY_SLICE = new Slice();
   private static final int SLICE_ALLOC_THRESHOLD = 524288;
   private static final double SLICE_ALLOW_SKEW = 1.25;

   private Slices() {
   }

   public static Slice ensureSize(Slice existingSlice, int minWritableBytes) {
      if (existingSlice == null) {
         return allocate(minWritableBytes);
      } else if (minWritableBytes <= existingSlice.length()) {
         return existingSlice;
      } else {
         int newCapacity;
         if (existingSlice.length() == 0) {
            newCapacity = 1;
         } else {
            newCapacity = existingSlice.length();
         }

         int minNewCapacity = existingSlice.length() + minWritableBytes;

         while (newCapacity < minNewCapacity) {
            if (newCapacity < 524288) {
               newCapacity <<= 1;
            } else {
               newCapacity = (int)(newCapacity * 1.25);
            }
         }

         Slice newSlice = allocate(newCapacity);
         newSlice.setBytes(0, existingSlice, 0, existingSlice.length());
         return newSlice;
      }
   }

   public static Slice allocate(int capacity) {
      return capacity == 0 ? EMPTY_SLICE : new Slice(new byte[capacity]);
   }

   public static Slice allocateDirect(int capacity) {
      return capacity == 0 ? EMPTY_SLICE : wrappedBuffer(ByteBuffer.allocateDirect(capacity));
   }

   public static Slice copyOf(Slice slice) {
      return copyOf(slice, 0, slice.length());
   }

   public static Slice copyOf(Slice slice, int offset, int length) {
      Preconditions.checkPositionIndexes(offset, offset + length, slice.length());
      Slice copy = allocate(length);
      copy.setBytes(0, slice, offset, length);
      return copy;
   }

   public static Slice wrappedBuffer(ByteBuffer buffer) {
      if (buffer instanceof DirectBuffer) {
         DirectBuffer direct = (DirectBuffer)buffer;
         return new Slice(null, direct.address(), buffer.capacity(), direct);
      } else if (buffer.hasArray()) {
         int address = Unsafe.ARRAY_BYTE_BASE_OFFSET + buffer.arrayOffset();
         return new Slice(buffer.array(), address, buffer.capacity(), null);
      } else {
         throw new IllegalArgumentException("cannot wrap " + buffer.getClass().getName());
      }
   }

   public static Slice wrappedBuffer(byte[] array) {
      return array.length == 0 ? EMPTY_SLICE : new Slice(array);
   }

   public static Slice wrappedBuffer(byte[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedBooleanArray(boolean... array) {
      return wrappedBooleanArray(array, 0, array.length);
   }

   public static Slice wrappedBooleanArray(boolean[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedShortArray(short... array) {
      return wrappedShortArray(array, 0, array.length);
   }

   public static Slice wrappedShortArray(short[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedIntArray(int... array) {
      return wrappedIntArray(array, 0, array.length);
   }

   public static Slice wrappedIntArray(int[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedLongArray(long... array) {
      return wrappedLongArray(array, 0, array.length);
   }

   public static Slice wrappedLongArray(long[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedFloatArray(float... array) {
      return wrappedFloatArray(array, 0, array.length);
   }

   public static Slice wrappedFloatArray(float[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice wrappedDoubleArray(double... array) {
      return wrappedDoubleArray(array, 0, array.length);
   }

   public static Slice wrappedDoubleArray(double[] array, int offset, int length) {
      return length == 0 ? EMPTY_SLICE : new Slice(array, offset, length);
   }

   public static Slice copiedBuffer(String string, Charset charset) {
      Preconditions.checkNotNull(string, "string is null");
      Preconditions.checkNotNull(charset, "charset is null");
      return wrappedBuffer(string.getBytes(charset));
   }

   public static Slice utf8Slice(String string) {
      return copiedBuffer(string, StandardCharsets.UTF_8);
   }

   public static Slice mapFileReadOnly(File file) throws IOException {
      Preconditions.checkNotNull(file, "file is null");
      if (!file.exists()) {
         throw new FileNotFoundException(file.toString());
      } else {
         RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
         FileChannel channel = randomAccessFile.getChannel();

         Slice var4;
         try {
            MappedByteBuffer byteBuffer = channel.map(MapMode.READ_ONLY, 0L, file.length());
            var4 = wrappedBuffer(byteBuffer);
         } finally {
            IO.close(randomAccessFile);
            IO.close(channel);
         }

         return var4;
      }
   }
}
