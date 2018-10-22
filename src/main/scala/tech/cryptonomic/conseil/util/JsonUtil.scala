package tech.cryptonomic.conseil.util

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * Jackson wrapper for JSON serialization and deserialization functions.
  */
object JsonUtil {

  /*
   * We're reducing visibility of the JsonString constuction (both class and object)
   * to allow instantiation only from JsonUtil's methods
   * The goal is to guarantee that only valid json will be contained within the value class wrapper
   */
  final case class JsonString private (json: String) extends AnyVal with Product with Serializable

  object JsonString {

    // Note: instead of making it private, it might make sense to verify the input
    // and return the [[JsonString]] within a wrapping effect (e.g. Option, Try, Either)
    private[JsonUtil] def apply(json: String): JsonString = new JsonString(json)

    /** A [[JsonString]] representing a json object with no attributes */
    lazy val emptyObject = JsonString("{}")

  }

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(value: Map[Symbol, Any]): JsonString = {
    toJson(value map { case (k,v) => k.name -> v})
  }

  def toJson[T](value: T): JsonString = {
    JsonString(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value))
  }

  def toMap[V](json:String)(implicit m: Manifest[V]): Map[String, V] = fromJson[Map[String,V]](json)

  def fromJson[T](json: String)(implicit m : Manifest[T]): T = {
    mapper.readValue[T](json.filterNot(Character.isISOControl))
  }
}