package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompositeIterator<T> implements Iterator<T> {
   private final Iterator<Iterator<T>> source;
   private Iterator<T> current;

   private CompositeIterator(Iterator<Iterator<T>> source) {
      this.source = source;
      if (this.source.hasNext()) {
         this.current = (Iterator<T>)this.source.next();
      }
   }

   public boolean hasNext() {
      if (this.current == null) {
         return false;
      } else if (this.current.hasNext()) {
         return true;
      } else if (this.source.hasNext()) {
         this.current = (Iterator<T>)this.source.next();
         return this.hasNext();
      } else {
         return false;
      }
   }

   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return (T)this.current.next();
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }
}
