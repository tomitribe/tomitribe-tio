package org.tomitribe.util.hash;

import java.io.IOException;
import java.io.InputStream;
import sun.misc.Unsafe;

public class XxHash32 {
   private static final int PRIME32_1 = 506952113;
   private static final int PRIME32_2 = 99338871;
   private static final int PRIME32_3 = 1119006269;
   private static final int PRIME32_4 = 668265263;
   private static final int PRIME32_5 = 374761393;
   private static final int DEFAULT_SEED = 0;
   private final int seed;
   private static final long BUFFER_ADDRESS = Unsafe.ARRAY_BYTE_BASE_OFFSET;
   private final byte[] buffer = new byte[16];
   private int bufferSize;
   private long bodyLength;
   private int v1;
   private int v2;
   private int v3;
   private int v4;

   public XxHash32() {
      this(0);
   }

   public XxHash32(int seed) {
      this.seed = seed;
      this.v1 = seed + 506952113 + 99338871;
      this.v2 = seed + 99338871;
      this.v3 = seed;
      this.v4 = seed - 506952113;
   }

   public XxHash32 update(byte[] data) {
      return this.update(data, 0, data.length);
   }

   public XxHash32 update(byte[] data, int offset, int length) {
      Preconditions.checkPositionIndexes(offset, offset + length, data.length);
      this.updateHash(data, Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, length);
      return this;
   }

   public XxHash32 update(Slice data) {
      return this.update(data, 0, data.length());
   }

   public XxHash32 update(Slice data, int offset, int length) {
      Preconditions.checkPositionIndexes(0, offset + length, data.length());
      this.updateHash(data.getBase(), data.getAddress() + offset, length);
      return this;
   }

   public int hash() {
      int hash;
      if (this.bodyLength > 0L) {
         hash = this.computeBody();
      } else {
         hash = this.seed + 374761393;
      }

      hash = (int)(hash + this.bodyLength + this.bufferSize);
      return updateTail(hash, this.buffer, BUFFER_ADDRESS, 0, this.bufferSize);
   }

   private int computeBody() {
      return Integer.rotateLeft(this.v1, 1) + Integer.rotateLeft(this.v2, 7) + Integer.rotateLeft(this.v3, 12) + Integer.rotateLeft(this.v4, 18);
   }

   private void updateHash(Object base, long address, int length) {
      if (this.bufferSize > 0) {
         int available = Math.min(16 - this.bufferSize, length);
         JvmUtils.unsafe.copyMemory(base, address, this.buffer, BUFFER_ADDRESS + this.bufferSize, available);
         this.bufferSize += available;
         address += available;
         length -= available;
         if (this.bufferSize == 16) {
            this.updateBody(this.buffer, BUFFER_ADDRESS, this.bufferSize);
            this.bufferSize = 0;
         }
      }

      if (length >= 16) {
         int index = this.updateBody(base, address, length);
         address += index;
         length -= index;
      }

      if (length > 0) {
         JvmUtils.unsafe.copyMemory(base, address, this.buffer, BUFFER_ADDRESS, length);
         this.bufferSize = length;
      }
   }

   private int updateBody(Object base, long address, int length) {
      int remaining;
      for (remaining = length; remaining >= 16; remaining -= 16) {
         this.v1 = mix(this.v1, JvmUtils.unsafe.getInt(base, address));
         this.v2 = mix(this.v2, JvmUtils.unsafe.getInt(base, address + 4L));
         this.v3 = mix(this.v3, JvmUtils.unsafe.getInt(base, address + 8L));
         this.v4 = mix(this.v4, JvmUtils.unsafe.getInt(base, address + 12L));
         address += 16L;
      }

      int index = length - remaining;
      this.bodyLength += index;
      return index;
   }

   public static long hash(int value) {
      int hash = 374761397;
      hash = updateTail(hash, value);
      hash = finalShuffle(hash);
      return hash;
   }

   public static int hash(String data) {
      return hash(Slices.utf8Slice(data));
   }

   public static int hash(InputStream in) throws IOException {
      return hash(0, in);
   }

   public static int hash(int seed, InputStream in) throws IOException {
      XxHash32 hash = new XxHash32(seed);
      byte[] buffer = new byte[8192];

      while (true) {
         int length = in.read(buffer);
         if (length == -1) {
            return hash.hash();
         }

         hash.update(buffer, 0, length);
      }
   }

   public static int hash(Slice data) {
      return hash(data, 0, data.length());
   }

   public static int hash(int seed, Slice data) {
      return hash(seed, data, 0, data.length());
   }

   public static int hash(Slice data, int offset, int length) {
      return hash(0, data, offset, length);
   }

   public static int hash(int seed, Slice data, int offset, int length) {
      Preconditions.checkPositionIndexes(0, offset + length, data.length());
      Object base = data.getBase();
      long address = data.getAddress() + offset;
      int hash;
      if (length >= 16) {
         hash = updateBody(seed, base, address, length);
      } else {
         hash = seed + 374761393;
      }

      hash += length;
      int index = length & -16;
      return updateTail(hash, base, address, index, length);
   }

   private static int updateTail(int hash, Object base, long address, int index, int length) {
      if (index <= length - 4) {
         hash = updateTail(hash, JvmUtils.unsafe.getInt(base, address + index));
         index += 4;
      }

      while (index < length) {
         hash = updateTail(hash, JvmUtils.unsafe.getByte(base, address + index));
         index++;
      }

      return finalShuffle(hash);
   }

   private static int updateBody(int seed, Object base, long address, int length) {
      int v1 = seed + 506952113 + 99338871;
      int v2 = seed + 99338871;
      int v3 = seed;
      int v4 = seed - 506952113;

      for (int remaining = length; remaining >= 16; remaining -= 16) {
         v1 = mix(v1, JvmUtils.unsafe.getInt(base, address));
         v2 = mix(v2, JvmUtils.unsafe.getInt(base, address + 4L));
         v3 = mix(v3, JvmUtils.unsafe.getInt(base, address + 8L));
         v4 = mix(v4, JvmUtils.unsafe.getInt(base, address + 12L));
         address += 16L;
      }

      return Integer.rotateLeft(v1, 1) + Integer.rotateLeft(v2, 7) + Integer.rotateLeft(v3, 12) + Integer.rotateLeft(v4, 18);
   }

   private static int mix(int current, int value) {
      return Integer.rotateLeft(current + value * 99338871, 13) * 506952113;
   }

   private static int update(int hash, int value) {
      int temp = hash + mix(0, value);
      return temp * 506952113 + 668265263;
   }

   private static int updateTail(int hash, int value) {
      int temp = hash + value * 1119006269;
      return Integer.rotateLeft(temp, 17) * 668265263;
   }

   private static int updateTail(int hash, byte value) {
      int unsigned = value & 255;
      int temp = hash + unsigned * 374761393;
      return Integer.rotateLeft(temp, 11) * 506952113;
   }

   private static int finalShuffle(int hash) {
      hash ^= hash >>> 15;
      hash *= 99338871;
      hash ^= hash >>> 13;
      hash *= 1119006269;
      return hash ^ hash >>> 16;
   }
}
