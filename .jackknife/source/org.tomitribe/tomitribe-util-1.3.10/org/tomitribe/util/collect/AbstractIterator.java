package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractIterator<T> implements Iterator<T> {
   private T next;

   public boolean hasNext() {
      if (this.next != null) {
         return true;
      } else {
         try {
            this.next = this.advance();
            return this.next != null;
         } catch (NoSuchElementException var2) {
            return false;
         }
      }
   }

   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         T v = this.next;
         this.next = null;
         return v;
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   protected abstract T advance() throws NoSuchElementException;
}
