package cl.uchile.dcc.citric
package model.panels

import model.panels.classes.DropPanel
import model.panels.traits.Panel
import model.units.classes.PlayerCharacter

import scala.collection.mutable.ArrayBuffer

class DropPanelTest extends munit.FunSuite{

  /* Values and variables for the test*/

  /* Default player for testing */
  private val name = "testPlayer"
  private val maxHp = 10
  private val attack = 1
  private val defense = 1
  private val evasion = 1
  /* Add any other constants you need here... */
  private var stars = 0
  private var wins = 0
  private var currentHP = maxHp
  private val defaultNorm = 1
  private var currentNorm = 1
  private var normObjective = "stars"
  /*
  This is the object under test.
  We initialize it in the beforeEach method so we can reuse it in all the tests.
  This is a good practice because it will reset the object before each test, so you don't have
  to worry about the state of the object between tests.
  */
  private var player1: PlayerCharacter = _ // <- x = _ is the same as x = null
  private var player2: PlayerCharacter = _
  private var player3: PlayerCharacter = _

  private var panel1: Panel = _
  private var panel2: Panel = _
  private var panel3: Panel = _

  //initialize array buffer of characters and panels to create the DropPanel

  private var dropPanel: DropPanel = _

  // This method is executed before each `test(...)` method.
  override def beforeEach(context: BeforeEach): Unit = {
    player1 = new PlayerCharacter(name, maxHp, attack, defense, evasion,
      wins, defaultNorm, currentNorm, normObjective)
    player2 = new PlayerCharacter(name, maxHp, attack, defense, evasion,
      wins, defaultNorm, currentNorm, normObjective)
    player3 = new PlayerCharacter(name, maxHp, attack, defense, evasion,
      wins, defaultNorm, currentNorm, normObjective)

    val characters: ArrayBuffer[PlayerCharacter] = ArrayBuffer[PlayerCharacter](player1, player2, player3)
    val nextPanels: ArrayBuffer[Panel] = ArrayBuffer[Panel](panel1, panel2, panel3)

    //Init the panel
    dropPanel = new DropPanel(characters, nextPanels) {
    }
  }

  // 1. Test invariant properties
  test("A DropPanel should have correctly set their attributes") {
    assertEquals(dropPanel.characters, ArrayBuffer[PlayerCharacter](player1, player2, player3))
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3))
  }

  // 2. Test methods
  test("A DropPanel should add a character to the list of characters currently on this panel") {
    dropPanel.addCharacter(player1)
    assertEquals(dropPanel.characters, ArrayBuffer[PlayerCharacter](player1, player2, player3, player1))
  }

  test("A DropPanel should remove a character to the list of characters currently on this panel") {
    assertEquals(dropPanel.characters, ArrayBuffer[PlayerCharacter](player1, player2, player3))
    dropPanel.removeCharacter(player1)
    assertEquals(dropPanel.characters, ArrayBuffer[PlayerCharacter](player2, player3))
  }

  test("A DropPanel should add a panel to the list of NextPanels if necessary to build/modify the board") {
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3))
    val panel4: Panel = new DropPanel(ArrayBuffer[PlayerCharacter](player1, player2), ArrayBuffer[Panel](panel1, panel2))
    dropPanel.addPanel(panel4)
    assertNotEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3))
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3, panel4))
  }

  test("A DropPanel should remove a panel from the list of NextPanels if necessary to build/modify the board") {
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3))
    //remove a panel that already exist in the list of NextPanels of the panel itself
    dropPanel.removePanel(panel3)
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2))
    assertNotEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2, panel3))
    //remove a panel that doesn't exist in the list of NextPanels of the panel itself
    val panel4: Panel = new DropPanel(ArrayBuffer[PlayerCharacter](player1, player2), ArrayBuffer[Panel](panel1, panel2))
    dropPanel.removePanel(panel4)
    assertEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1, panel2))
    assertNotEquals(dropPanel.nextPanels, ArrayBuffer[Panel](panel1))
  }
  
  test("A character should be able to decrease their stars by landing in a DropPanel") {
    val other = new PlayerCharacter(name, maxHp, attack, defense, evasion,
      wins, defaultNorm, currentNorm, normObjective)
    assertEquals(player1.stars, other.stars)
    dropPanel.dropStars(player1)
    assert(player1.stars < other.stars)
  }
  
}
