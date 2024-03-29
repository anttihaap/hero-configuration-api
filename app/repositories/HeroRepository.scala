package repositories

import com.mongodb.client.model.Filters
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import models.Hero
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class HeroFilters(faction: Option[String], name: Option[String], minAttack: Option[Int], maxAttack: Option[Int])

@Singleton
class HeroRepository @Inject() (mongoDb: MongoDatabase)(implicit ec: ExecutionContext) {
  val collection: MongoCollection[Hero] = mongoDb.getCollection[Hero]("heroes")

  def createHero(hero: Hero): Future[Unit] = collection.insertOne(hero).toFuture().map(_ => ())

  def deleteHero(id: ObjectId): Future[Boolean] = collection.deleteOne(equal("_id", id)).toFuture().map {
    _.getDeletedCount > 0
  }

  def listAllHeroes(heroFilters: HeroFilters): Future[Seq[Hero]] = {
    val filters = Seq(
      heroFilters.name.map(name => equal("name", name)),
      heroFilters.faction.map(faction => equal("faction", faction)),
      heroFilters.minAttack.map(min => gte("attack", min)),
      heroFilters.maxAttack.map(max => lte("attack", max))
    ).flatten match {
      case Nil     => Filters.empty()
      case filters => Filters.and(filters: _*)
    }
    collection.find(filters).toFuture()
  }

  def updateHero(id: ObjectId, hero: Hero): Future[Boolean] = {
    val updates =
      Seq(
        set("name", hero.name),
        set("specialName", hero.specialName),
        set("attack", hero.attack),
        set("defence", hero.defence),
        set("faction", hero.faction)
      )
    collection.updateOne(equal("_id", id), updates).toFuture().map {
      _.getModifiedCount > 0
    }
  }
}
