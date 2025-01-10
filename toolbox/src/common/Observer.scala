package org.smaji.cjkv_toolbox.toolbox

class Pub[T]() {
  val subs= collection.mutable.Set[Sub[T]]()

  def pub(msg: T)= {
    subs.foreach(_.update(msg))
  }

  def add(sub: Sub[T])= synchronized (subs add sub)
  def remove(sub: Sub[T])= synchronized (subs remove sub)
}

trait Sub[T]() {
  def update(msg: T): Unit
}

