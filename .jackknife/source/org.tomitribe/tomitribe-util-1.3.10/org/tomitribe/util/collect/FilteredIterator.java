package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilteredIterator<T> extends AbstractIterator<T> implements Iterator<T> {
   private final Iterator<T> iterator;
   private final FilteredIterator.Filter<T> filter;

   public FilteredIterator(Iterator<T> iterator, FilteredIterator.Filter<T> filter) {
      if (iterator == null) {
         throw new IllegalArgumentException("iterator cannot be null");
      } else if (filter == null) {
         throw new IllegalArgumentException("filter cannot be null");
      } else {
         this.iterator = iterator;
         this.filter = filter;
      }
   }

   @Override
   protected T advance() throws NoSuchElementException {
      T next = null;

      while ((next = (T)this.iterator.next()) != null) {
         if (this.filter.accept(next)) {
            return next;
         }
      }

      throw new NoSuchElementException();
   }

   public interface Filter<T> {
      boolean accept(T var1);
   }
}
