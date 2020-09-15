import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import untitled.goose.framework.dsl.GooseDSL
import untitled.goose.framework.dsl.board.words.DispositionType.Spiral
import untitled.goose.framework.model.entities.runtime.GameStateExtensions._
import untitled.goose.framework.model.events.consumable._
import untitled.goose.framework.model.events.persistent.{GainTurnEvent, LoseTurnEvent, TileActivatedEvent}
import untitled.goose.framework.model.rules.ruleset.PlayerOrderingType.Fixed


object GooseGame extends GooseDSL with CustomValues {

  Rules of "Goose Game"
  2 to 4 players

  Players have order(Fixed)

  The game board has size(63)
  the game board has disposition(Spiral)

  The tiles (1 to 63) have group("path")
  All tiles "path" have color(Color.web("6f9d79"))

  the tile 6 has name(theBridge)
  the tile 19 has name(theInn)
  the tile 31 has name(theWell)
  the tile 42 has name(theLabyrinth)
  the tile 52 has name(thePrison)
  the tile 58 has name(theDeath)

  The tile theBridge has background("bridge.png")
  the tile theWell has background("well.png")
  the tile theInn has background("inn.png")
  the tile theLabyrinth has background("labyrinth.png")
  the tile thePrison has(
    color(DarkGray),
    background("prison.png")
  )
  the tile theDeath has background("death.png")

  The tile 63 has(
    name(theEnd),
    background("victory.png"),
    color(Gold)
  )

  The tiles(5, 9, 14, 18, 23, 27, 32, 36, 41, 45, 50, 54, 59) have group(gooseGroup)
  All tiles gooseGroup have background("goose_dice.png")

  Players start on tile 1

  Create movementDice "six-faced" having totalSides(6)

  Players loseTurn priority is 10

  Each turn players are (always allowed to roll 2 movementDice "six-faced" as "roll a dice" priority 5)


  //To win you must reach tile 63 exactly.
  // If your dice roll is more than you need then you move in to tile 63 bounce back out again,
  // each spot on the dice is still one step in this move.
  // If you land on any of the special tiles while you are doing this then you must follow the normal instructions.
  always when numberOf(events[TileEnteredEvent] matching (e => tileIs(theEnd)(e.tile))) is (_ > 0) resolve (
    forEach trigger ((e, s) => InvertMovementEvent(e.player, s.currentTurn, s.currentCycle))
    ) andThen consume

  //When you land on square 63 exactly you are the winner!
  always when numberOf(events[StopOnTileEvent] matching (e => tileIs(theEnd)(e.tile))) is (_ > 0) resolve(
    forEach trigger ((e, s) => VictoryEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => TileActivatedEvent(e.tile, s.currentTurn, s.currentCycle))
  ) andThen consume


  //If you throw a 3 on your first turn you can move straight to tile 26.
  When(isPlayerFirstTurn) and numberOf(events[MovementDiceRollEvent] matching (_.diceResult.contains(3))) is (_ > 0) resolve(
    displayMessage("Special first throw!", "You rolled a 3 on your first turn, go to tile 26"),
    trigger((e, s) => TeleportEvent(s.getTile(26).get, e.player, s.currentTurn, s.currentCycle))
  ) andThen consume && save

  //If your piece lands on a Goose tile you can throw your dice again.
  always when numberOf(events[StopOnTileEvent] matching (_.tile.definition.belongsTo(gooseGroup))) is (_ > 0) resolve(
    displayMessage("Landed on a Goose", "You can roll the dice again"),
    forEach trigger ((e, s) => GainTurnEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => TileActivatedEvent(e.tile, s.currentTurn, s.currentCycle))
  ) andThen consume

  //If you land on the Bridge, miss a turn while you pay the toll.
  //If you land on the Inn miss a turn while you stop for some tasty dinner.
  always when numberOf(events[StopOnTileEvent] matching (e => tileIs(theBridge)(e.tile) || tileIs(theInn)(e.tile))
  ) is (_ > 0) resolve(
    forEach displayMessage ((e, _) => ("Landed on " + e.tile.definition.name.get, if (tileIs(theBridge)(e.tile))
      "Wait a turn while you pay the toll" else "Wait a turn while you stop for some tasty dinner")),
    forEach trigger ((e, s) => LoseTurnEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => TileActivatedEvent(e.tile, s.currentTurn, s.currentCycle))
  ) andThen consume

  //If you you land on the Well make a wish and miss three turns.
  //If you land on the Prison you will have to miss three turns while you are behind bars.
  always when numberOf(events[StopOnTileEvent] matching (e => tileIs(theWell)(e.tile) || tileIs(thePrison)(e.tile))
  ) is (_ > 0) resolve(
    forEach displayMessage ((e, _) => ("Landed on " + e.tile.definition.name.get, if (tileIs(theWell)(e.tile))
      "Make a wish and miss three turns" else "Miss three turns while you are behind bars")),
    forEach trigger ((e, s) => LoseTurnEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => LoseTurnEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => LoseTurnEvent(e.player, s.currentTurn, s.currentCycle)),
    forEach trigger ((e, s) => TileActivatedEvent(e.tile, s.currentTurn, s.currentCycle))
  ) andThen consume

  //If another player passes you before your three turns are up you can start moving again on your next go.
  always when numberOf(events[PlayerPassedEvent] matching (e => tileIs(theWell)(e.tile) || tileIs(thePrison)(e.tile))
  ) is (_ > 0) resolve (
    forEach updateState ((e, _) => _ => e.player.history = e.player.history.excludeEventType[LoseTurnEvent]())
    ) andThen consume

  //If you land on the Labyrinth, square 42, you will get lost in the maze and have to move back to square 37
  //If you land on Death, square 58, you have to go back to square 1 and start all over again!
  always when numberOf(events[StopOnTileEvent] matching (e => tileIs(theLabyrinth)(e.tile) || tileIs(theDeath)(e.tile))
  ) is (_ > 0) resolve(
    forEach displayMessage ((e, _) => ("Landed on " + e.tile.definition.name.get, if (tileIs(theLabyrinth)(e.tile))
      "You enter the labyrinth but you get lost... You exit on tile 37" else "You died! Go back to the beginning and try again")),
    forEach trigger ((e, s) => {
      val tile = if (tileIs(theLabyrinth)(e.tile)) s.getTile(37).get else s.getTile(1).get
      TeleportEvent(tile, e.player, s.currentTurn, s.currentCycle)
    }),
    forEach trigger ((e, s) => TileActivatedEvent(e.tile, s.currentTurn, s.currentCycle))
  ) andThen consume

  Include these system behaviours(
    MovementWithDice,
    MultipleStep,
    Teleport,
    SkipTurnManager,
    VictoryManager
  )
}

