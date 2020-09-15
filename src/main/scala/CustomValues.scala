import untitled.goose.framework.model.entities.runtime.GameStateExtensions._
import untitled.goose.framework.model.entities.runtime.{GameState, Tile}
import untitled.goose.framework.model.events.persistent.TurnEndedEvent

trait CustomValues {
  val gooseGroup = "GooseTile"
  val theBridge = "the Bridge"
  val theWell = "the Well"
  val theInn = "the Inn"
  val theLabyrinth = "the Labyrinth"
  val thePrison = "the Prison"
  val theDeath = "the Death"
  val theEnd = "the End"


  def tileIs(name: String) : Tile => Boolean = _.definition.name.contains(name)

  //TODO why not autoimport of extensions?
  def isPlayerFirstTurn : GameState => Boolean = _.currentPlayer.history.only[TurnEndedEvent].isEmpty
}
