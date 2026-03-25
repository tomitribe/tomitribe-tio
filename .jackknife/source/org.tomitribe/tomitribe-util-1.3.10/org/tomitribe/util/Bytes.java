package org.tomitribe.util;

public class Bytes implements Comparable<Bytes> {
   private long b;
   private long kb;
   private long mb;
   private long gb;

   public void add(long bytes) {
      this.compact();
      this.b += bytes;
   }

   public long get() {
      this.compact();
      return this.mb;
   }

   private void compact() {
      if (this.b > 1024L) {
         this.kb = this.kb + this.b / 1024L;
         this.b %= 1024L;
      }

      if (this.kb > 1024L) {
         this.mb = this.mb + this.kb / 1024L;
         this.kb %= 1024L;
      }

      if (this.mb > 1024L) {
         this.gb = this.gb + this.mb / 1024L;
         this.mb %= 1024L;
      }
   }

   public int compareTo(Bytes o) {
      this.compact();
      o.compact();
      if (this.gb != o.gb) {
         return this.gb > o.gb ? 1 : -1;
      } else if (this.mb != o.mb) {
         return this.mb > o.mb ? 1 : -1;
      } else if (this.kb != o.kb) {
         return this.kb > o.kb ? 1 : -1;
      } else if (this.b != o.b) {
         return this.b > o.b ? 1 : -1;
      } else {
         return 0;
      }
   }

   public String toString() {
      this.compact();
      if (this.gb > 0L) {
         double n = this.gb + this.mb * 9.76562E-4;
         return String.format("%,.2fgb", n);
      } else if (this.mb > 0L) {
         double n = this.mb + this.kb * 9.76562E-4;
         return String.format("%,.2fmb", n);
      } else if (this.kb > 0L) {
         double n = this.kb + this.b * 9.76562E-4;
         return String.format("%,.2fkb", n);
      } else {
         return this.b + "";
      }
   }
}
