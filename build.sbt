name := "goose-game"

version := "0.1"

scalaVersion := "2.12.12"


libraryDependencies += "io.vertx" %% "vertx-lang-scala" % "3.9.1"
// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"
libraryDependencies += "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1"


// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "14.0.1" classifier osName
)