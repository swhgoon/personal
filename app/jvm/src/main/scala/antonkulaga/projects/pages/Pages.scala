package antonkulaga.projects.pages

import akka.http.extensions.pjax.PJax
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import org.denigma.controls.Twirl
import play.twirl.api.Html

class Pages extends Directives with PJax{

  def defaultPage: Option[Html] = {
    Some(html.dynamics())
  }

  def index: Route =  pathSingleSlash{ ctx=>
    ctx.materializer.executionContext
    ctx.complete {
      HttpResponse(  entity = HttpEntity(MediaTypes.`text/html`, html.index(defaultPage).body  ))
    }
  }

  def page(html: Html): Route = pjax[Twirl](html, loadPage){ h=> c=>
    val resp = HttpResponse(  entity = HttpEntity(MediaTypes.`text/html`, h.body  ))
    c.complete(resp)
  }

  val loadPage: Html => Html = h => html.index(Some(h))


  def test: Route = pathPrefix("test" ~ Slash) { ctx=>
      pjax[Twirl](Html(s"<h1>${ctx.unmatchedPath}</h1>"), loadPage){ h=> c=>
        val resp = HttpResponse( entity = HttpEntity(MediaTypes.`text/html`, h.body) )
        c.complete(resp)
      }(ctx)
    }

  def menu = pathPrefix("pages"~ Slash){ctx =>
    ctx.unmatchedPath.toString() match {
      case "notch-delta"=> page(html.dynamics())(ctx)
      case "pages/diff"=> page(html.statistics())(ctx)
      case other => ctx.complete("other")
    }
  }



  def routes: Route = index ~ test


}