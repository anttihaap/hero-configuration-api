package repositories

import com.google.inject.{Guice, Injector}
import models.Hero
import modules.MongoModule
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.mongodb.scala.model.Filters
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.{Configuration, Environment, Mode}
import testdata.TestData.createDummyHero
import org.scalatest.time.SpanSugar._

import scala.concurrent.ExecutionContext

class HeroRepositorySpec extends PlaySpec with ScalaFutures with BeforeAndAfterEach {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 10.seconds)

  val environment: Environment = Environment.simple(mode = Mode.Test)
  val configuration: Configuration = Configuration.load(environment)
  val injector: Injector = Guice.createInjector(new MongoModule(environment, configuration))
  val mongoDb: MongoDatabase = injector.getInstance(classOf[MongoDatabase])
  val collection: MongoCollection[Hero] = mongoDb.getCollection[Hero]("heroes")

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val heroRepository: HeroRepository = new HeroRepository(mongoDb)(ec)

  val dummyHero: Hero = createDummyHero()

  override def beforeEach(): Unit = {
    collection.deleteMany(Filters.empty()).toFuture()
  }

  override def afterEach(): Unit = {
    collection.deleteMany(Filters.empty()).toFuture()
  }

  "HeroRepository" should {
    "create a hero" in {
      heroRepository.createHero(dummyHero).futureValue

      val heroesAfterCreate = collection.find().toFuture()

      whenReady(heroesAfterCreate) { heroes =>
        heroes should have length 1
        heroes.head should equal(dummyHero)
      }
    }

    "delete a hero" in {
      heroRepository.createHero(dummyHero).futureValue

      heroRepository.deleteHero(dummyHero._id).futureValue

      val heroesAfterDelete = collection.find().toFuture()
      whenReady(heroesAfterDelete) { heroes =>
        heroes should have length 0
      }
    }

    "list all heroes" in {
      val dummyHero1 = createDummyHero()
      val dummyHero2 = createDummyHero()
      heroRepository.createHero(dummyHero1).futureValue
      heroRepository.createHero(dummyHero2).futureValue

      val heroFilters = HeroFilters(None, None, None, None)
      val listAllHeroes = heroRepository.listAllHeroes(heroFilters)

      whenReady(listAllHeroes) { heroes =>
        heroes should have length 2
        heroes.contains(dummyHero1) mustBe true
        heroes.contains(dummyHero2) mustBe true
      }
    }

    "list all filters heroes by faction" in {
      val dummyHero1 = createDummyHero()
      val dummyHero2 = createDummyHero()
      heroRepository.createHero(dummyHero1).futureValue
      heroRepository.createHero(dummyHero2).futureValue

      val heroFilters = HeroFilters(Some(dummyHero1.faction), None, None, None)
      val listAllHeroes = heroRepository.listAllHeroes(heroFilters)

      whenReady(listAllHeroes) { heroes =>
        heroes should have length 1
        heroes should contain(dummyHero1)
      }
    }

    "list all filters heroes by attack" in {
      val dummyHero1 = createDummyHero(attack = 2)
      val dummyHero2 = createDummyHero(attack = 5)
      heroRepository.createHero(dummyHero1).futureValue
      heroRepository.createHero(dummyHero2).futureValue

      val heroFilters = HeroFilters(None, None, Some(1), Some(4))
      val listAllHeroes = heroRepository.listAllHeroes(heroFilters)

      whenReady(listAllHeroes) { heroes =>
        heroes should have length 1
        heroes should contain(dummyHero1)
      }
    }

    "update a hero" in {
      val dummyHero = createDummyHero()
      heroRepository.createHero(dummyHero).futureValue
      val dummyUpdatedHero = createDummyHero().copy(_id = dummyHero._id)

      heroRepository.updateHero(dummyHero._id, dummyUpdatedHero).futureValue

      val heroesAfterUpdate = collection.find().toFuture()
      whenReady(heroesAfterUpdate) { heroes =>
        heroes should have length 1
        heroes.head should equal(dummyUpdatedHero)
      }
    }
  }
}
