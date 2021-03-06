object Versions extends WebJarsVersions with ScalaJSVersions with SharedVersions
{
	val scala = "2.11.7"

	val akkaHttp = "1.0"

	val bcrypt = "2.4"

	val ammonite = "0.5.1"

	val apacheCodec = "1.10"

	val akkaHttpExtensions = "0.0.6"

	val bindingControls = "0.0.9-M1"

	val personal = "0.0.2"

	val retry = "0.2.1"

	val threejsFacade = "0.0.71-0.1.5"

	val macroParadise = "2.1.0-M5"

}

trait ScalaJSVersions {

	val jqueryFacade = "0.10"

	val semanticUIFacade = "0.0.1"

	val dom = "0.8.2"

	val codemirrorFacade = "5.5-0.5"

	val binding = "0.8.1-M1"

}

//versions for libs that are shared between client and server
trait SharedVersions
{
	val scalaRx = "0.2.8"

	val scalaTags = "0.5.2"

	val scalaCSS = "0.3.1"

	val productCollections = "1.4.2"

	val scalaTest = "3.0.0-SNAP13"

	val fastparse = "0.3.3"

}


trait WebJarsVersions{

	val jquery =  "2.1.4"

	val semanticUI = "2.1.6"

	val codemirror = "5.8"

	val threeJS = "r71"

	val webcomponents = "0.7.7"

}

