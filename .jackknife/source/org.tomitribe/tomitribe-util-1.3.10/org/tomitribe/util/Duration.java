package org.tomitribe.util;

import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.tomitribe.util.editor.Editors;

public class Duration implements Comparable<Duration> {
   private long time;
   private TimeUnit unit;

   public Duration() {
   }

   public Duration(long time, TimeUnit unit) {
      this.time = time;
      this.unit = unit;
   }

   public Duration(String string) {
      this(string, null);
   }

   public Duration(String string, TimeUnit defaultUnit) {
      String[] strings = string.split(",| and ");
      Duration total = new Duration();

      for (String value : strings) {
         Duration part = new Duration();
         String s = value.trim();
         StringBuilder t = new StringBuilder();
         StringBuilder u = new StringBuilder();

         int i;
         for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c) && (i != 0 || c != '-')) {
               break;
            }

            t.append(c);
         }

         if (t.length() == 0) {
            invalidFormat(s);
         }

         while (i < s.length()) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
               break;
            }

            i++;
         }

         for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
               u.append(c);
            } else {
               invalidFormat(s);
            }
         }

         part.time = Long.parseLong(t.toString());
         part.unit = parseUnit(u.toString());
         if (part.unit == null) {
            part.unit = defaultUnit;
         }

         total = total.add(part);
      }

      this.time = total.time;
      this.unit = total.unit;
   }

   public long getTime() {
      return this.time;
   }

   public long getTime(TimeUnit unit) {
      return unit.convert(this.time, this.unit);
   }

   public void setTime(long time) {
      this.time = time;
   }

   public TimeUnit getUnit() {
      return this.unit;
   }

   public void setUnit(TimeUnit unit) {
      this.unit = unit;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Duration that = (Duration)o;
         Duration.Normalize n = new Duration.Normalize(this, that);
         return n.a == n.b;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = (int)(this.time ^ this.time >>> 32);
      return 31 * result + this.unit.hashCode();
   }

   public Duration add(Duration that) {
      Duration.Normalize n = new Duration.Normalize(this, that);
      return new Duration(n.a + n.b, n.base);
   }

   public Duration subtract(Duration that) {
      Duration.Normalize n = new Duration.Normalize(this, that);
      return new Duration(n.a - n.b, n.base);
   }

   public static Duration parse(String text) {
      return new Duration(text);
   }

   private static void invalidFormat(String text) {
      throw new IllegalArgumentException("Illegal duration format: '" + text + "'.  Valid examples are '10s' or '10 seconds'.");
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.time);
      if (this.unit != null) {
         sb.append(" ");
         sb.append(this.unit);
      }

      return sb.toString();
   }

   private static TimeUnit parseUnit(String u) {
      if (u.length() == 0) {
         return null;
      } else if ("NANOSECONDS".equalsIgnoreCase(u)) {
         return TimeUnit.NANOSECONDS;
      } else if ("NANOSECOND".equalsIgnoreCase(u)) {
         return TimeUnit.NANOSECONDS;
      } else if ("NANOS".equalsIgnoreCase(u)) {
         return TimeUnit.NANOSECONDS;
      } else if ("NANO".equalsIgnoreCase(u)) {
         return TimeUnit.NANOSECONDS;
      } else if ("NS".equalsIgnoreCase(u)) {
         return TimeUnit.NANOSECONDS;
      } else if ("MICROSECONDS".equalsIgnoreCase(u)) {
         return TimeUnit.MICROSECONDS;
      } else if ("MICROSECOND".equalsIgnoreCase(u)) {
         return TimeUnit.MICROSECONDS;
      } else if ("MICROS".equalsIgnoreCase(u)) {
         return TimeUnit.MICROSECONDS;
      } else if ("MICRO".equalsIgnoreCase(u)) {
         return TimeUnit.MICROSECONDS;
      } else if ("MILLISECONDS".equalsIgnoreCase(u)) {
         return TimeUnit.MILLISECONDS;
      } else if ("MILLISECOND".equalsIgnoreCase(u)) {
         return TimeUnit.MILLISECONDS;
      } else if ("MILLIS".equalsIgnoreCase(u)) {
         return TimeUnit.MILLISECONDS;
      } else if ("MILLI".equalsIgnoreCase(u)) {
         return TimeUnit.MILLISECONDS;
      } else if ("MS".equalsIgnoreCase(u)) {
         return TimeUnit.MILLISECONDS;
      } else if ("SECONDS".equalsIgnoreCase(u)) {
         return TimeUnit.SECONDS;
      } else if ("SECOND".equalsIgnoreCase(u)) {
         return TimeUnit.SECONDS;
      } else if ("SEC".equalsIgnoreCase(u)) {
         return TimeUnit.SECONDS;
      } else if ("S".equalsIgnoreCase(u)) {
         return TimeUnit.SECONDS;
      } else if ("MINUTES".equalsIgnoreCase(u)) {
         return TimeUnit.MINUTES;
      } else if ("MINUTE".equalsIgnoreCase(u)) {
         return TimeUnit.MINUTES;
      } else if ("MIN".equalsIgnoreCase(u)) {
         return TimeUnit.MINUTES;
      } else if ("M".equalsIgnoreCase(u)) {
         return TimeUnit.MINUTES;
      } else if ("HOURS".equalsIgnoreCase(u)) {
         return TimeUnit.HOURS;
      } else if ("HOUR".equalsIgnoreCase(u)) {
         return TimeUnit.HOURS;
      } else if ("HRS".equalsIgnoreCase(u)) {
         return TimeUnit.HOURS;
      } else if ("HR".equalsIgnoreCase(u)) {
         return TimeUnit.HOURS;
      } else if ("H".equalsIgnoreCase(u)) {
         return TimeUnit.HOURS;
      } else if ("DAYS".equalsIgnoreCase(u)) {
         return TimeUnit.DAYS;
      } else if ("DAY".equalsIgnoreCase(u)) {
         return TimeUnit.DAYS;
      } else if ("D".equalsIgnoreCase(u)) {
         return TimeUnit.DAYS;
      } else {
         throw new IllegalArgumentException("Unknown time unit '" + u + "'.  Supported units " + Join.join(", ", lowercase(TimeUnit.values())));
      }
   }

   public int compareTo(Duration that) {
      Duration.Normalize n = new Duration.Normalize(this, that);
      if (n.a > n.b) {
         return 1;
      } else {
         return n.a == n.b ? 0 : -1;
      }
   }

   private static List<String> lowercase(Enum... units) {
      List<String> list = new ArrayList();

      for (Enum unit : units) {
         list.add(unit.name().toLowerCase());
      }

      return list;
   }

   static {
      PropertyEditorManager.registerEditor(Duration.class, Duration.DurationEditor.class);
      Editors.get(Duration.class);
   }

   public static class DurationEditor extends PropertyEditorSupport {
      public void setAsText(String text) {
         Duration d = Duration.parse(text);
         this.setValue(d);
      }
   }

   private static class Normalize {
      private long a;
      private long b;
      private TimeUnit base;

      private Normalize(Duration a, Duration b) {
         this.base = lowest(a, b);
         this.a = a.unit == null ? a.time : this.base.convert(a.time, a.unit);
         this.b = b.unit == null ? b.time : this.base.convert(b.time, b.unit);
      }

      private static TimeUnit lowest(Duration a, Duration b) {
         if (a.unit == null) {
            return b.unit;
         } else if (b.unit == null) {
            return a.unit;
         } else if (a.time == 0L) {
            return b.unit;
         } else {
            return b.time == 0L ? a.unit : TimeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
         }
      }
   }
}
