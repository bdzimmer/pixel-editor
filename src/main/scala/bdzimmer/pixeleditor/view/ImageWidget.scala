// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view


import java.awt.{BorderLayout, Component, Dimension, GridLayout} // scalastyle:ignore illegal.imports
import java.awt.{Color, Graphics, Font}               // scalastyle:ignore illegal.imports
import java.awt.event.{ActionEvent, ActionListener}   // scalastyle:ignore illegal.imports
import java.awt.image.BufferedImage                   // scalastyle:ignore illegal.imports
import java.io.File

import javax.swing.{JButton, JComponent, JPanel, SwingConstants}
import javax.swing.border.EmptyBorder


class ImageWidget(
    val title: String,
    image: BufferedImage,
    buttons: List[JButton],
    buttonWidth: Int = 100,
    yOffset: Int = 0) extends JComponent {

  val wx = image.getWidth + buttonWidth
  val wy = image.getHeight + yOffset

  setAlignmentX(Component.RIGHT_ALIGNMENT);

  setLayout(new BorderLayout())

  val buttonPanel = new JPanel()
  buttonPanel.setPreferredSize(new Dimension(buttonWidth, this.getWidth))
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
    graphics.fillRect(0, 0, this.getWidth, this.getHeight)
    graphics.drawImage(image, 0, yOffset, null)

    graphics.setFont(new Font("Monospace", Font.BOLD, 12))
    graphics.setColor(Color.white)
    graphics.drawString(title, 5, 15)
  }

}


object ImageWidget {
  val DefaultWidth = 320
  val DefaultHeight = 200
}
