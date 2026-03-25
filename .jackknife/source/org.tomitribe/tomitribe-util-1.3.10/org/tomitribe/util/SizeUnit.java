package org.tomitribe.util;

public enum SizeUnit {
   BYTES {
      @Override
      public long toBytes(long s) {
         return s;
      }

      @Override
      public long toKilobytes(long s) {
         return s / 1024L;
      }

      @Override
      public long toMegabytes(long s) {
         return s / 1048576L;
      }

      @Override
      public long toGigabytes(long s) {
         return s / 1073741824L;
      }

      @Override
      public long toTerabytes(long s) {
         return s / 1099511627776L;
      }

      @Override
      public long convert(long s, SizeUnit u) {
         return u.toBytes(s);
      }
   },
   KILOBYTES {
      @Override
      public long toBytes(long s) {
         return x(s, 1024L, 9007199254740991L);
      }

      @Override
      public long toKilobytes(long s) {
         return s;
      }

      @Override
      public long toMegabytes(long s) {
         return s / 1024L;
      }

      @Override
      public long toGigabytes(long s) {
         return s / 1048576L;
      }

      @Override
      public long toTerabytes(long s) {
         return s / 1073741824L;
      }

      @Override
      public long convert(long s, SizeUnit u) {
         return u.toKilobytes(s);
      }
   },
   MEGABYTES {
      @Override
      public long toBytes(long s) {
         return x(s, 1048576L, 8796093022207L);
      }

      @Override
      public long toKilobytes(long s) {
         return x(s, 1024L, 9007199254740991L);
      }

      @Override
      public long toMegabytes(long s) {
         return s;
      }

      @Override
      public long toGigabytes(long s) {
         return s / 1024L;
      }

      @Override
      public long toTerabytes(long s) {
         return s / 1048576L;
      }

      @Override
      public long convert(long s, SizeUnit u) {
         return u.toMegabytes(s);
      }
   },
   GIGABYTES {
      @Override
      public long toBytes(long s) {
         return x(s, 1073741824L, 8589934591L);
      }

      @Override
      public long toKilobytes(long s) {
         return x(s, 1048576L, 8796093022207L);
      }

      @Override
      public long toMegabytes(long s) {
         return x(s, 1024L, 9007199254740991L);
      }

      @Override
      public long toGigabytes(long s) {
         return s;
      }

      @Override
      public long toTerabytes(long s) {
         return s / 1024L;
      }

      @Override
      public long convert(long s, SizeUnit u) {
         return u.toGigabytes(s);
      }
   },
   TERABYTES {
      @Override
      public long toBytes(long s) {
         return x(s, 1099511627776L, 8388607L);
      }

      @Override
      public long toKilobytes(long s) {
         return x(s, 1073741824L, 8589934591L);
      }

      @Override
      public long toMegabytes(long s) {
         return x(s, 1048576L, 8796093022207L);
      }

      @Override
      public long toGigabytes(long s) {
         return x(s, 1024L, 9007199254740991L);
      }

      @Override
      public long toTerabytes(long s) {
         return s;
      }

      @Override
      public long convert(long s, SizeUnit u) {
         return u.toTerabytes(s);
      }
   };

   static final long B0 = 1L;
   static final long B1 = 1024L;
   static final long B2 = 1048576L;
   static final long B3 = 1073741824L;
   static final long B4 = 1099511627776L;

   private SizeUnit() {
   }

   static long x(long d, long m, long over) {
      if (d > over) {
         return Long.MAX_VALUE;
      } else {
         return d < -over ? Long.MIN_VALUE : d * m;
      }
   }

   public long toBytes(long size) {
      throw new AbstractMethodError();
   }

   public long toKilobytes(long size) {
      throw new AbstractMethodError();
   }

   public long toMegabytes(long size) {
      throw new AbstractMethodError();
   }

   public long toGigabytes(long size) {
      throw new AbstractMethodError();
   }

   public long toTerabytes(long size) {
      throw new AbstractMethodError();
   }

   public long convert(long sourceSize, SizeUnit sourceUnit) {
      throw new AbstractMethodError();
   }
}
