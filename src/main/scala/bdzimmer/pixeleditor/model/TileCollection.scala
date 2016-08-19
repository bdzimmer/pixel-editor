// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.immutable.Seq


case class ColorTriple(val r: Int, val g: Int, val b: Int)



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

  import java.awt.event.{ActionEvent, ActionListener}   // scalastyle:ignore illegal.imports
  import java.awt.{GridLayout, BorderLayout, Dimension}
  import javax.swing.border.EmptyBorder
  import javax.swing._

  import java.awt.event.{FocusAdapter, FocusEvent}

  import bdzimmer.pixeleditor.view.PaletteEditorNew
  import bdzimmer.pixeleditor.view.ImageWidget


  class WidgetWindow(worldObjects: IndexedSeq[ImageWidget], title: String) extends JFrame {

    val serialVersionUID = 1L;
    val margin = 20

    setTitle(title);

    val scrollingSurface = new JPanel()
    scrollingSurface.setLayout(new GridLayout(worldObjects.length, 1, margin, margin));

    val widgetWidth = worldObjects(0).wx

    val scrollPane = new JScrollPane()
    scrollPane.getVerticalScrollBar().setUnitIncrement(20)
    scrollPane.setViewportView(scrollingSurface)
    scrollPane.setPreferredSize(new Dimension(widgetWidth + margin, widgetWidth + margin))
    scrollPane.setOpaque(true)

    // this is important, otherwise there will be extra padding
    // and a horizontal scrollbar will be created.
    scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0))

    refresh()

    // TODO: what happens when there are not enough components for a vertical scrollbar?
    val scrollBar = scrollPane.getVerticalScrollBar()
    scrollPane.remove(scrollBar);

    setLayout(new BorderLayout())
    add(scrollPane, BorderLayout.CENTER)
    add(scrollBar, BorderLayout.EAST)
    pack()


    addFocusListener(new FocusAdapter() {
      override def focusGained(event: FocusEvent): Unit = {
        println("widget window Focus gained!");
        repaint()
      }
    })
    setFocusable(true)

    // TODO: is this necessary?
    repaint()

    setVisible(true)

    private def refresh(): Unit = {
      scrollingSurface.removeAll()
      val surfaceWidth = worldObjects(0).wx + margin
      val surfaceHeight = worldObjects.map(_.wy).sum + margin
      val panelHeight = worldObjects(0).wy * 3 + margin * 2
      scrollingSurface.setPreferredSize(new Dimension(surfaceWidth, surfaceHeight))
      worldObjects.foreach(wo => scrollingSurface.add(wo))
      scrollingSurface.repaint()
      repaint()
    }

  }

  def paletteChunkWidget(name: String, palette: Array[ColorTriple], bitsPerChannel: Int): ImageWidget = {
    val cols = 16
    val swatchSize = 32
    val image = PaletteEditorNew.imageForPalette(palette.length, cols, swatchSize)
    val rows = palette.length / 16
    PaletteEditorNew.drawPalette(image, palette, bitsPerChannel, rows, cols, swatchSize)


    val edit = new JButton("Edit")
    edit.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        new PaletteEditorNew(palette, 6, name).setVisible(true)
      }
    })
    edit.setFocusable(false)

    val res = new ImageWidget(name, image, List(edit))
    res
  }

}


object Baloney {


    def main(args: Array[String]): Unit = {

    def pal = (0 until 32).map(_ => ColorTriple(0, 20, 0)).toArray

    val widgets = List("Cave Floor", "Cave Walls", "Baloney", "Cheese", "Snowstorm").map(
          name => TileCollectionViews.paletteChunkWidget(name, pal, 6)).toIndexedSeq

    new TileCollectionViews.WidgetWindow(widgets, "Palette Chunks")

    // val junk = new bdzimmer.pixeleditor.view.PaletteEditorNew(pal, 6, "demo palette")
    // junk.setVisible(true)

    // new bdzimmer.pixeleditor.view.PaletteEditorNew(pal, 6)

  }
}
