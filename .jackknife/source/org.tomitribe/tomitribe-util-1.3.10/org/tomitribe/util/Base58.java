package org.tomitribe.util;

import java.util.Arrays;

public class Base58 {
   public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
   private static final char ENCODED_ZERO = ALPHABET[0];
   private static final int[] INDEXES = new int[128];

   private Base58() {
   }

   public static String encode(byte[] input) {
      if (input.length == 0) {
         return "";
      } else {
         int zeros = 0;

         while (zeros < input.length && input[zeros] == 0) {
            zeros++;
         }

         input = Arrays.copyOf(input, input.length);
         char[] encoded = new char[input.length * 2];
         int outputStart = encoded.length;
         int inputStart = zeros;

         while (inputStart < input.length) {
            encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
            if (input[inputStart] == 0) {
               inputStart++;
            }
         }

         while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            outputStart++;
         }

         while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
         }

         return new String(encoded, outputStart, encoded.length - outputStart);
      }
   }

   public static byte[] decode(String input) throws Base58.InvalidFormatException {
      if (input.length() == 0) {
         return new byte[0];
      } else {
         byte[] input58 = new byte[input.length()];

         for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
               throw new Base58.InvalidFormatException.InvalidCharacter(c, i);
            }

            input58[i] = (byte)digit;
         }

         int zeros = 0;

         while (zeros < input58.length && input58[zeros] == 0) {
            zeros++;
         }

         byte[] decoded = new byte[input.length()];
         int outputStart = decoded.length;
         int inputStart = zeros;

         while (inputStart < input58.length) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
               inputStart++;
            }
         }

         while (outputStart < decoded.length && decoded[outputStart] == 0) {
            outputStart++;
         }

         return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
      }
   }

   private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
      int remainder = 0;

      for (int i = firstDigit; i < number.length; i++) {
         int digit = number[i] & 255;
         int temp = remainder * base + digit;
         number[i] = (byte)(temp / divisor);
         remainder = temp % divisor;
      }

      return (byte)remainder;
   }

   static {
      Arrays.fill(INDEXES, -1);
      int i = 0;

      while (i < ALPHABET.length) {
         INDEXES[ALPHABET[i]] = i++;
      }
   }

   public static class InvalidFormatException extends IllegalArgumentException {
      public InvalidFormatException() {
      }

      public InvalidFormatException(String message) {
         super(message);
      }

      public static class InvalidCharacter extends Base58.InvalidFormatException {
         public final char character;
         public final int position;

         public InvalidCharacter(char character, int position) {
            super("Invalid character '" + Character.toString(character) + "' at position " + position);
            this.character = character;
            this.position = position;
         }
      }
   }
}
