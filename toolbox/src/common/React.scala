package org.smaji.cjkv_toolbox.toolbox.react

class Event[T] private () {
  def map[N](fn: Function1[T, N], oneshot: Boolean= false)= {
    val next= Event[N]()
    val glue= (x: T)=> {
      val n= fn(x)
      next.update(n)
    }
    subs add glue
    next
  }

  def oneshot[N](fn: Function1[T, N])= {
    val next= Event[N]()
    val glue= new Function1[T, Unit] {
      def apply(x: T)= {
        val n= fn(x)
        next update n
        subs remove this
      }
    }
    subs add glue
    next
  }

  def reset()= {
    subs.clear()
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
    el.foreach(_.subs add next.update)
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
      next update n
    }
    subs add glue
    next
  }

  def oneshot[N](fn: Function1[T, N])= {
    val newValue= fn(value)
    val next= Signal[N](newValue)
    val glue= new Function1[T, Unit] {
      def apply(x: T)= {
        val n= fn(x)
        next update n
        subs remove this
      }
    }
    subs add glue
    next
  }

  def reset()= {
    subs.clear()
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
      val newValue= sl.foldLeft(value)
        ((acc, s)=> fn(acc, s.value))
      next.update(newValue)
    }
    sl.foreach(_.subs add glue)
    next
  }

  def allMatch[T](predicate: Function1[T, Boolean])(sl: Signal[T]*)= {
    def forall()= sl.map(_.value).forall(predicate)
    val next= Signal(forall())
    val glue= (x: T)=> {
      val newValue=
        if predicate(x) then
          forall()
        else
          false
      next.update(newValue)
    }
    sl.foreach(_.subs add glue)
    next
  }

  def allMatch(sl: Signal[Boolean]*): Signal[Boolean]=
    allMatch[Boolean](identity)(sl*)

  def noneMatch[T](predicate: Function1[T, Boolean])(sl: Signal[T]*)=
    allMatch[T](!predicate(_))(sl*)

  def noneMatch(sl: Signal[Boolean]*)=
    allMatch[Boolean](!identity(_))(sl*)

  def anyMatch[T](predicate: Function1[T, Boolean])(sl: Signal[T]*)= {
    def exist()= sl.map(_.value).exists(predicate)
    val next= Signal(exist())
    val glue= (x: T)=> {
      val newValue=
        if predicate(x) then
          true
        else
          exist()
      next.update(newValue)
    }
    sl.foreach(_.subs add glue)
    next
  }

  def anyMatch(sl: Signal[Boolean]*): Signal[Boolean]=
    anyMatch[Boolean](identity)(sl*)
}

