package org.tomitribe.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class PrintString extends PrintStream {
   private final ByteArrayOutputStream baos = (ByteArrayOutputStream)this.out;

   public PrintString() {
      this(512);
   }

   public PrintString(int size) {
      super(new ByteArrayOutputStream(size), true);
   }

   public byte[] toByteArray() {
      this.flush();
      return this.baos.toByteArray();
   }

   public String toString(String charsetName) throws UnsupportedEncodingException {
      this.flush();
      return this.baos.toString(charsetName);
   }

   public int size() {
      this.flush();
      return this.baos.size();
   }

   public String toString() {
      this.flush();
      return this.baos.toString();
   }
}
