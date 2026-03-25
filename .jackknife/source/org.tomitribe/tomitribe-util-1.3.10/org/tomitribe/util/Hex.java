package org.tomitribe.util;

import java.util.regex.Pattern;

public class Hex {
   private static final Pattern valid = Pattern.compile("^[A-Fa-f0-9]+$");
   protected static final char[] hexArray = "0123456789abcdef".toCharArray();

   private Hex() {
   }

   public static String toString(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];

      for (int j = 0; j < bytes.length; j++) {
         int v = bytes[j] & 255;
         hexChars[j * 2] = hexArray[v >>> 4];
         hexChars[j * 2 + 1] = hexArray[v & 15];
      }

      return new String(hexChars);
   }

   public static byte[] fromString(String s) {
      if (s == null) {
         throw new IllegalArgumentException("hex string is null");
      } else if (s.length() == 0) {
         return new byte[0];
      } else if (!valid.matcher(s).matches()) {
         throw new Hex.InvalidHexFormatException(s);
      } else {
         int len = s.length();
         byte[] data = new byte[len / 2];

         for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
         }

         return data;
      }
   }

   public static class InvalidHexFormatException extends IllegalArgumentException {
      private final String string;

      public InvalidHexFormatException(String string) {
         super(String.format("Invalid hex string '%s'", string));
         this.string = string;
      }

      public String getString() {
         return this.string;
      }
   }
}
