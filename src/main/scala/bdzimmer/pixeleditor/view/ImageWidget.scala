// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view


import java.awt.{BorderLayout, Component, Dimension, GridLayout} // scalastyle:ignore illegal.imports
import java.awt.{Color, Graphics, Font}               // scalastyle:ignore illegal.imports
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}   // scalastyle:ignore illegal.imports
import java.awt.image.BufferedImage                   // scalastyle:ignore illegal.imports
import java.io.File

import javax.swing.{JButton, JComponent, JPanel, SwingConstants, JScrollPane}
import javax.swing.border.EmptyBorder

import scala.collection.mutable.Buffer


class ImageWidget(
    val title: String,
    image: BufferedImage,
    buttons: List[JButton],
    buttonWidth: Int = 100,
    yOffset: Int = 0) extends JComponent {

  val wx = image.getWidth + buttonWidth
  val wy = image.getHeight + yOffset

  private var selected = false

  setAlignmentX(Component.RIGHT_ALIGNMENT);

  setLayout(new BorderLayout())

  val buttonPanel = new JPanel()
  buttonPanel.setPreferredSize(new Dimension(buttonWidth, getWidth))
  buttonPanel.setLayout(new GridLayout(buttons.length, 1, 0, 0))
  buttonPanel.setBackground(Color.black)

  buttons.foreach(buttonPanel.add(_))
  add(buttonPanel, BorderLayout.EAST)

  override def getPreferredSize(): Dimension = new Dimension(wx, wy)

  override def getSize(): Dimension = new Dimension(wx, wy)

  override def paintComponent(graphics: Graphics): Unit = {
    super.paintComponent(graphics)

    println("ImageWidget paintComponent")

    graphics.setColor(Color.black)
    graphics.fillRect(0, 0, getWidth, getHeight)
    graphics.drawImage(image, 0, yOffset, null)

    graphics.setFont(new Font("Monospace", Font.BOLD, 12))
    graphics.setColor(Color.white)
    graphics.drawString(title, 5, 15)

    if (selected) {
      graphics.setColor(new Color(128, 128, 128))
      graphics.drawRect(0, 0, wx, wy)
      graphics.drawRect(1, 1, wx - 2, wy - 2)
      graphics.drawRect(2, 2, wx - 4, wy - 4)
    }
  }


  def setSelected(selected: Boolean): Unit = {
    this.selected = selected
  }

  def getSelected(): Boolean = {
    selected
  }

}


object ImageWidget {
  val DefaultWidth = 320
  val DefaultHeight = 200
}



// single-column scrollpane of image widgets of potentially different heights and widths
// they can also be selected
class WidgetScroller(widgets: Buffer[ImageWidget]) extends JScrollPane {

  private var selectedIdx = 0

  val margin = 5
  val scrollingSurface = new JPanel()

  getVerticalScrollBar().setUnitIncrement(20)
  setViewportView(scrollingSurface)
  setOpaque(true)

  // this is important; if left out, there will be extra padding
  // and a horizontal scrollbar will be created.
  setBorder(new EmptyBorder(0, 0, 0, 0))

  rebuild()

  val scrollBar = getVerticalScrollBar()
  remove(scrollBar)

  // rebuild the scrolling pane and redraw
  // call after adding or removing widgets
  def rebuild(): Unit = {

    println("WidgetScroller rebuild")

    // TODO: change the order of these operations to be more logical
    scrollingSurface.removeAll()
    // the widgets can have different heights, so we don't want gridlayout
    // scrollingSurface.setLayout(new GridLayout(widgets.length, 1, margin, margin));
    val surfaceWidth = widgets.map(_.wx).max + margin
    val surfaceHeight = widgets.map(_.wy + margin).sum
    scrollingSurface.setPreferredSize(new Dimension(surfaceWidth, surfaceHeight))
    widgets.foreach(widget => {
      println("adding " + widget.title)

      if (widget.getMouseListeners.length == 0) {
        widget.addMouseListener(new MouseAdapter() {
          override def mouseClicked(event: MouseEvent): Unit = {
            if (selectedIdx < widgets.length) {
              widgets(selectedIdx).setSelected(false)
            }
            selectedIdx = widgets.indexOf(widget)
            println("highlighting widget " + selectedIdx)
            widgets(selectedIdx).setSelected(true)
            WidgetScroller.this.repaint()
          }
        })
      }

      scrollingSurface.add(widget)
    })

    val paneHeight = math.min(surfaceHeight, surfaceWidth)
    setPreferredSize(new Dimension(surfaceWidth, paneHeight))
    scrollingSurface.revalidate()
    scrollingSurface.repaint()
    repaint() // TODO: is this necessary

  }

  def getSelectedIdx(): Int = {
    selectedIdx
  }

}
