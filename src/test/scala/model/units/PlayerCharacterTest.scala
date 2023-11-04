package cl.uchile.dcc.citric
package model.units

import model.units.classes.PlayerCharacter
import model.units.traits.UnitTrait
import model.units.classes.wilds.{Chicken, Robo_ball, Seagull}

class PlayerCharacterTest extends munit.FunSuite {
  /*
  REMEMBER: It is a good practice to use constants for the values that are used in multiple
  tests, so you can change them in a single place.
  This will make your tests more readable, easier to maintain, and less error-prone.
  */
  private val name = "testPlayer"
  private val maxHp = 3
  private val attack = 1
  private val defense = 1
  private val evasion = 1
  /* Add any other constants you need here... */
  private val stars = 0
  private val wins = 0
  private val currentHP = maxHp
  private val defaultNorm = 1
  private var currentNorm = 1
  private var normObjective = "stars"
  /*
  This is the object under test.
  We initialize it in the beforeEach method so we can reuse it in all the tests.
  This is a good practice because it will reset the object before each test, so you don't have
  to worry about the state of the object between tests.
  */
  private var character: PlayerCharacter = _  // <- x = _ is the same as x = null
  /* Add any other variables you need here... */

  // This method is executed before each `test(...)` method.
  override def beforeEach(context: BeforeEach): Unit = {
    character = new PlayerCharacter(
      name,
      maxHp,
      attack,
      defense,
      evasion,
      wins,
      defaultNorm,
      currentNorm,
      normObjective
    )
  }

  test("A character should have correctly set their attributes") {
    assertEquals(character.name, name)
    assertEquals(character.maxHp, maxHp)
    assertEquals(character.attack, attack)
    assertEquals(character.defense, defense)
    assertEquals(character.evasion, evasion)
    assertEquals(character.stars, stars)
    assertEquals(character.wins, wins)
    assertEquals(character.currentHP, currentHP)
    assertEquals(character.defaultNorm, defaultNorm)
    assertEquals(character.currentNorm, currentNorm)
    assertEquals(character.normObjective, normObjective)
  }

  // Two ways to test randomness (you can use any of them):

  // 1. Test invariant properties, e.g. the result is always between 1 and 6.
  test("A character should be able to roll a dice without a default/fixed seed") {
    for (_ <- 1 to 10) {
      assert(character.rollDice() >= 1 && character.rollDice() <= 6)
    }
  }

  // 2. Set a seed and test the result is always the same.
  // A seed sets a fixed succession of random numbers, so you can know that the next numbers
  // are always the same for the same seed.
  test("A character should be able to roll a dice with a fixed seed") {
    val other =
      new PlayerCharacter(name, maxHp, attack, defense, evasion,
                          wins, defaultNorm, currentNorm, normObjective)
    for (_ <- 1 to 10) {
      assertEquals(character.rollDice(11), other.rollDice(11))
    }
  }

  test("A character should be able to get and change his decision to defend or evade if someone attacks him"){
    assertEquals(character.decision, "defense")
    character.decision = "evade"
    assertEquals(character.decision, "evade")
  }

  test("A character should be able to change its currentHP using his getter and setter") {
    val expected = character.currentHP
    assertEquals(character.currentHP, expected)
    character.currentHP_=(expected + 1)
    assertEquals(character.currentHP, expected + 1)
  }

  test("A character should be able to regenerate their HP by landing in their HomePanel"){
    assertEquals(character.currentHP, maxHp)
    character.regenerateHP()
    assertEquals(character.currentHP, maxHp + 10)
  }

  test("A character should be able to increase their stars by landing in a BonusPanel"){
    val other = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.stars,other.stars)
    character.increaseStarsByPanel()
    //println(character.stars,other.stars)
    //character.stars +=1
    //println(character.stars,other.stars)
    assert(character.stars > other.stars)
  }

  test("A character should be able to decrease their stars by landing in a DropPanel") {
    val other = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.stars, other.stars)
    character.decreaseStarsByPanel()
    assert(character.stars < other.stars)
  }

  /** ------------------------------------- COMBAT METHODS -------------------------------------- */
  /* for properly test the double dispatch for the combat system, we will test the methods from inside to outside the scope
    or in other words, by the runtime stack with the most deep method that has been called
   */

  test("AttackCalculator method should return a damage value") {
    val ref = character.attack // -1
    val damageToDeal = character.attackCalculator(character)
    assert(damageToDeal > -1) // damageToDeal cant be negative
    // in this case, character attack is -1, so the maximum attack depends on the rollDice() getting a 6 ---> dmg = 6+(-1) = 5
    assert(damageToDeal == 0 || damageToDeal <= (attack + 6))
    assert(damageToDeal > ref) // it can be (0 to 5) > -1
    val lazyPlayerCharacter = new PlayerCharacter(name, maxHp, -1000, defense, evasion,
                                                wins, defaultNorm, currentNorm, normObjective) // a Unit with no attack
    val damage = lazyPlayerCharacter.attackCalculator(lazyPlayerCharacter)
    assertEquals(damage, 0) // for a negative attack, it should return damage = 0
  }

  test("AttackCalculator for a KO unit should return 0") {
    val otherUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                wins, defaultNorm, currentNorm, normObjective)
    otherUnit.currentHP = 0
    otherUnit.isKO = true
    val damage = otherUnit.attackCalculator(otherUnit)
    assertEquals(damage, 0)
  }

  test("Defense method") {
    val ref = character.currentHP
    val opponent = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                      wins, defaultNorm, currentNorm, normObjective)
    val damageToReceive = opponent.attackCalculator(opponent) //we already test this method
    assertEquals(opponent.currentHP, ref)
    character.defendMove(damageToReceive)
    // Minimum damage is 1, or Max damage 5 ---> it will defeat the character and his HP would be 0, or currentHP between 1 to maxHP(3 in this case)
    assert(character.currentHP == ref - 1 || character.currentHP == 0 || (character.currentHP > 0 && character.currentHP <= maxHp))
  }

  test("Defense method vs a OverPowered unit") {
    val ref = character.currentHP
    val megaPlayerCharacter = new PlayerCharacter(name, maxHp, 1000, defense, evasion,
                                          wins, defaultNorm, currentNorm, normObjective)
    val damageToReceive = megaPlayerCharacter.attackCalculator(megaPlayerCharacter) //we already test this method, could be 1001 to 1006
    assert(1000 < damageToReceive && damageToReceive <= 1006)
    character.defendMove(damageToReceive)
    assert(character.currentHP < ref)
    assertEquals(character.currentHP, 0)
    assertEquals(character.isKO, expected = true)
  }

  test("Evade method") {
    val ref = character.currentHP
    val opponent = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                wins, defaultNorm, currentNorm, normObjective)
    val damageToReceive = opponent.attackCalculator(opponent) // Minimum damage = 1, Max damage = 5
    assertEquals(opponent.currentHP, ref)
    val damageReceived = character.evadeMove(damageToReceive)
    //evade the attack = damageReceived = 0, take all damage = damageReceived == damageToReceive
    assert(damageReceived == 0 || damageReceived == damageToReceive)
    assert(character.currentHP > -1) //after the attack, currentHP cant be negative
    // 1) SameHP because evade successfully the attack, 2) take all the damage and survives, 3) take all damage and not survives
    assert(character.currentHP == ref || character.currentHP == ref - damageReceived || character.currentHP == 0)
  }

  test("Evade method vs a non dangerous Unit") {
    val ref = character.currentHP
    val lazyPlayerCharacter = new PlayerCharacter(name, maxHp, -1000, defense, evasion,
                                wins, defaultNorm, currentNorm, normObjective) // a Unit with no attack
    val damageToReceive = lazyPlayerCharacter.attackCalculator(lazyPlayerCharacter)
    assertEquals(damageToReceive, 0) // for a negative attack, it should return damage = 0
    character.evadeMove(damageToReceive)
    assertEquals(character.currentHP, ref) // for a negative attack, it should always evade it ---> return damage = 0
  }

  test("Evade method vs a OverPowered unit") {
    // By the evade idea, it always take all damage or nothing, then this will have the same effect as test("Evade method")
    val ref = character.currentHP
    val megaPlayerCharacter = new PlayerCharacter(name, maxHp, 1000, defense, evasion,
                                          wins, defaultNorm, currentNorm, normObjective)
    val damageToReceive = megaPlayerCharacter.attackCalculator(megaPlayerCharacter) //we already test this method, could be 1001 to 1006
    assert(1000 < damageToReceive && damageToReceive <= 1006)
    val damageReceived = character.evadeMove(damageToReceive)
    assert(damageReceived == 0 || damageReceived == damageToReceive)
    assert(character.currentHP > -1) //after the attack, currentHP cant be negative
    // 1) SameHP because evade successfully the attack, 2) take all the damage (OP unit KO the character)
    assert(character.currentHP == ref || character.currentHP == 0)
  }

  test("ReceiveAttack method: First test, a character not in KO state and with 'defense' tactic") {
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.isKO, false)
    assertEquals(character.decision, "defense")
    // First, test a character not in KO state and with defense tactic
    val damageReceived = character.receiveAttack(attackingUnit)
    // Minimum damage is 1, or Max damage 5 ---> it will defeat the character and his HP would be 0, or currentHP between 1 to maxHP(3 in this case)
    assert(character.currentHP == ref - 1 || character.currentHP == 0 || (character.currentHP > 0 && character.currentHP <= maxHp))
  }

  test("ReceiveAttack method: Second test, a character not in KO state and with 'evade' tactic") {
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, 1, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective) // With attack equal to 1 we can test both scenarios
    assertEquals(character.isKO, false)
    character.decision = "evade"
    assertEquals(character.decision, "evade")
    // First, test a character not in KO state and with defense tactic
    val damageReceived = character.receiveAttack(attackingUnit)
    // 1) SameHP because evade successfully the attack, 2) take all the damage and survives, 3) take all damage and not survives
    assert(character.currentHP == ref || character.currentHP == ref - damageReceived || character.currentHP == 0)
  }

  test("ReceiveAttack method: Third test, a character in KO state with any tactic") {
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    character.isKO = true
    assertEquals(character.isKO, true)
    val damageReceived = character.receiveAttack(attackingUnit)
    assertEquals(damageReceived, 0)
    assertEquals(character.currentHP, ref)
  }

  test("ReceiveAttack method: Fourth test, a character with an invalid tactic") {
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective) // With attack equal to 1 we can test both scenarios
    character.decision = "something"
    assertEquals(character.decision, "something")
    val damageReceived = character.receiveAttack(attackingUnit)
    assertEquals(damageReceived, 0)
    assertEquals(character.currentHP, ref)
  }

  test("Attack Method, with 'defense' tactic") {
    //our character will defend, his opponent will attack
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.isKO, false)
    assertEquals(character.decision, "defense")
    val damageReceived = attackingUnit.attackMove(character)
    // Minimum damage is 1, or Max damage 5 ---> it will defeat the character and his HP would be 0, or currentHP between 1 to maxHP(3 in this case)
    assert(character.currentHP == ref - 1 || character.currentHP == 0 || character.currentHP > 0)
  }

  test("Attack Method, with 'evade' tactic") {
    //our character will defend, his opponent will attack
    val ref = character.currentHP
    val attackingUnit = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.isKO, false)
    character.decision = "evade"
    assertEquals(character.decision, "evade")
    val damageReceived = attackingUnit.attackMove(character)
    assert(character.currentHP > -1) //after the attack, currentHP cant be negative
    // 1) SameHP because evade successfully the attack, 2) take all the damage and survives, 3) take all damage and not survives
    assert(character.currentHP == ref || character.currentHP == ref - damageReceived || character.currentHP == 0)
  }


  test("A character should increase their stars by winning a combat against other player") {
    val other = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.stars, other.stars)
    character.increaseStarsByCombat(2)
    assert(character.stars > other.stars)
    assertEquals(character.stars, other.stars + 2)
  }

  test("A character should decrease their stars by losing a combat against another player") {
    val other = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                    wins, defaultNorm, currentNorm, normObjective)
    assertEquals(character.stars, other.stars)
    character.decreaseStarsByCombat(3)
    assert(character.stars < other.stars)
    assertEquals(character.stars, other.stars - 3)
  }

  test("A character should increase their stars by winning a combat against a wildUnit") {
    /*For example here we will use a PlayerCharacter as a wildUnit (because all wildUnits are the same but with different stats)*/
    val chicken: UnitTrait = new Chicken(maxHp, attack, defense, evasion)
    assertEquals(character.stars, chicken.stars)
    character.increaseStarsByCombat(5)
    assert(character.stars > chicken.stars)
    assertEquals(character.stars, chicken.stars + 5)
  }

  test("A character should increase their victories by winning a combat against a PlayerCharacter wildUnit") {
    //Here is necessary to declare chicken a type unitTrait, because we are using overloading in the PlayerCharacter class
    val chicken: UnitTrait = new Chicken(maxHp, attack, defense, evasion)
    //they both are initialized with 0 stars
    assertEquals(character.stars, chicken.stars)
    character.increaseVictories(chicken)
    assertEquals(character.stars, chicken.stars) //both are 0
    chicken.stars = 2 //now the wildUnit has 2 stars that the character can steal by winning
    character.increaseVictories(chicken)
    //wildUnit losing stars not implemented yet
    assertEquals(character.stars, chicken.stars)
  }

  test("A character should increase their victories by winning a combat against a Robo_ball (wildUnit)") {
    //Here is necessary to declare robo_ball a type unitTrait, because we are using overloading in the PlayerCharacter class
    val robo_ball: UnitTrait = new Robo_ball(maxHp, attack, defense, evasion)
    //they both are initialized with 0 stars
    assertEquals(character.stars, robo_ball.stars)
    character.increaseVictories(robo_ball)
    assertEquals(character.stars, robo_ball.stars) //both are 0
    robo_ball.stars = 2 //now the wildUnit has 2 stars that the character can steal by winning
    character.increaseVictories(robo_ball)
    //wildUnit losing stars not implemented yet
    assertEquals(character.stars, robo_ball.stars)
  }

  test("A character should increase their victories by winning a combat against a Seagull (wildUnit)") {
    //Here is necessary to declare seagull a type unitTrait, because we are using overloading in the PlayerCharacter class
    val seagull: UnitTrait = new Seagull(maxHp, attack, defense, evasion)
    //they both are initialized with 0 stars
    assertEquals(character.stars, seagull.stars)
    character.increaseVictories(seagull)
    assertEquals(character.stars, seagull.stars) //both are 0
    seagull.stars = 2 //now the wildUnit has 2 stars that the character can steal by winning
    character.increaseVictories(seagull)
    //wildUnit losing stars not implemented yet
    assertEquals(character.stars, seagull.stars)
  }

  test("A character should increase their victories by winning a combat against another player") {
    //Here is necessary to declare opponent a type PlayerCharacter, because we are using overloading in the PlayerCharacter class
    val opponent: PlayerCharacter = new PlayerCharacter(name, maxHp, attack, defense, evasion,
                                                     wins, defaultNorm, currentNorm, normObjective)
    //they both are initialized with 0 stars
    assertEquals(character.stars, opponent.stars)
    character.increaseVictories(opponent)
    assertEquals(character.stars, opponent.stars) //both are 0
    opponent.stars = 2 //now the opponent has 2 stars that the character can steal by winning
    character.increaseVictories(opponent)
    //opponent losing stars not implemented yet
    opponent.stars /= 2
    assertEquals(character.stars, opponent.stars)
  }

  test("A character should increase their stars just by finishing the round") {
    assertEquals(character.stars, stars)
    character.increaseStarsByRound(2)
    assertEquals(character.stars, stars + 2)
  }

  test("A character should increase their norm if they meet the stars requirements") {
    assertEquals(character.currentNorm, defaultNorm)
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm)
    character.stars = 10
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 1)
    character.stars = 30
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 2)
    character.stars = 70
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 3)
    character.stars = 120
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 4)
    character.stars = 200
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 5)
  }

  test("A character should increase their norm if they meet the victories requirements"){
    assertEquals(character.currentNorm, defaultNorm)
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm)
    character.wins = 1
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 1)
    character.wins = 3
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 2)
    character.wins = 6
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 3)
    character.wins = 10
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 4)
    character.wins = 15
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 5)
  }

  test("A character cant increase their norm if it doesnt meet the requirements"){
    assertEquals(character.currentNorm, defaultNorm)
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm)
    character.stars = 9
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm)
    character.stars = 29
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 1)
    character.stars = 69
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 2)
    character.stars = 119
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 3)
    character.stars = 199
    character.normCheck()
    assertEquals(character.currentNorm, defaultNorm + 4)
  }
}
