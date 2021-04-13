package game

import java.awt.Graphics2D

trait GameDrawer[DATA] {
  def draw(d:DATA, g:Graphics2D)

}
