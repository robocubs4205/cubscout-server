package com.robocubs4205.cubscout.model.scorecard

/**
  * Created by trevor on 7/29/17.
  */
case class Scorecard(id: Long, gameId: Long)

sealed trait ScorecardSection {
  def id: Long

  def scorecardId: Long

  def index: Long

  def discriminator: String
}

sealed trait FieldSection extends ScorecardSection{
  def `type`: FieldType
  final override val discriminator = "field"
  def isOptional:Boolean
  def label:String
}

final case class RequiredFieldSection(id: Long,
                                      scorecardId: Long,
                                      index: Long,
                                      `type`: FieldType,
                                      label:String)
  extends FieldSection {
  override val isOptional = false
}

final case class OptionalFieldSection(id: Long,
                                      scorecardId: Long,
                                      index: Long,
                                      `type`: FieldType,
                                      label:String,
                                      nullWhen:NullWhen,
                                      checkboxMessage:String)
  extends FieldSection{
  override val isOptional = true
}

final case class ParagraphSection(id:Long,scorecardId:Long,index:Long,text:String) extends ScorecardSection{
  override def discriminator: String = "paragraph"
}

final case class TitleSection(id:Long,scorecardId:Long,index:Long,text:String) extends ScorecardSection{
  override def discriminator: String = "title"
}

sealed trait FieldType

object FieldType{
  case object Count extends FieldType

  case object Rating extends FieldType

  case object Boolean extends FieldType
}


sealed trait NullWhen
object NullWhen{
  case object Checked extends NullWhen

  case object UnChecked extends NullWhen
}

