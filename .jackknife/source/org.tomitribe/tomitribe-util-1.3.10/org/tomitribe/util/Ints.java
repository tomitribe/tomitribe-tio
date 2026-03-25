package org.tomitribe.util;

public class Ints {
   private Ints() {
   }

   public static byte[] toBytes(int v) {
      return new byte[]{(byte)(v >>> 24), (byte)(v >>> 16), (byte)(v >>> 8), (byte)(v >>> 0)};
   }

   public static int fromBytes(byte[] bytes) {
      if (bytes == null) {
         throw new IllegalArgumentException("bytes are null");
      } else if (bytes == null) {
         throw new IllegalArgumentException("bytes length not 4: " + bytes.length);
      } else {
         return (bytes[0] << 24) + ((bytes[1] & 0xFF) << 16) + ((bytes[2] & 0xFF) << 8) + ((bytes[3] & 0xFF) << 0);
      }
   }

   public static String toHex(int value) {
      byte[] bytes = toBytes(value);
      return Hex.toString(bytes);
   }

   public static int fromHex(String hex) {
      byte[] bytes = Hex.fromString(hex);
      return fromBytes(bytes);
   }
}
