package antonkulaga.projects.pages

import scalacss.Defaults._

object MyStyles extends StyleSheet.Standalone {
  import dsl._

  ".CodeMirror" -(
    height.auto important
    // width.auto important
    )
  ".CodeMirror-scroll" -(
    overflow.visible,
    height.auto
    )//-(overflowX.auto,overflowY.hidden)
}