
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "community-engagement-platform",
    libraryDependencies ++= {
      // Determine OS version of JavaFX binaries
      val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")   => "linux"
        case n if n.startsWith("Mac")     => "mac"
        case n if n.startsWith("Windows") => "win"
        case _                            => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "21.0.4" classifier osName)
    },
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",
      // Time handling library
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      // JSON library for potential data export/import
      "org.json4s" %% "json4s-native" % "4.0.6",
      // SQLite database driver
      "org.xerial" % "sqlite-jdbc" % "3.43.2.2"
    )
  )
