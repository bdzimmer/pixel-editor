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
