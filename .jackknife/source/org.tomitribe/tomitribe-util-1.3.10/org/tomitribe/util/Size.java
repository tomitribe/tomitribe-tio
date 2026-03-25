package org.tomitribe.util;

import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.tomitribe.util.editor.Editors;

@XmlJavaTypeAdapter(Size.Adapter.class)
public class Size implements Comparable<Size> {
   private long size;
   private SizeUnit unit;

   public Size() {
   }

   public Size(long size, SizeUnit unit) {
      this.size = size;
      this.unit = unit;
   }

   public Size(String string) {
      this(string, null);
   }

   public Size(String string, SizeUnit defaultUnit) {
      String[] strings = string.split(",| and ");
      Size total = new Size();

      for (String s : strings) {
         Size part = new Size();
         s = s.trim();
         StringBuilder t = new StringBuilder();
         StringBuilder u = new StringBuilder();

         int i;
         for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c) && (i != 0 || c != '-') && (i <= 0 || c != '.')) {
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

         part.unit = parseUnit(u.toString());
         if (part.unit == null) {
            part.unit = defaultUnit;
         }

         String size = t.toString();
         if (size.contains(".")) {
            if (part.unit == null) {
               throw new IllegalArgumentException("unit must be specified with floating point numbers");
            }

            double d = Double.parseDouble(size);
            long bytes = part.unit.toBytes(1L);
            part.size = (long)(bytes * d);
            part.unit = SizeUnit.BYTES;
         } else {
            part.size = Integer.parseInt(size);
         }

         total = total.add(part);
      }

      this.size = total.size;
      this.unit = total.unit;
   }

   public long getSize() {
      return this.size;
   }

   public long getSize(SizeUnit unit) {
      return unit.convert(this.size, this.unit);
   }

   public void setSize(long size) {
      this.size = size;
   }

   public SizeUnit getUnit() {
      return this.unit;
   }

   public void setUnit(SizeUnit unit) {
      this.unit = unit;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Size that = (Size)o;
         Size.Normalize n = new Size.Normalize(this, that);
         return n.a == n.b;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = (int)(this.size ^ this.size >>> 32);
      return 31 * result + (this.unit != null ? this.unit.hashCode() : 0);
   }

   public Size add(Size that) {
      Size.Normalize n = new Size.Normalize(this, that);
      return new Size(n.a + n.b, n.base);
   }

   public Size subtract(Size that) {
      Size.Normalize n = new Size.Normalize(this, that);
      return new Size(n.a - n.b, n.base);
   }

   public Size to(SizeUnit unit) {
      return new Size(unit.convert(this.size, this.unit), unit);
   }

   public static Size parse(String text) {
      return new Size(text);
   }

   private static void invalidFormat(String text) {
      throw new IllegalArgumentException("Illegal size format: '" + text + "'.  Valid examples are '10kb' or '10 kilobytes'.");
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.size);
      if (this.unit != null) {
         sb.append(abbreviate(this.unit));
      }

      return sb.toString();
   }

   private static SizeUnit parseUnit(String u) {
      if (u.length() == 0) {
         return null;
      } else if ("BYTES".equalsIgnoreCase(u)) {
         return SizeUnit.BYTES;
      } else if ("BYTE".equalsIgnoreCase(u)) {
         return SizeUnit.BYTES;
      } else if ("B".equalsIgnoreCase(u)) {
         return SizeUnit.BYTES;
      } else if ("KILOBYTES".equalsIgnoreCase(u)) {
         return SizeUnit.KILOBYTES;
      } else if ("KILOBYTE".equalsIgnoreCase(u)) {
         return SizeUnit.KILOBYTES;
      } else if ("KB".equalsIgnoreCase(u)) {
         return SizeUnit.KILOBYTES;
      } else if ("K".equalsIgnoreCase(u)) {
         return SizeUnit.KILOBYTES;
      } else if ("MEGABYTES".equalsIgnoreCase(u)) {
         return SizeUnit.MEGABYTES;
      } else if ("MEGABYTE".equalsIgnoreCase(u)) {
         return SizeUnit.MEGABYTES;
      } else if ("MB".equalsIgnoreCase(u)) {
         return SizeUnit.MEGABYTES;
      } else if ("M".equalsIgnoreCase(u)) {
         return SizeUnit.MEGABYTES;
      } else if ("GIGABYTES".equalsIgnoreCase(u)) {
         return SizeUnit.GIGABYTES;
      } else if ("GIGABYTE".equalsIgnoreCase(u)) {
         return SizeUnit.GIGABYTES;
      } else if ("GB".equalsIgnoreCase(u)) {
         return SizeUnit.GIGABYTES;
      } else if ("G".equalsIgnoreCase(u)) {
         return SizeUnit.GIGABYTES;
      } else if ("TERABYTES".equalsIgnoreCase(u)) {
         return SizeUnit.TERABYTES;
      } else if ("TERABYTE".equalsIgnoreCase(u)) {
         return SizeUnit.TERABYTES;
      } else if ("TB".equalsIgnoreCase(u)) {
         return SizeUnit.TERABYTES;
      } else if ("T".equalsIgnoreCase(u)) {
         return SizeUnit.TERABYTES;
      } else {
         throw new IllegalArgumentException("Unknown size unit '" + u + "'.  Supported units " + Join.join(", ", lowercase(SizeUnit.values())));
      }
   }

   private static String abbreviate(SizeUnit u) {
      switch (u) {
         case BYTES:
            return "bytes";
         case KILOBYTES:
            return "kb";
         case MEGABYTES:
            return "mb";
         case GIGABYTES:
            return "gb";
         case TERABYTES:
            return "tb";
         default:
            throw new IllegalArgumentException("Unknown size unit '" + u + "'.  Supported units " + Join.join(", ", lowercase(SizeUnit.values())));
      }
   }

   public int compareTo(Size that) {
      Size.Normalize n = new Size.Normalize(this, that);
      if (n.a > n.b) {
         return 1;
      } else {
         return n.a == n.b ? 0 : -1;
      }
   }

   private SizeUnit lowest(SizeUnit a, SizeUnit b) {
      int min = Math.min(a.ordinal(), b.ordinal());
      return SizeUnit.values()[min];
   }

   private static List<String> lowercase(Enum... units) {
      List<String> list = new ArrayList();

      for (Enum unit : units) {
         list.add(unit.name().toLowerCase());
      }

      return list;
   }

   static {
      PropertyEditorManager.registerEditor(Size.class, Size.SizeEditor.class);
      Editors.get(Size.class);
   }

   public static class Adapter extends XmlAdapter<String, Size> {
      public Size unmarshal(String v) throws Exception {
         return new Size(v);
      }

      public String marshal(Size v) throws Exception {
         return v.toString();
      }
   }

   private static class Normalize {
      private long a;
      private long b;
      private SizeUnit base;

      private Normalize(Size a, Size b) {
         this.base = lowest(a, b);
         this.a = a.unit == null ? a.size : this.base.convert(a.size, a.unit);
         this.b = b.unit == null ? b.size : this.base.convert(b.size, b.unit);
      }

      private static SizeUnit lowest(Size a, Size b) {
         if (a.unit == null) {
            return b.unit;
         } else if (b.unit == null) {
            return a.unit;
         } else if (a.size == 0L) {
            return b.unit;
         } else {
            return b.size == 0L ? a.unit : SizeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
         }
      }
   }

   public static class SizeEditor extends PropertyEditorSupport {
      public void setAsText(String text) {
         Size d = Size.parse(text);
         this.setValue(d);
      }
   }
}
