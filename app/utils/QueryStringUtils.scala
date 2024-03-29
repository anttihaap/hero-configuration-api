package utils

import scala.util.{Failure, Success, Try}

object QueryStringUtils {
  def parseOptionalIntQueryParam(queryString: Map[String, Seq[String]], paramName: String): Either[String, Option[Int]] = {
    queryString.get(paramName) match {
      case Some(values) =>
        values.headOption match {
          case Some(paramValue) =>
            Try(paramValue.toInt) match {
              case Success(parsedInt) => Right(Some(parsedInt))
              case Failure(_)         => Left(s"Invalid value for parameter '$paramName': '$paramValue' is not a valid integer.")
            }
          case None => Right(None)
        }
      case None => Right(None)
    }
  }

  def parseOptionalStringQueryParam(queryString: Map[String, Seq[String]], paramName: String): Option[String] = {
    queryString.get(paramName).flatMap(_.headOption)
  }
}
