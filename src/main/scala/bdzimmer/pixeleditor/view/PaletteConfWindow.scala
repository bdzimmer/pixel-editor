// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import javax.swing.{BoxLayout, JButton, JComboBox, JLabel, JPanel, WindowConstants}
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Component, Dimension, GridLayout, FlowLayout}
import java.awt.image.BufferedImage

import bdzimmer.pixeleditor.model.Color
import bdzimmer.pixeleditor.model.TileCollectionModel._


class PaletteConfWindow(
    title: String,
    paletteChunks: Buffer[Named[Array[Color]]],
    settings: Settings) extends CommonWindow {

  setTitle(title)

  val cols = settings.viewPaletteCols

  val conf: Buffer[Int] = Buffer()

  private val palImages: Buffer[ImagePanel] = Buffer()

  build(WindowConstants.HIDE_ON_CLOSE)
  rebuild()
  setResizable(false)

  /// ///

  def rebuild(): Unit = {
    println("palette conf window rebuild")

    palImages.clear()
    panel.removeAll()

    // panel.setLayout(new GridLayout(conf.length + 1, 1, 5, 5))
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS))
    var palOffset = 0

    val controlHeight = chunkSelector().getPreferredSize.getHeight.toInt

    def rowButton(label: String): JButton = {
      val button = new JButton(label)
      button.setPreferredSize(new Dimension(48, controlHeight))
      button.setMaximumSize(button.getPreferredSize())
      button.setAlignmentY(Component.TOP_ALIGNMENT)
      button
    }

    for (i <- 0 until conf.length) {

      val rowPanel = new JPanel()
      rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.LINE_AXIS))

      val removeButton = rowButton("-")
      removeButton.addActionListener(new ActionListener {
        override def actionPerformed(ae: ActionEvent): Unit = {
          conf.remove(i)
          rebuild()
        }
      })
      rowPanel.add(removeButton)

      val upButton = rowButton("^")
      upButton.addActionListener(new ActionListener {
        override def actionPerformed(ae: ActionEvent): Unit = {
          if (i > 0) {
            val temp = conf(i - 1)
            conf(i - 1) = conf(i)
            conf(i) = temp
          }
          rebuild()
        }
      })
      rowPanel.add(upButton)

      val downButton = rowButton("v")
      downButton.addActionListener(new ActionListener {
        override def actionPerformed(ae: ActionEvent): Unit = {
          if (i < conf.length - 1) {
            val temp = conf(i + 1)
            conf(i + 1) = conf(i)
            conf(i) = temp
          }
          rebuild()
        }
      })
      rowPanel.add(downButton)

      if (conf(i) >= paletteChunks.length) {
        conf(i) = paletteChunks.length - 1
      }
      val chunkIdx = conf(i)

      val selector = chunkSelector()
      selector.setSelectedIndex(chunkIdx)
      selector.setMaximumSize(selector.getPreferredSize)
      selector.setAlignmentY(Component.TOP_ALIGNMENT)
      selector.addActionListener(new ActionListener {
        override def actionPerformed(ae: ActionEvent): Unit = {
          conf(i) = ae.getSource.asInstanceOf[JComboBox[String]].getSelectedIndex
          rebuild()
        }
      })
      rowPanel.add(selector)

      val selectedChunk = paletteChunks(chunkIdx)
      val pal = selectedChunk.value

      println("selected " + selectedChunk.name + " " + pal.length)
      val label = new JLabel(" " + palOffset + " - " + (palOffset + pal.length - 1))
      label.setPreferredSize(new Dimension(64, controlHeight))
      label.setMaximumSize(label.getPreferredSize)
      label.setAlignmentY(Component.TOP_ALIGNMENT)
      rowPanel.add(label)

      val rows = (pal.length + cols - 1) / cols
      val palImage = PaletteWindow.imageForPalette(pal.length, cols, PaletteConfWindow.SwatchSize)
      PaletteWindow.drawPalette(
          palImage, pal, settings.bitsPerChannel, rows, cols, PaletteConfWindow.SwatchSize)

      val palImagePanel = new ImagePanel(palImage)
      palImagePanel.setAlignmentY(Component.TOP_ALIGNMENT)
      palImagePanel.repaint()

      palImages += palImagePanel
      rowPanel.add(palImagePanel)

      rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
      panel.add(rowPanel)

      palOffset += selectedChunk.value.length
    }

    val addPanel = new JPanel()
    addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.LINE_AXIS))
    val addButton = rowButton("+")
    addButton.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        conf += 0
        rebuild()
      }
    })
    addPanel.add(addButton)
    addPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    panel.add(addPanel)

    panel.revalidate()
    panel.repaint()
    pack()
  }

  def chunkSelector(): JComboBox[String] = {
    new JComboBox(paletteChunks.map(_.name).toArray)
  }


  /// ///


  override def buildPanel(): JPanel = {
    new JPanel()
  }


  override def onFocus(): Unit = {
    println("PaletteConfWindow focus gained")

    // redraw the palette images using the configuration
    for (i <- 0 until conf.length) {
      val pal = paletteChunks(conf(i)).value
      val rows = (pal.length + cols - 1) / cols
      val imagePanel = palImages(i)
      PaletteWindow.drawPalette(
          imagePanel.getImage, pal, settings.bitsPerChannel, rows, cols, PaletteConfWindow.SwatchSize)
      imagePanel.repaint()
    }
    repaint()
  }

}


object PaletteConfWindow {
    val SwatchSize = 16
}