package models

import play.api.libs.json._
import org.mongodb.scala.bson.ObjectId
import utils.ObjectIdFormat._

case class Hero(_id: ObjectId, name: String, specialName: String, attack: Int, defence: Int, faction: String)

object Hero {
  implicit val heroFormat: OFormat[Hero] = Json.format[Hero]
}
