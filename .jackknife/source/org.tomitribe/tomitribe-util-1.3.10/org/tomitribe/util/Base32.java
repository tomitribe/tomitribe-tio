package org.tomitribe.util;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class Base32 {
   private static final int SECRET_SIZE = 10;
   private static final SecureRandom RANDOM = new SecureRandom();
   private static final Base32 INSTANCE = new Base32("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");
   private String ALPHABET;
   private char[] DIGITS;
   private int MASK;
   private int SHIFT;
   private HashMap<Character, Integer> CHAR_MAP;
   static final String SEPARATOR = "-";

   static Base32 getInstance() {
      return INSTANCE;
   }

   protected Base32(String alphabet) {
      this.ALPHABET = alphabet;
      this.DIGITS = this.ALPHABET.toCharArray();
      this.MASK = this.DIGITS.length - 1;
      this.SHIFT = Integer.numberOfTrailingZeros(this.DIGITS.length);
      this.CHAR_MAP = new HashMap();

      for (int i = 0; i < this.DIGITS.length; i++) {
         this.CHAR_MAP.put(this.DIGITS[i], i);
      }
   }

   public static byte[] decode(String encoded) throws Base32.DecodingException {
      return getInstance().decodeInternal(encoded);
   }

   protected byte[] decodeInternal(String encoded) throws Base32.DecodingException {
      encoded = encoded.trim().replaceAll("-", "").replaceAll(" ", "");
      encoded = encoded.replaceFirst("[=]*$", "");
      encoded = encoded.toUpperCase(Locale.US);
      if (encoded.length() == 0) {
         return new byte[0];
      } else {
         int encodedLength = encoded.length();
         int outLength = encodedLength * this.SHIFT / 8;
         byte[] result = new byte[outLength];
         int buffer = 0;
         int next = 0;
         int bitsLeft = 0;

         for (char c : encoded.toCharArray()) {
            if (!this.CHAR_MAP.containsKey(c)) {
               throw new Base32.DecodingException("Illegal character: " + c);
            }

            buffer <<= this.SHIFT;
            buffer |= this.CHAR_MAP.get(c) & this.MASK;
            bitsLeft += this.SHIFT;
            if (bitsLeft >= 8) {
               result[next++] = (byte)(buffer >> bitsLeft - 8);
               bitsLeft -= 8;
            }
         }

         return result;
      }
   }

   public static String encode(byte[] data) {
      return getInstance().encodeInternal(data);
   }

   protected String encodeInternal(byte[] data) {
      if (data.length == 0) {
         return "";
      } else if (data.length >= 268435456) {
         throw new IllegalArgumentException();
      } else {
         int outputLength = (data.length * 8 + this.SHIFT - 1) / this.SHIFT;
         StringBuilder result = new StringBuilder(outputLength);
         int buffer = data[0];
         int next = 1;
         int bitsLeft = 8;

         while (bitsLeft > 0 || next < data.length) {
            if (bitsLeft < this.SHIFT) {
               if (next < data.length) {
                  buffer <<= 8;
                  buffer |= data[next++] & 255;
                  bitsLeft += 8;
               } else {
                  int pad = this.SHIFT - bitsLeft;
                  buffer <<= pad;
                  bitsLeft += pad;
               }
            }

            int index = this.MASK & buffer >> bitsLeft - this.SHIFT;
            bitsLeft -= this.SHIFT;
            result.append(this.DIGITS[index]);
         }

         return result.toString();
      }
   }

   public static String random() {
      byte[] buffer = new byte[10];
      RANDOM.nextBytes(buffer);
      byte[] secretKey = Arrays.copyOf(buffer, 10);
      return encode(secretKey);
   }

   public static class DecodingException extends Exception {
      public DecodingException(String message) {
         super(message);
      }
   }
}
