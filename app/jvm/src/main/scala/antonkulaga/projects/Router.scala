package antonkulaga.projects

import akka.actor.ActorSystem
import akka.http.extensions.security.LoginInfo
import akka.http.extensions.stubs.{Registration, _}
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.stage.{Context, PushStage, SyncDirective, TerminationDirective}
import antonkulaga.projects.pages.{Head, Pages}

class Router(implicit fm: Materializer, system: ActorSystem) extends Directives {
  val sessionController: SessionController = new InMemorySessionController
  val loginController: InMemoryLoginController = new InMemoryLoginController()
  loginController.addUser(LoginInfo("admin","test2test","test@email"))

  def routes: Route = new Head().routes ~
    new Registration(
      loginController.loginByName,
      loginController.loginByEmail,
      loginController.register,
      sessionController.userByToken,
      sessionController.makeToken
    )
      .routes ~
    new Pages().routes//~new WebSockets(SocketTransport(deviceActor).webSocketFlow).routes


  def testBackFlow(channel: String, sender: String): Flow[String, Strict, Unit] =  Flow[String].collect{
    case message=> TextMessage.Strict(s"$sender: $message") // ... pack outgoing messages into WS text messages ...
  }

  def websocketChatFlow(channel: String, username: String): Flow[Message, Message, Unit] =
    Flow[Message]
      .collect {
      case TextMessage.Strict(msg) ⇒
        println(s"WE GOT $msg !")
        msg
    }.via(testBackFlow(channel, username)).via(reportErrorsFlow) // ... then log any processing errors on stdin

  def reportErrorsFlow[T]: Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          println(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })

}
