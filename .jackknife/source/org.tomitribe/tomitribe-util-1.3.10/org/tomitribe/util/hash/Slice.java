package org.tomitribe.util.hash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import sun.misc.Unsafe;

public final class Slice implements Comparable<Slice> {
   private final Object base;
   private final long address;
   private final int size;
   private final Object reference;
   private int hash;

   @Deprecated
   public static Slice toUnsafeSlice(ByteBuffer byteBuffer) {
      return Slices.wrappedBuffer(byteBuffer);
   }

   Slice() {
      this.base = null;
      this.address = 0L;
      this.size = 0;
      this.reference = null;
   }

   Slice(byte[] base) {
      Preconditions.checkNotNull(base, "base is null");
      this.base = base;
      this.address = Unsafe.ARRAY_BYTE_BASE_OFFSET;
      this.size = base.length;
      this.reference = null;
   }

   Slice(byte[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_BYTE_BASE_OFFSET + offset;
      this.size = length;
      this.reference = null;
   }

   Slice(boolean[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_BOOLEAN_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_BOOLEAN_INDEX_SCALE;
      this.reference = null;
   }

   Slice(short[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_SHORT_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_SHORT_INDEX_SCALE;
      this.reference = null;
   }

   Slice(int[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_INT_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_INT_INDEX_SCALE;
      this.reference = null;
   }

   Slice(long[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_LONG_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_LONG_INDEX_SCALE;
      this.reference = null;
   }

   Slice(float[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_FLOAT_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_FLOAT_INDEX_SCALE;
      this.reference = null;
   }

   Slice(double[] base, int offset, int length) {
      Preconditions.checkNotNull(base, "base is null");
      Preconditions.checkPositionIndexes(offset, offset + length, base.length);
      this.base = base;
      this.address = Unsafe.ARRAY_DOUBLE_BASE_OFFSET + offset;
      this.size = length * Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
      this.reference = null;
   }

   Slice(Object base, long address, int size, Object reference) {
      if (address <= 0L) {
         throw new IllegalArgumentException(String.format("Invalid address: %s", address));
      } else if (size <= 0) {
         throw new IllegalArgumentException(String.format("Invalid size: %s", size));
      } else {
         Preconditions.checkArgument(address + size >= size, "Address + size is greater than 64 bits");
         this.reference = reference;
         this.base = base;
         this.address = address;
         this.size = size;
      }
   }

   public Object getBase() {
      return this.base;
   }

   public long getAddress() {
      return this.address;
   }

   public int length() {
      return this.size;
   }

   public void fill(byte value) {
      int offset = 0;
      int length = this.size;

      for (long longValue = fillLong(value); length >= 8; length -= 8) {
         JvmUtils.unsafe.putLong(this.base, this.address + offset, longValue);
         offset += 8;
      }

      while (length > 0) {
         JvmUtils.unsafe.putByte(this.base, this.address + offset, value);
         offset++;
         length--;
      }
   }

   public void clear() {
      this.clear(0, this.size);
   }

   public void clear(int offset, int length) {
      while (length >= 8) {
         JvmUtils.unsafe.putLong(this.base, this.address + offset, 0L);
         offset += 8;
         length -= 8;
      }

      while (length > 0) {
         JvmUtils.unsafe.putByte(this.base, this.address + offset, (byte)0);
         offset++;
         length--;
      }
   }

   public byte getByte(int index) {
      this.checkIndexLength(index, 1);
      return JvmUtils.unsafe.getByte(this.base, this.address + index);
   }

   public short getUnsignedByte(int index) {
      return (short)(this.getByte(index) & 255);
   }

   public short getShort(int index) {
      this.checkIndexLength(index, 2);
      return JvmUtils.unsafe.getShort(this.base, this.address + index);
   }

   public int getInt(int index) {
      this.checkIndexLength(index, 4);
      return JvmUtils.unsafe.getInt(this.base, this.address + index);
   }

   public long getLong(int index) {
      this.checkIndexLength(index, 8);
      return JvmUtils.unsafe.getLong(this.base, this.address + index);
   }

   public float getFloat(int index) {
      this.checkIndexLength(index, 4);
      return JvmUtils.unsafe.getFloat(this.base, this.address + index);
   }

   public double getDouble(int index) {
      this.checkIndexLength(index, 8);
      return JvmUtils.unsafe.getDouble(this.base, this.address + index);
   }

   public void getBytes(int index, Slice destination) {
      this.getBytes(index, destination, 0, destination.length());
   }

   public void getBytes(int index, Slice destination, int destinationIndex, int length) {
      destination.setBytes(destinationIndex, this, index, length);
   }

   public void getBytes(int index, byte[] destination) {
      this.getBytes(index, destination, 0, destination.length);
   }

   public void getBytes(int index, byte[] destination, int destinationIndex, int length) {
      this.checkIndexLength(index, length);
      Preconditions.checkPositionIndexes(destinationIndex, destinationIndex + length, destination.length);
      copyMemory(this.base, this.address + index, destination, (long)Unsafe.ARRAY_BYTE_BASE_OFFSET + destinationIndex, length);
   }

   public byte[] getBytes() {
      return this.getBytes(0, this.length());
   }

   public byte[] getBytes(int index, int length) {
      byte[] bytes = new byte[length];
      this.getBytes(index, bytes, 0, length);
      return bytes;
   }

   public void getBytes(int index, OutputStream out, int length) throws IOException {
      this.checkIndexLength(index, length);
      byte[] buffer = new byte[4096];

      while (length > 0) {
         int size = Math.min(buffer.length, length);
         this.getBytes(index, buffer, 0, size);
         out.write(buffer, 0, size);
         length -= size;
         index += size;
      }
   }

   public void setByte(int index, int value) {
      this.checkIndexLength(index, 1);
      JvmUtils.unsafe.putByte(this.base, this.address + index, (byte)(value & 0xFF));
   }

   public void setShort(int index, int value) {
      this.checkIndexLength(index, 2);
      JvmUtils.unsafe.putShort(this.base, this.address + index, (short)(value & 65535));
   }

   public void setInt(int index, int value) {
      this.checkIndexLength(index, 4);
      JvmUtils.unsafe.putInt(this.base, this.address + index, value);
   }

   public void setLong(int index, long value) {
      this.checkIndexLength(index, 8);
      JvmUtils.unsafe.putLong(this.base, this.address + index, value);
   }

   public void setFloat(int index, float value) {
      this.checkIndexLength(index, 4);
      JvmUtils.unsafe.putFloat(this.base, this.address + index, value);
   }

   public void setDouble(int index, double value) {
      this.checkIndexLength(index, 8);
      JvmUtils.unsafe.putDouble(this.base, this.address + index, value);
   }

   public void setBytes(int index, Slice source) {
      this.setBytes(index, source, 0, source.length());
   }

   public void setBytes(int index, Slice source, int sourceIndex, int length) {
      this.checkIndexLength(index, length);
      Preconditions.checkPositionIndexes(sourceIndex, sourceIndex + length, source.length());
      copyMemory(source.base, source.address + sourceIndex, this.base, this.address + index, length);
   }

   public void setBytes(int index, byte[] source) {
      this.setBytes(index, source, 0, source.length);
   }

   public void setBytes(int index, byte[] source, int sourceIndex, int length) {
      Preconditions.checkPositionIndexes(sourceIndex, sourceIndex + length, source.length);
      copyMemory(source, (long)Unsafe.ARRAY_BYTE_BASE_OFFSET + sourceIndex, this.base, this.address + index, length);
   }

   public int setBytes(int index, InputStream in, int length) throws IOException {
      this.checkIndexLength(index, length);
      byte[] bytes = new byte[4096];
      int remaining = length;

      while (remaining > 0) {
         int bytesRead = in.read(bytes, 0, Math.min(bytes.length, remaining));
         if (bytesRead < 0) {
            if (remaining == length) {
               return -1;
            }
            break;
         }

         copyMemory(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, this.base, this.address + index, bytesRead);
         remaining -= bytesRead;
         index += bytesRead;
      }

      return length - remaining;
   }

   public Slice slice(int index, int length) {
      if (index == 0 && length == this.length()) {
         return this;
      } else {
         this.checkIndexLength(index, length);
         return length == 0 ? Slices.EMPTY_SLICE : new Slice(this.base, this.address + index, length, this.reference);
      }
   }

   public int compareTo(Slice that) {
      return this == that ? 0 : this.compareTo(0, this.size, that, 0, that.size);
   }

   public int compareTo(int offset, int length, Slice that, int otherOffset, int otherLength) {
      if (this == that && offset == otherOffset && length == otherLength) {
         return 0;
      } else {
         this.checkIndexLength(offset, length);
         that.checkIndexLength(otherOffset, otherLength);

         int compareLength;
         for (compareLength = Math.min(length, otherLength); compareLength >= 8; compareLength -= 8) {
            long thisLong = JvmUtils.unsafe.getLong(this.base, this.address + offset);
            thisLong = Long.reverseBytes(thisLong);
            long thatLong = JvmUtils.unsafe.getLong(that.base, that.address + otherOffset);
            thatLong = Long.reverseBytes(thatLong);
            int v = compareUnsignedLongs(thisLong, thatLong);
            if (v != 0) {
               return v;
            }

            offset += 8;
            otherOffset += 8;
         }

         while (compareLength > 0) {
            byte thisByte = JvmUtils.unsafe.getByte(this.base, this.address + offset);
            byte thatByte = JvmUtils.unsafe.getByte(that.base, that.address + otherOffset);
            int v = compareUnsignedBytes(thisByte, thatByte);
            if (v != 0) {
               return v;
            }

            offset++;
            otherOffset++;
            compareLength--;
         }

         return Integer.compare(length, otherLength);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Slice)) {
         return false;
      } else {
         Slice that = (Slice)o;
         if (this.length() != that.length()) {
            return false;
         } else {
            int offset = 0;

            int length;
            for (length = this.size; length >= 8; length -= 8) {
               long thisLong = JvmUtils.unsafe.getLong(this.base, this.address + offset);
               long thatLong = JvmUtils.unsafe.getLong(that.base, that.address + offset);
               if (thisLong != thatLong) {
                  return false;
               }

               offset += 8;
            }

            while (length > 0) {
               byte thisByte = JvmUtils.unsafe.getByte(this.base, this.address + offset);
               byte thatByte = JvmUtils.unsafe.getByte(that.base, that.address + offset);
               if (thisByte != thatByte) {
                  return false;
               }

               offset++;
               length--;
            }

            return true;
         }
      }
   }

   public int hashCode() {
      if (this.hash != 0) {
         return this.hash;
      } else {
         this.hash = this.hashCode(0, this.size);
         return this.hash;
      }
   }

   public int hashCode(int offset, int length) {
      return (int)XxHash64.hash(this, offset, length);
   }

   public boolean equals(int offset, int length, Slice that, int otherOffset, int otherLength) {
      if (length != otherLength) {
         return false;
      } else if (this == that && offset == otherOffset) {
         return true;
      } else {
         this.checkIndexLength(offset, length);
         that.checkIndexLength(otherOffset, otherLength);

         while (length >= 8) {
            long thisLong = JvmUtils.unsafe.getLong(this.base, this.address + offset);
            long thatLong = JvmUtils.unsafe.getLong(that.base, that.address + otherOffset);
            if (thisLong != thatLong) {
               return false;
            }

            offset += 8;
            otherOffset += 8;
            length -= 8;
         }

         while (length > 0) {
            byte thisByte = JvmUtils.unsafe.getByte(this.base, this.address + offset);
            byte thatByte = JvmUtils.unsafe.getByte(that.base, that.address + otherOffset);
            if (thisByte != thatByte) {
               return false;
            }

            offset++;
            otherOffset++;
            length--;
         }

         return true;
      }
   }

   public String toString(Charset charset) {
      return this.toString(0, this.length(), charset);
   }

   public String toStringUtf8() {
      return this.toString(StandardCharsets.UTF_8);
   }

   public String toString(int index, int length, Charset charset) {
      if (length == 0) {
         return "";
      } else {
         return this.base instanceof byte[]
            ? new String((byte[])this.base, (int)(this.address - Unsafe.ARRAY_BYTE_BASE_OFFSET + index), length, charset)
            : StringDecoder.decodeString(this.toByteBuffer(index, length), charset);
      }
   }

   public ByteBuffer toByteBuffer() {
      return this.toByteBuffer(0, this.size);
   }

   public ByteBuffer toByteBuffer(int index, int length) {
      this.checkIndexLength(index, length);
      if (this.base instanceof byte[]) {
         return ByteBuffer.wrap((byte[])this.base, (int)(this.address - Unsafe.ARRAY_BYTE_BASE_OFFSET + index), length);
      } else {
         try {
            Object[] args = new Object[]{this.address + index, length, this.reference};
            return (ByteBuffer)JvmUtils.newByteBuffer.invokeExact(args);
         } catch (Throwable var4) {
            if (var4 instanceof Error) {
               throw (Error)var4;
            } else if (var4 instanceof RuntimeException) {
               throw (RuntimeException)var4;
            } else {
               if (var4 instanceof InterruptedException) {
                  Thread.currentThread().interrupt();
               }

               throw new RuntimeException(var4);
            }
         }
      }
   }

   public String toString() {
      StringBuilder builder = new StringBuilder("Slice{");
      if (this.base != null) {
         builder.append("base=").append(identityToString(this.base)).append(", ");
      }

      builder.append("address=").append(this.address);
      builder.append(", length=").append(this.length());
      builder.append('}');
      return builder.toString();
   }

   private static String identityToString(Object o) {
      return o == null ? null : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
   }

   private static void copyMemory(Object src, long srcAddress, Object dest, long destAddress, int length) {
      int bytesToCopy = length - length % 8;
      JvmUtils.unsafe.copyMemory(src, srcAddress, dest, destAddress, bytesToCopy);
      JvmUtils.unsafe.copyMemory(src, srcAddress + bytesToCopy, dest, destAddress + bytesToCopy, length - bytesToCopy);
   }

   private void checkIndexLength(int index, int length) {
      Preconditions.checkPositionIndexes(index, index + length, this.length());
   }

   private static long fillLong(byte value) {
      return (value & 255L) << 56
         | (value & 255L) << 48
         | (value & 255L) << 40
         | (value & 255L) << 32
         | (value & 255L) << 24
         | (value & 255L) << 16
         | (value & 255L) << 8
         | value & 255L;
   }

   private static int compareUnsignedBytes(byte thisByte, byte thatByte) {
      return unsignedByteToInt(thisByte) - unsignedByteToInt(thatByte);
   }

   private static int unsignedByteToInt(byte thisByte) {
      return thisByte & 0xFF;
   }

   private static int compareUnsignedLongs(long thisLong, long thatLong) {
      return Long.compare(flipUnsignedLong(thisLong), flipUnsignedLong(thatLong));
   }

   private static long flipUnsignedLong(long thisLong) {
      return thisLong ^ Long.MIN_VALUE;
   }
}
