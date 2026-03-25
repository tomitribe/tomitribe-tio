package org.tomitribe.util;

import java.util.BitSet;

public class Binary {
   private Binary() {
   }

   public static byte[] toBytes(String binaryString) {
      return toBitSet(binaryString).toByteArray();
   }

   public static BitSet toBitSet(String binaryString) {
      BitSet set = new BitSet(binaryString.length());
      StringBuilder sb = new StringBuilder(binaryString);

      for (int i = 0; i < sb.length(); i++) {
         set.set(i, '1' == sb.charAt(i));
      }

      return set;
   }

   public static String toString(byte[] bytes) {
      BitSet set = BitSet.valueOf(bytes);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < set.length(); i++) {
         sb.append(set.get(i) ? "1" : "0");
      }

      while (sb.length() % 8 != 0) {
         sb.insert(0, "0");
      }

      return sb.toString();
   }
}
