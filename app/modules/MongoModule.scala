package modules

import com.google.inject.{AbstractModule, Provides}
import models.Hero
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import play.api.{Configuration, Environment, Mode}

class MongoModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  @Provides
  def provideMongoDatabase(): MongoDatabase = {
    val mongoClient = MongoClient(configuration.get[String]("mongodb.uri"))
    val dbName: String = environment.mode match {
      case Mode.Test => "test_db"
      case _         => "db"
    }
    val codecRegistry = fromRegistries(
      fromProviders(classOf[Hero]),
      DEFAULT_CODEC_REGISTRY
    )
    mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry)
  }
}
