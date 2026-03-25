package org.tomitribe.util.collect;

import java.util.Iterator;

public class FilteredIterable<T> implements Iterable<T> {
   private final Iterable<T> iterable;
   private final FilteredIterator.Filter<T> filter;

   public FilteredIterable(Iterable<T> iterable, FilteredIterator.Filter<T> filter) {
      this.iterable = iterable;
      this.filter = filter;
   }

   public Iterator<T> iterator() {
      return new FilteredIterator<>(this.iterable.iterator(), this.filter);
   }
}
