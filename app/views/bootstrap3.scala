package views.html

import views.html.helper._

/**
 * The class which auto-load the implicit constructor for Twitter Bootstrap 3 (see twitterbootstrap3.scala.html)
 */
object bootstrap3 {
        implicit val addressinputgroup = new FieldConstructor {
                def apply(elements: FieldElements) = twitterbootstrap3(elements)
        }
}