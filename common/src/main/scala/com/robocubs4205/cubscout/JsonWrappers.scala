package com.robocubs4205.cubscout

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.http.Status._

sealed trait JsonWrapper[T]

final case class ResponseCtx(context: Option[String], id: Long)

sealed trait JsonResponseWrapper[T] extends JsonWrapper[T] {
  def context: Option[String]

  def responseId: Long
}

object JsonResponseWrapper {

  def apply[T](data: T)(implicit ctx: ResponseCtx, ew: EtagWriter[T]) = JsonSingleResponseWrapper(data, ctx.context, ctx.id)

  def apply[T](items: Iterable[T])(implicit ctx: ResponseCtx, ew: EtagWriter[T]) =
    JsonArrayResponseWrapper(items, ctx.context, ctx.id)

  implicit def JsonResponseWrapperWrites[T](implicit wt: Writes[T]): Writes[JsonResponseWrapper[T]] = {
    case e: JsonErrorResponseWrapper => JsonErrorResponseWrapper.jsonErrorResponseWrapperWrites.writes(e)
    case r: JsonSingleResponseWrapper[T] => JsonSingleResponseWrapper.jsonSingleResponseWrapperWrites[T].writes(r)
    case r: JsonArrayResponseWrapper[T] => JsonArrayResponseWrapper.jsonArrayResponseWrapperWrites[T].writes(r)
  }
}

sealed trait JsonRequestWrapper[T] extends JsonWrapper[T]

object JsonRequestWrapper {
  implicit def JsonRequestWrapperReads[T](implicit rt: Reads[T]): Reads[JsonRequestWrapper[T]] =
    JsonSingleRequestWrapper.jsonSingleRequestWrapperReads[T].map[JsonRequestWrapper[T]](x => x) orElse
      JsonArrayRequestWrapper.jsonArrayRequestWrapperReads[T].map[JsonRequestWrapper[T]](x => x)
}


sealed trait JsonSingleWrapper[T] extends JsonWrapper[T] {
  def data: T
}

sealed trait JsonArrayWrapper[T] extends JsonWrapper[T] {
  def items: Iterable[T]
}


final case class JsonSingleResponseWrapper[T](data: T, context: Option[String], responseId: Long)
                                             (implicit val ew: EtagWriter[T])
  extends JsonResponseWrapper[T] with JsonSingleWrapper[T]

object JsonSingleResponseWrapper {
  def jsonSingleResponseWrapperWrites[T](implicit wt: Writes[T]): Writes[JsonSingleResponseWrapper[T]] = (
    (JsPath \ "data").write[T] and
      (JsPath \ "context").writeNullable[String] and
      (JsPath \ "responseId").write[Long] and
      (JsPath \ "data" \ "etag").write[String]
    ) (v => (v.data, v.context, v.responseId, v.ew.etag(v.data)))
}

final case class JsonArrayResponseWrapper[T](items: Iterable[T], context: Option[String],
                                             responseId: Long)(implicit val ew: EtagWriter[T])
  extends JsonResponseWrapper[T] with JsonArrayWrapper[T]

object JsonArrayResponseWrapper {

  private case class JsonDataWithEtagWrapper[T](t: T, etag: String)

  implicit private def JsonDataWithEtagWrapperWrites[T](implicit wt: Writes[T]): Writes[JsonDataWithEtagWrapper[T]] = (
    JsPath.write[T] and
      (JsPath \ "etag").write[String]
    ) (unlift(JsonDataWithEtagWrapper.unapply[T]))

  def jsonArrayResponseWrapperWrites[T](implicit wt: Writes[T]): Writes[JsonArrayResponseWrapper[T]] = (
    (JsPath \ "data" \ "items").write[Iterable[JsonDataWithEtagWrapper[T]]] and
      (JsPath \ "context").writeNullable[String] and
      (JsPath \ "responseId").write[Long]
    ) (v => (v.items.map(i => JsonDataWithEtagWrapper(i, v.ew.etag(i))), v.context, v.responseId))
}

final case class JsonSingleRequestWrapper[T](data: T)
  extends JsonRequestWrapper[T] with JsonSingleWrapper[T]

object JsonSingleRequestWrapper {
  implicit def jsonSingleRequestWrapperReads[T](implicit rt: Reads[T]): Reads[JsonSingleRequestWrapper[T]] =
    (JsPath \ "data").read[T].map(JsonSingleRequestWrapper.apply[T])
}

final case class JsonArrayRequestWrapper[T](items: Iterable[T])
  extends JsonRequestWrapper[T] with JsonArrayWrapper[T]

object JsonArrayRequestWrapper {
  implicit def jsonArrayRequestWrapperReads[T](implicit rt: Reads[T]): Reads[JsonArrayRequestWrapper[T]] =
    (JsPath \ "data" \ "items").read[Iterable[T]].map(JsonArrayRequestWrapper.apply[T])
}


final case class JsonErrorResponseWrapper(errors: Iterable[JsonErrorWrapper], code: Long, message: String,
                                          context: Option[String], responseId: Long)
  extends JsonResponseWrapper[Nothing]


trait JsonErrorWrapper {
  def json: JsValue
}

object JsonErrorResponseWrapper {
  def apply(errors: Iterable[JsonErrorWrapper], code: Long, message: String)
           (implicit ctx: ResponseCtx): JsonErrorResponseWrapper =
    JsonErrorResponseWrapper(errors, code, message, ctx.context, ctx.id)

  def apply(error: JsonErrorWrapper, code: Long, message: String)
           (implicit ctx: ResponseCtx): JsonErrorResponseWrapper = apply(Seq(error), code, message)

  def apply(code: Long, message: String)(implicit ctx: ResponseCtx): JsonErrorResponseWrapper =
    apply(Seq(), code, message)

  def apply(errors: JsError)(implicit ctx: ResponseCtx): JsonErrorResponseWrapper = apply(
    errors.errors.flatMap(e => e._2.map(f => (e._1, f))).map(e => ParseErrorWrapper(e._1, e._2)),
    UNPROCESSABLE_ENTITY, "There were parse errors when processing the request")

  def apply(code:Long,exception:Exception)(implicit ctx: ResponseCtx):JsonErrorResponseWrapper =
    apply(code,exception.getMessage)

  final case class ParseErrorWrapper(path: JsPath, error: JsonValidationError) extends JsonErrorWrapper {
    override def json: JsValue = w.writes(this)

    val w: Writes[ParseErrorWrapper] = (
      (JsPath \ "path").write[String] and
        (JsPath \ "message").write[String] and
        (JsPath \ "reason").write[String]
      ) (e => (e.path.toJsonString, e.error.message, "parse error"))
  }

  implicit def jsonErrorResponseWrapperWrites[T]: Writes[JsonErrorResponseWrapper] = (
    (JsPath \ "error" \ "errors").writeNullable[Iterable[JsValue]] and
      (JsPath \ "error" \ "code").write[Long] and
      (JsPath \ "error" \ "message").write[String] and
      (JsPath \ "context").writeNullable[String] and
      (JsPath \ "responseId").write[Long]
    ) { e =>
    val es = e.errors.map(_.json).toSeq
    (if (es.isEmpty) None else Some(es), e.code, e.message, e.context, e.responseId)
  }

}




