package org.scalaquery.util

import java.io.Closeable

/**
 * An Iterator with a `close` method to close the underlying data source.
 * Implementers must close the data source when `hasNext` returns `false`.
 */
trait CloseableIterator[+T] extends Iterator[T] with Closeable { self =>

  /**
   * Close the underlying data source. The behaviour of any methods of this
   * object after closing it is undefined.
   */
  override def close(): Unit

  override def map[B](f: T => B): CloseableIterator[B] = new CloseableIterator[B] {
    def hasNext = self.hasNext
    def next() = f(self.next())
    def close() = self.close()
  }

  final def use[R](f: (Iterator[T] => R)): R =
    try f(this) finally close()

  final def use[R](f: =>R): R =
    try f finally close()

  protected final def noNext = throw new NoSuchElementException("next on empty iterator")
}

object CloseableIterator {

  /**
   * An empty CloseableIterator
   */
  val empty: CloseableIterator[Nothing] = new CloseableIterator[Nothing] {
    def hasNext = false
    def next() = noNext
    def close() {}
  }

  /**
   * A CloseableIterator which contains exactly one item.
   */
  class Single[+T](item: T) extends CloseableIterator[T] {
    private var more = true
    def hasNext = more
    def next() = if(more) { more = false; item } else noNext
    def close {}
  }
}
