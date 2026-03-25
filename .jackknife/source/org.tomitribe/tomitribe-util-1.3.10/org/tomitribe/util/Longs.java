package org.tomitribe.util;

import java.nio.charset.Charset;

public class Longs {
   private Longs() {
   }

   public static byte[] toBytes(long v) {
      return new byte[]{
         (byte)(v >>> 56), (byte)(v >>> 48), (byte)(v >>> 40), (byte)(v >>> 32), (byte)(v >>> 24), (byte)(v >>> 16), (byte)(v >>> 8), (byte)(v >>> 0)
      };
   }

   public static long fromBytes(byte[] bytes) {
      if (bytes == null) {
         throw new IllegalArgumentException("bytes are null");
      } else if (bytes == null) {
         throw new IllegalArgumentException("bytes length not 8: " + bytes.length);
      } else {
         return ((long)bytes[0] << 56)
            + ((long)(bytes[1] & 255) << 48)
            + ((long)(bytes[2] & 255) << 40)
            + ((long)(bytes[3] & 255) << 32)
            + ((long)(bytes[4] & 255) << 24)
            + ((bytes[5] & 255) << 16)
            + ((bytes[6] & 255) << 8)
            + ((bytes[7] & 255) << 0);
      }
   }

   public static String toHex(long value) {
      byte[] bytes = toBytes(value);
      return Hex.toString(bytes);
   }

   public static long fromHex(String hex) {
      byte[] bytes = Hex.fromString(hex);
      return fromBytes(bytes);
   }

   public static String toBase32(long value) {
      byte[] bytes = toBytes(value);
      return Base32.encode(bytes);
   }

   public static long fromBase32(String base32) {
      try {
         byte[] bytes = Base32.decode(base32);
         return fromBytes(bytes);
      } catch (Base32.DecodingException var2) {
         throw new IllegalStateException(var2);
      }
   }

   public static String toBase64(long value) {
      byte[] bytes = toBytes(value);
      return new String(Base64.encodeBase64(bytes), Charset.forName("UTF-8"));
   }

   public static long fromBase64(String base64) {
      byte[] bytes = Base64.decodeBase64(base64.getBytes(Charset.forName("UTF-8")));
      return fromBytes(bytes);
   }

   public static String toBase58(long value) {
      byte[] bytes = toBytes(value);
      return Base58.encode(bytes);
   }

   public static long fromBase58(String base58) {
      byte[] bytes = Base58.decode(base58);
      return fromBytes(bytes);
   }
}
