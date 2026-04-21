package org.smaji.cjkv_toolbox.toolbox.react

class Event[T] private () {
  def map[N](fn: Function1[T, N])= {
    val next= Event[N]()
    val glue= (x: T)=> {
        val n= fn(x)
        next.update(n)
      }
    synchronized (subs add glue)
    next
  }

  private val subs= collection.mutable.Set[Function1[T, Unit]]()
  private def update(value: T)= {
    subs.foreach(_(value))
  }
}

object Event {
  def create[T]()= {
    val event= Event[T]()
    val update= (value: T)=> event.update(value)
    (event, update)
  }

  def select[T](el: Event[T]*)= {
    val next= Event[T]()
    el.foreach(e=>e.synchronized(e.subs add next.update))
    next
  }
}

class Signal[T] private (var value: T) {
  private val subs= collection.mutable.Set[Function1[T, Unit]]()

  def get()= value

  def map[N](fn: Function1[T, N])= {
    val newValue= fn(value)
    val next= Signal[N](newValue)
    val glue= (x: T)=> {
      val n= fn(x)
      next.update(n)
    }
    synchronized (subs add glue)
    next
  }

  private def update(newValue: T)= {
    if (newValue != value){
      value= newValue
      subs.foreach(_(value))
    }
  }
}

object Signal {
  def create[T](value: T)=
    val signal=Signal[T](value)
    val update=(value: T)=> signal.update(value)
    (signal, update)

  def merge[T, N](sl: Seq[Signal[T]], fn: Function2[N, T, N], value: N)= {
    val newValue= sl.foldLeft(value)
      ((acc, s)=> fn(acc, s.value))
    val next= Signal(newValue)
    val glue= (x: T)=> {
        val acc= sl.foldLeft(value)
          ((acc, s)=> fn(acc, s.value))
        next.update(acc)
      }
    sl.foreach(e=>e.synchronized(e.subs add glue))
    next
  }
}

