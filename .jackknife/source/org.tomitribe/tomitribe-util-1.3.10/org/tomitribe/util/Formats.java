package org.tomitribe.util;

public class Formats {
   private Formats() {
   }

   public static String asDateTime(long milliseconds) {
      return String.format("%tF %<tT", milliseconds);
   }
}
