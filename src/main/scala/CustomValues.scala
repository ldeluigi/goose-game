import untitled.goose.framework.model.entities.definitions.TileDefinition
import untitled.goose.framework.model.entities.runtime.GameState
import untitled.goose.framework.model.entities.runtime.functional.HistoryExtensions.PimpedHistory
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


  def tileIs(name: String) : TileDefinition => Boolean = _.name.contains(name)

  //TODO why not autoimport of extensions?
  def isPlayerFirstTurn : GameState => Boolean = s => s.players(s.currentPlayer).history.only[TurnEndedEvent].isEmpty
}
