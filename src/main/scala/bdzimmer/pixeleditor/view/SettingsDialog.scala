// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view


import java.awt.{Dialog, GridLayout}                  // scalastyle:ignore illegal.imports
import java.awt.event.{ActionListener, ActionEvent}   // scalastyle:ignore illegal.imports
import javax.swing.{JButton, JDialog, JComboBox, JLabel}

import bdzimmer.pixeleditor.model.TileCollectionModel.Settings

import bdzimmer.util.StringUtils._


object SettingsDialog {

  // TODO: many built in settings that are selectable

  val Default = Settings(5, 256, 16, 16, 16, 256, 16, 16)


  def selector(items: List[Int], default: Int): JComboBox[String] = {
    val sel = new JComboBox[String](items.map(_.toString).toArray)
    sel.setEditable(true)
    sel.setSelectedItem(default.toString)
    sel
  }


  def getSettings(): Settings = {

      val dialog = new JDialog()
      dialog.setTitle("Tile Collection Settings")
      dialog.setLayout(new GridLayout(0, 2, 0, 0))

      dialog.add(new JLabel("Bits per Channel"))
      val bpc = selector((1 to 8).toList, Default.bitsPerChannel)
      dialog.add(bpc)

      dialog.add(new JLabel("Palette Size"))
      val ps = selector((4 to 8).map(math.pow(2, _).toInt).toList, Default.paletteSize)
      dialog.add(ps)

      dialog.add(new JLabel("Colors per Tile"))
      val cpt = selector((1 to 8).map(math.pow(2, _).toInt).toList, Default.colorsPerTile)
      dialog.add(cpt)

      dialog.add(new JLabel("Tile Width"))
      val tw = selector((2 to 10).map(math.pow(2, _).toInt).toList, Default.tileWidth)
      dialog.add(tw)

      dialog.add(new JLabel("Tile Height"))
      val th = selector((2 to 10).map(math.pow(2, _).toInt).toList, Default.tileHeight)
      dialog.add(th)

      dialog.add(new JLabel("VMap Size"))
      val vs = selector((4 to 10).map(math.pow(2, _).toInt).toList, Default.vMapSize)
      dialog.add(vs)

      dialog.add(new JLabel("View Palette Columns"))
      val vpc = selector(List(8, 16, 32), Default.viewPaletteCols)
      dialog.add(vpc)

      dialog.add(new JLabel("View Tile Columns"))
      val vtc = selector(List(8, 16, 32), Default.viewTileCols)
      dialog.add(vtc)


      // add an ok button
      val ok = new JButton("OK")
      ok.addActionListener(new ActionListener() {
        def actionPerformed(e: ActionEvent): Unit = {
          dialog.dispose
        }
      })
      dialog.add(ok)

      dialog.pack
      dialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE)
      dialog.setLocationRelativeTo(null)
      dialog.setVisible(true)

      // wait for it to close

      Settings(
        bpc.getEditor.getItem.toString.toIntSafe(0),
        ps.getEditor.getItem.toString.toIntSafe(0),
        cpt.getEditor.getItem.toString.toIntSafe(0),
        tw.getEditor.getItem.toString.toIntSafe(0),
        th.getEditor.getItem.toString.toIntSafe(0),
        vs.getEditor.getItem.toString.toIntSafe(0),
        vpc.getEditor.getItem.toString.toIntSafe(0),
        vtc.getEditor.getItem.toString.toIntSafe(0)
      )

  }

}
