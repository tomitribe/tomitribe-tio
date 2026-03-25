package org.tomitribe.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
   private TimeUtils() {
   }

   public static String formatMillis(long duration) {
      return format(duration, TimeUnit.MILLISECONDS, min(), max());
   }

   public static String formatMillis(long duration, TimeUnit min, TimeUnit max) {
      return format(duration, TimeUnit.MILLISECONDS, min, max);
   }

   public static String formatNanos(long duration, TimeUnit min, TimeUnit max) {
      return format(duration, TimeUnit.NANOSECONDS, min, max);
   }

   public static String formatNanos(long duration, TimeUnit min) {
      return format(duration, TimeUnit.NANOSECONDS, min, max());
   }

   public static String format(long duration, TimeUnit sourceUnit, TimeUnit min) {
      return format(duration, sourceUnit, min, max());
   }

   public static String format(long duration, TimeUnit sourceUnit) {
      return format(duration, sourceUnit, min(), max());
   }

   private static TimeUnit max() {
      TimeUnit[] values = TimeUnit.values();
      return values[values.length - 1];
   }

   private static TimeUnit min() {
      return TimeUnit.values()[0];
   }

   public static String abbreviateMillis(long duration) {
      return abbreviate(duration, TimeUnit.MILLISECONDS, min(), max());
   }

   public static String abbreviateMillis(long duration, TimeUnit min, TimeUnit max) {
      return abbreviate(duration, TimeUnit.MILLISECONDS, min, max);
   }

   public static String abbreviateNanos(long duration, TimeUnit min, TimeUnit max) {
      return abbreviate(duration, TimeUnit.NANOSECONDS, min, max);
   }

   public static String abbreviateNanos(long duration, TimeUnit min) {
      return abbreviate(duration, TimeUnit.NANOSECONDS, min, max());
   }

   public static String abbreviate(long duration, TimeUnit sourceUnit, TimeUnit min) {
      return abbreviate(duration, sourceUnit, min, max());
   }

   public static String abbreviate(long duration, TimeUnit sourceUnit) {
      return abbreviate(duration, sourceUnit, min(), max());
   }

   public static String abbreviate(long duration, TimeUnit sourceUnit, TimeUnit min, TimeUnit max) {
      String format = format(duration, sourceUnit, min, max);
      return abbreviate(format);
   }

   public static String format(long duration, TimeUnit sourceUnit, TimeUnit min, TimeUnit max) {
      StringBuilder res = new StringBuilder();
      String suffix = "";
      if (duration < 0L) {
         duration *= -1L;
         suffix = " ago";
      }

      for (TimeUnit current = max; duration > 0L; current = TimeUnit.values()[current.ordinal() - 1]) {
         long temp = current.convert(duration, sourceUnit);
         if (temp > 0L) {
            duration -= sourceUnit.convert(temp, current);
            res.append(temp).append(" ").append(current.name().toLowerCase());
            if (temp < 2L) {
               res.deleteCharAt(res.length() - 1);
            }

            res.append(", ");
         }

         if (current == min) {
            break;
         }
      }

      if (res.lastIndexOf(", ") < 0) {
         return "0 " + min.name().toLowerCase();
      } else {
         res.deleteCharAt(res.length() - 1);
         res.deleteCharAt(res.length() - 1);
         int i = res.lastIndexOf(", ");
         if (i > 0) {
            res.deleteCharAt(i);
            res.insert(i, " and");
         }

         res.append(suffix);
         return res.toString();
      }
   }

   public static String formatHighest(long duration, TimeUnit max) {
      TimeUnit[] units = TimeUnit.values();
      StringBuilder res = new StringBuilder();

      for (TimeUnit current = max; duration > 0L; current = units[current.ordinal() - 1]) {
         long temp = current.convert(duration, TimeUnit.MILLISECONDS);
         if (temp > 0L) {
            duration -= current.toMillis(temp);
            res.append(temp).append(" ").append(current.name().toLowerCase());
            if (temp < 2L) {
               res.deleteCharAt(res.length() - 1);
            }
            break;
         }

         if (current == TimeUnit.MILLISECONDS) {
            break;
         }
      }

      return res.toString();
   }

   public static String abbreviate(String time) {
      time = time.replaceAll(" days", "d");
      time = time.replaceAll(" day", "d");
      time = time.replaceAll(" hours", "hr");
      time = time.replaceAll(" hour", "hr");
      time = time.replaceAll(" minutes", "m");
      time = time.replaceAll(" minute", "m");
      time = time.replaceAll(" seconds", "s");
      time = time.replaceAll(" second", "s");
      time = time.replaceAll(" milliseconds", "ms");
      return time.replaceAll(" millisecond", "ms");
   }

   public static String daysAndMinutes(long duration) {
      return formatMillis(duration, TimeUnit.MINUTES, TimeUnit.DAYS);
   }

   public static String hoursAndMinutes(long duration) {
      return formatMillis(duration, TimeUnit.MINUTES, TimeUnit.HOURS);
   }

   public static String hoursAndSeconds(long duration) {
      return formatMillis(duration, TimeUnit.SECONDS, TimeUnit.HOURS);
   }
}
