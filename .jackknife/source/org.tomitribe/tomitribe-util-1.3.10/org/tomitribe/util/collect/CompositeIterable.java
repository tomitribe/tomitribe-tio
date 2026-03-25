package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompositeIterable<T> implements Iterator<T> {
   private final Iterator<Iterable<T>> archives;
   private Iterator<T> current;

   private CompositeIterable(Iterable<Iterable<T>> archives) {
      this.archives = archives.iterator();
      if (this.archives.hasNext()) {
         this.current = ((Iterable)this.archives.next()).iterator();
      }
   }

   public boolean hasNext() {
      if (this.current == null) {
         return false;
      } else if (this.current.hasNext()) {
         return true;
      } else if (this.archives.hasNext()) {
         this.current = ((Iterable)this.archives.next()).iterator();
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
