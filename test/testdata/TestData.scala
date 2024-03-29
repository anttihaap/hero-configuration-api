package testdata

import models.Hero
import org.mongodb.scala.bson.ObjectId

import scala.util.Random

object TestData {
  val random = new Random()
  private def randomString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val randomChars = (1 to length).map(_ => chars(random.nextInt(chars.length)))
    randomChars.mkString
  }

  private def randomInt(min: Int, max: Int): Int = {
    min + random.nextInt((max - min) + 1)
  }

  def createDummyHero(attack: Int = randomInt(0, 10)): Hero = {
    Hero(
      _id = new ObjectId(),
      name = randomString(10),
      specialName = randomString(10),
      attack = attack,
      defence = randomInt(0, 10),
      faction = randomString(10)
    )
  }
}
