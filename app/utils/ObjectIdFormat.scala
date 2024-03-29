package utils

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

object ObjectIdFormat {
  implicit val objectIdFormat: Format[ObjectId] = new Format[ObjectId] {
    override def writes(o: ObjectId): JsValue = JsString(o.toHexString)

    override def reads(json: JsValue): JsResult[ObjectId] = json match {
      case JsString(s) => JsSuccess(new ObjectId(s))
      case _           => JsError("Invalid ObjectId format")
    }
  }
}
