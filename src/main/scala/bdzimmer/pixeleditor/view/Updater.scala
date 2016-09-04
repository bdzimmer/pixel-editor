// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Abstraction for things that need to be redrawn.

package bdzimmer.pixeleditor.view

import java.awt.Component


trait Updater {
  def update(): Unit
}


trait WidgetUpdater extends Updater {
  val widget: ImageWidget
}


class DumbUpdater(component: Component) extends Updater {
  def update(): Unit = {
    component.repaint()
  }
}


trait Container[T] {
  def get(): T
  def set(t: T): Unit
}


class ArrayContainer[T](items: Array[T], idx: Int) extends Container[T] {

  def get(): T = {
    items(idx)
  }

  def set(t: T): Unit = {
    items(idx) = t
  }

}


class SimpleContainer[T](var t: T) extends Container[T] {

  def get(): T = t

  def set(t: T): Unit = {
    this.t = t
  }

}