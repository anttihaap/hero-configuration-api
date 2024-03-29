package controllers

import models.Hero
import org.mockito.ArgumentMatchers.{any, eq => eqMatcher}
import org.mockito.Mockito.{verify, when}
import org.mongodb.scala.bson.ObjectId
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{JsObject, Json}
import play.api.test._
import play.api.test.Helpers._
import repositories.{HeroFilters, HeroRepository}
import testdata.TestData.createDummyHero

import scala.concurrent.{ExecutionContext, Future}

class HeroControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val mockHeroRepository: HeroRepository = mock[HeroRepository]
  val controller = new HeroController(mockHeroRepository, stubControllerComponents())

  val dummyObjectId = new ObjectId()
  val dummyHero: Hero = createDummyHero()
  val dummyHeroAsJsObject: JsObject = Json.toJson[Hero](dummyHero).as[JsObject]
  val dummyInvalidHeroJsObject: JsObject = dummyHeroAsJsObject ++ Json.obj("name" -> 1)

  "createHero" should {
    "return CREATED with valid Hero" in {
      when(mockHeroRepository.createHero(any[Hero])).thenReturn(Future.successful(()))

      val result = controller.createHero().apply(FakeRequest(GET, "/").withBody(dummyHeroAsJsObject))

      status(result) mustBe CREATED
    }

    "return BAD_REQUEST with invalid Hero" in {
      val result = controller.createHero().apply(FakeRequest(GET, "/").withBody(dummyInvalidHeroJsObject))

      status(result) mustBe BAD_REQUEST
    }
  }

  "deleteHero" should {
    "return OK when hero exists" in {
      when(mockHeroRepository.deleteHero(any[ObjectId])).thenReturn(Future.successful(true))

      val result = controller.deleteHero(dummyObjectId.toHexString).apply(FakeRequest(GET, "/"))

      status(result) mustBe OK
    }

    "return NOT_FOUND when hero does not exist invalid Hero" in {
      when(mockHeroRepository.deleteHero(any[ObjectId])).thenReturn(Future.successful(false))

      val result = controller.deleteHero(dummyObjectId.toHexString).apply(FakeRequest(GET, "/"))

      status(result) mustBe NOT_FOUND
    }

    "return BAD_REQUEST when id is not object id" in {
      val result = controller.deleteHero("invalid").apply(FakeRequest(GET, "/"))

      status(result) mustBe BAD_REQUEST
    }
  }

  "listHero" should {
    "return list of heroes" in {
      when(mockHeroRepository.listAllHeroes(any[HeroFilters])).thenReturn(Future.successful(Seq(dummyHero, dummyHero)))

      val result = controller.listAllHeroes().apply(FakeRequest(GET, "/"))

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.arr(dummyHeroAsJsObject, dummyHeroAsJsObject)
    }

    "should handle query parameter filters" in {
      val filterQueryString = "faction=faction&name=name&minAttack=1&maxAttack=2"
      controller.listAllHeroes().apply(FakeRequest(GET, "/?" + filterQueryString))

      val expectedFilters: HeroFilters = HeroFilters(
        faction = Some("faction"),
        name = Some("name"),
        minAttack = Some(1),
        maxAttack = Some(2)
      )
      verify(mockHeroRepository).listAllHeroes(eqMatcher(expectedFilters))
    }

    "return BAD_REQUEST when maxAttack query parameter is invalid" in {
      val result = controller.listAllHeroes().apply(FakeRequest(GET, "/?maxAttack=invalid"))

      status(result) mustBe BAD_REQUEST
    }

    "return BAD_REQUEST when minAttack query parameter is invalid" in {
      val result = controller.listAllHeroes().apply(FakeRequest(GET, "/?minAttack=invalid"))

      status(result) mustBe BAD_REQUEST
    }
  }

  "updateHero" should {
    "return OK when hero is updated" in {
      when(mockHeroRepository.updateHero(any[ObjectId], any[Hero])).thenReturn(Future.successful(true))

      val result = controller
        .updateHero(dummyObjectId.toHexString)
        .apply(
          FakeRequest(GET, "/")
            .withBody(dummyHeroAsJsObject)
        )

      status(result) mustBe OK
    }

    "return BAD_REQUEST when hero is invalid" in {
      val result = controller
        .updateHero(dummyObjectId.toHexString)
        .apply(
          FakeRequest(GET, "/")
            .withBody(dummyInvalidHeroJsObject)
        )

      status(result) mustBe BAD_REQUEST
    }

    "return BAD_REQUEST when objectId is invalid" in {
      val result = controller.updateHero("invalid").apply(FakeRequest(GET, "/").withBody(dummyHeroAsJsObject))

      status(result) mustBe BAD_REQUEST
    }

    "return NOT_FOUND when hero does not exist" in {
      when(mockHeroRepository.updateHero(any[ObjectId], any[Hero])).thenReturn(Future.successful(false))

      val result = controller
        .updateHero(new ObjectId().toHexString)
        .apply(
          FakeRequest(GET, "/")
            .withBody(dummyHeroAsJsObject)
        )

      status(result) mustBe NOT_FOUND
    }
  }
}
