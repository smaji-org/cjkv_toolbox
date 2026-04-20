package org.smaji.cjkv_toolbox.toolbox.react

class Event[T]() {
  def this(value: T)= {
    this()
    this.last= Some(value)
  }
  var last: Option[T]= None
  val subs= collection.mutable.Set[Function1[T, Unit]]()

  def map[N](fn: Function1[T, N])= {
    val next= Event[N]()
    val glue= (x: T)=> {
        val n= fn(x)
        next.update(n)
      }
    synchronized (subs add glue)
    next
  }

  def update(value: T)= {
    last= Some(value)
    subs.foreach(_(value))
  }
}

object Event {
  def select[T](el: Event[T]*)= {
    val next= Event[T]()
    el.foreach(e=>e.synchronized(e.subs add next.update))
    next
  }
}

class Signal[T](var value: T) {
  val subs= collection.mutable.Set[Function1[T, Unit]]()

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

  def update(newValue: T)= {
    if (newValue != value){
      value= newValue
      subs.foreach(_(value))
    }
  }
}

object Signal {
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

