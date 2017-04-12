// Copyright (c) 2017 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view


import scala.collection.mutable.Buffer

import java.awt.{BorderLayout}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent, ItemEvent, ItemListener}
import java.awt.image.BufferedImage
import javax.swing.{JButton, JComboBox, JOptionPane, JPanel, JToolBar, JToggleButton, JSeparator, JComponent, JTextField, WindowConstants, SwingConstants}
import javax.swing.event.{ChangeListener, ChangeEvent, DocumentListener, DocumentEvent}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, TileContainer}
import bdzimmer.pixeleditor.controller.PalUtil

import bdzimmer.util.StringUtils._



class VMapEntryEditor(
    entries: Array[VMapEntry],
    vMapWindowUpdater: Updater,
    settings: Settings) {

    var vMapEntryIdx = 0

    // widgets to edit the properties of the selected vmapentry

    val pixelsIdxField = new JTextField("0")
    pixelsIdxField.getDocument.addDocumentListener(new DocumentChangeListener() {
      override def changeUpdate(e: DocumentEvent) {
        println("pixelsIdx")
        val pixelsIdx = pixelsIdxField.getText.toIntSafe(0)
        entries(vMapEntryIdx) = entries(vMapEntryIdx).copy(pixelsIdx = pixelsIdx)
        vMapWindowUpdater.update()
      }
    });

    val offsetChoices = (0 to 16).map(x => x * settings.colorsPerTile).toList
    val palOffsetSelector = SettingsDialog.selector(offsetChoices, 0)
    palOffsetSelector.addItemListener(new ItemListener() {
      override def itemStateChanged(e: ItemEvent) {
        if (palOffsetSelector.getSelectedIndex > -1) {
          println("palOffset: " + offsetChoices(palOffsetSelector.getSelectedIndex))
          val palOffset = offsetChoices(palOffsetSelector.getSelectedIndex)
          entries(vMapEntryIdx) = entries(vMapEntryIdx).copy(palOffset = palOffset)
          vMapWindowUpdater.update()
        }
      }
    });

    val flipXButton = new JToggleButton("Flip X")
    flipXButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
        println("flip x")
        entries(vMapEntryIdx) = entries(vMapEntryIdx).copy(flipX = flipXButton.isSelected)
        vMapWindowUpdater.update()
      }
    });

    val flipYButton = new JToggleButton("Flip Y")
    flipXButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
        println("flip y")
        entries(vMapEntryIdx) = entries(vMapEntryIdx).copy(flipY = flipXButton.isSelected)
        vMapWindowUpdater.update()
      }
    });

    // TODO: attribs

    def selectEntry(vMapEntryIdx: Int): Unit = {
      this.vMapEntryIdx = vMapEntryIdx
      val entry = entries(this.vMapEntryIdx)

      pixelsIdxField.setText(entry.pixelsIdx.toString)
      palOffsetSelector.setSelectedItem(entry.palOffset)
      flipXButton.setSelected(entry.flipX)
      flipYButton.setSelected(entry.flipY)

    }


    def components(): List[JComponent] = {
      List(pixelsIdxField, palOffsetSelector, flipXButton, flipYButton)
    }

}


abstract class DocumentChangeListener extends DocumentListener {

  override def insertUpdate(event: DocumentEvent) {
    changeUpdate(event);
  }

  override def removeUpdate(event: DocumentEvent) {
    changeUpdate(event);
  }

  override def changedUpdate(event: DocumentEvent) {
    changeUpdate(event);
  }

  def changeUpdate(event: DocumentEvent)

}
