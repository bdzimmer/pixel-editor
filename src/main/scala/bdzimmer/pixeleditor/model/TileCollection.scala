// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.immutable.Seq
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.ImageWidget


case class ColorTriple(val r: Int, val g: Int, val b: Int)


trait WidgetUpdater {
  val widget: ImageWidget
  def update(): Unit
}


object TileCollection {

  case class Settings(
    bitsPerColor: Int,
    paletteSize: Int,
    colorsPerTile: Int,
    tileWidth: Int,
    tileHeight: Int,
    vmapSize: Int
   )

  case class Pixels(
    colorsPerTile: Int,
    tiles: Array[Tile]
  )

  case class VMap(
    palConfs: Seq[PaletteConf],
    entries:  Seq[VMapEntry]
  )


  case class VMapEntry(
    pixelsIdx: Int,
    palIdx: Int,
    flipX: Boolean,
    flipY: Boolean,
    attribs: TileProperties
  )


  case class PaletteConf(
    chunks: Seq[(Int, Seq[ColorTriple])]
  )

}



// experimental view classes

object TileCollectionViews {

  import java.awt.event.{ActionEvent, ActionListener}
  import java.awt.{GridLayout, BorderLayout, Dimension}
  import javax.swing.border.EmptyBorder
  import javax.swing._

  import java.awt.event.{FocusAdapter, FocusEvent}

  import bdzimmer.pixeleditor.view.PaletteEditorNew
  import java.awt.image.BufferedImage


  class PaletteChunkUpdater(
      chunk: PaletteChunk, bitsPerChannel: Int,
      cols: Int, swatchSize: Int) extends WidgetUpdater {

    val rows = (chunk.pal.length + cols - 1) / cols
    val image = PaletteEditorNew.imageForPalette(chunk.pal.length, cols, swatchSize)

    val edit = new JButton("Edit")
    edit.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        new PaletteEditorNew(chunk.name, chunk.pal, 6, PaletteChunkUpdater.this).setVisible(true)
      }
    })
    edit.setFocusable(false)

    draw()
    val widget = new ImageWidget(chunk.name, image, List(edit), 64, 24)

    def draw(): Unit = {
      println("PaletteChunkUpdater draw")
      PaletteEditorNew.drawPalette(image, chunk.pal, bitsPerChannel, rows, cols, swatchSize)
    }

    def update(): Unit = {
      println("PaletteChunkUpdater update")
      draw()
      widget.repaint()
    }

  }


  // a UI helper case class
  case class PaletteChunk(val name: String, pal: Array[ColorTriple])


  class PaletteChunksWindow(
      title: String,
      val chunks: ArrayBuffer[PaletteChunk],
      bitsPerChannel: Int,
      cols: Int, swatchSize: Int) extends JFrame {

    val widgets = chunks.map(chunk => {
      val updater = new PaletteChunkUpdater(chunk, bitsPerChannel, cols, swatchSize)
      updater.widget
    })

    setTitle(title)

    val scrollPane = new WidgetScroller(widgets)

    setLayout(new BorderLayout())
    add(scrollPane, BorderLayout.CENTER)
    add(scrollPane.scrollBar, BorderLayout.EAST)

    addFocusListener(new FocusAdapter() {
      override def focusGained(event: FocusEvent): Unit = {
        println("palette chunks window focus gained!");
        repaint()
      }
    })
    setFocusable(true)

    rebuild()
    pack()
    setVisible(true)

    def add(chunk: PaletteChunk): Unit = {
      chunks  += chunk
      widgets += new PaletteChunkUpdater(chunk, bitsPerChannel, cols, swatchSize).widget
    }

    def rebuild(): Unit = {
      scrollPane.rebuild()
      repaint()
    }

  }


  class WidgetScroller(widgets: ArrayBuffer[ImageWidget]) extends JScrollPane {

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
    // call after adding or removing widgets from the buffer
    def rebuild(): Unit = {

      println("WidgetScroller rebuild")

      scrollingSurface.removeAll()
      // scrollingSurface.setLayout(new GridLayout(widgets.length, 1, margin, margin));
      val surfaceWidth = widgets.map(_.wx).max + margin
      val surfaceHeight = widgets.map(_.wy + margin).sum
      scrollingSurface.setPreferredSize(new Dimension(surfaceWidth, surfaceHeight))
      widgets.foreach(widget => {
        println("adding " + widget.title)
        scrollingSurface.add(widget)
      })

      val paneHeight = math.min(surfaceHeight, surfaceWidth)
      setPreferredSize(new Dimension(surfaceWidth, paneHeight))
      scrollingSurface.repaint()
      repaint() // TODO: is this necessary

    }
  }
}


object Experiment {

  def main(args: Array[String]): Unit = {

    def pal = (0 until 32).map(_ => ColorTriple(0, 0, 0)).toArray
    val chunks = ArrayBuffer("Cave Floor", "Cave Walls", "Baloney", "Cheese", "Snowstorm").map(
          name => TileCollectionViews.PaletteChunk(name, pal.clone()))

    val pc = new TileCollectionViews.PaletteChunksWindow("Palette Chunks", chunks, 6, 16, 16)

    val largePal = pal.clone() ++ pal.clone()
    pc.add(TileCollectionViews.PaletteChunk("Cavern", largePal))
    pc.rebuild()

  }
}
