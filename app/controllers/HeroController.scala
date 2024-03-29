package controllers

import models.Hero
import org.mongodb.scala.bson.ObjectId

import javax.inject._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import repositories.{HeroFilters, HeroRepository}
import utils.QueryStringUtils.{parseOptionalIntQueryParam, parseOptionalStringQueryParam}
import utils.ObjectIdFormat._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class HeroController @Inject() (
    heroRepository: HeroRepository,
    val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  def createHero: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val heroJsObject = request.body.as[JsObject] + ("_id" -> Json.toJson(new ObjectId()))
    heroJsObject
      .validate[Hero]
      .fold(
        error => Future.successful(BadRequest(s"Invalid hero data: ${error}")),
        hero => processCreateHero(hero.copy(_id = new ObjectId()))
      )
  }

  private def processCreateHero(hero: Hero): Future[Result] = {
    heroRepository
      .createHero(hero)
      .map { _ => Created("Hero created") }
      .recover { case ex: Exception =>
        InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  def deleteHero(id: String): Action[AnyContent] = Action.async { implicit request =>
    Try(new ObjectId(id)) match {
      case Success(objectId) => processDeleteHero(objectId)
      case Failure(_) =>
        Future.successful(BadRequest("Invalid ObjectId format"))
    }
  }

  private def processDeleteHero(objectId: ObjectId): Future[Result] = {
    heroRepository
      .deleteHero(objectId)
      .map {
        case true  => Ok("Hero deleted")
        case false => NotFound("Hero not found")
      }
      .recover { case ex: Exception =>
        InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  def listAllHeroes: Action[AnyContent] = Action.async { implicit request =>
    createHeroFilters(request.queryString) match {
      case Left(errorMsg) => Future.successful(BadRequest(errorMsg))
      case Right(heroFilters) =>
        heroRepository
          .listAllHeroes(heroFilters)
          .map(heroes => Ok(Json.toJson(heroes)))
    }
  }

  private def createHeroFilters(
      queryStrings: Map[String, Seq[String]]
  ): Either[String, HeroFilters] = {
    for {
      maxAttack <- parseOptionalIntQueryParam(queryStrings, "maxAttack")
      minAttack <- parseOptionalIntQueryParam(queryStrings, "minAttack")
    } yield HeroFilters(
      faction = parseOptionalStringQueryParam(queryStrings, "faction"),
      name = parseOptionalStringQueryParam(queryStrings, "name"),
      minAttack = minAttack,
      maxAttack = maxAttack
    )
  }

  def updateHero(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    Try(new ObjectId(id)) match {
      case Success(objectId) =>
        val heroJsObject = request.body.as[JsObject] + ("_id" -> Json.toJson(objectId))
        heroJsObject
          .validate[Hero]
          .fold(
            error => Future.successful(BadRequest(s"Invalid hero data: ${error}")),
            hero => processUpdateHero(objectId, hero)
          )
      case Failure(_) =>
        Future.successful(BadRequest("Invalid ObjectId format"))
    }
  }

  private def processUpdateHero(objectId: ObjectId, hero: Hero): Future[Result] = {
    heroRepository
      .updateHero(objectId, hero)
      .map {
        case true  => Ok("Hero updated")
        case false => NotFound("Hero not found")
      }
      .recover { case ex: Exception =>
        InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }
}
