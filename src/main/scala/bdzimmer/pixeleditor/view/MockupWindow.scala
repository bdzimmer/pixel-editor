// Copyright (c) 2017 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import javax.swing.{JTextField, JComboBox, JOptionPane}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, TileProperties, TileContainer}
import bdzimmer.pixeleditor.controller.{IO, TileUtil}
import bdzimmer.pixeleditor.model.{Map => Background}

import bdzimmer.util.StringUtils._

import java.io.File


sealed abstract class MockupElement(
  val dirname: String,
  val filename: String) {

}

final case class BackgroundElement(
    id: String,
    bg: Background, tilesId: String,
    dir: String) extends MockupElement(dir, id)

final case class TileCollectionElement(
  id: String,
  tc: TileCollection,
  vMapIdx: Int,
  palConfIdx: Int,
  palOffset: Int,
  dir: String) extends MockupElement(dir, id)


class MockupWindow(title: String, dirName: String)
  extends BufferNamedWindow[MockupElement](title, Buffer()) {

  val paletteSize = 256 // TODO: MockupWindow settings
  val tileContainer = new TileContainer
  val globalPalette = TileUtil.colorArray(paletteSize)

  override def buildUpdater(item: Named[MockupElement]): WidgetUpdater = {
    new MockupElementUpdater(item)
  }


  override def buildItem(): Option[Named[MockupElement]] = {
    val elementType = new JComboBox(Array("Background", "Tiles"))

    val option = JOptionPane.showConfirmDialog(
        null, Array("Type:", elementType), "Add Mockup Element", JOptionPane.OK_CANCEL_OPTION)

    if (option == JOptionPane.OK_OPTION) {

      val res = TileCollectionWindow.fileChooser(dirName, false)

      res.map({case (parent, name) => {
        val filename = parent / name
        val file = new File(filename)
        elementType.getSelectedIndex match {
          case 0 => BackgroundElement(name, new Background(file), "", parent) named name
          case 1 => TileCollectionElement(
              name,
              IO.readCollection(file),
              0, 0, 0, parent) named name
        }
      }})
    } else {
      None
    }
  }


  override def editAction(idx: Int): Unit = {
     val item = items(idx)

     // TODO: get tiles and tile properties for background element

     val editor = item.value match {
       case x: BackgroundElement     => new MapEditorWindow(x.dirname, x.bg, x.filename, globalPalette, null, null, tileContainer)
       case x: TileCollectionElement => new TileCollectionWindow(x.filename, x.tc, x.dirname, x.filename)
     }

     editor.setLocationRelativeTo(null) // TODO: set location from saved window location settings
     editor.setVisible(true)
  }


  class MockupElementUpdater(element: Named[MockupElement]) extends WidgetUpdater {

    // no palette needed here, will use global palette

    val image: TilesetImage = ???

    draw()
    val widget = new ImageWidget(element.name, image.indexedGraphics.getImage, List(), 0, 24)

    def draw(): Unit = {
      println("MockupElementUpdater draw")
      image.draw(false, false)
    }

    def update(): Unit = {
      draw()
      widget.repaint()
    }

  }


}
