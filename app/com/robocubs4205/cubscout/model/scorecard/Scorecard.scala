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
  def fieldType: FieldType
  final override val discriminator = "field"
  def isOptional:Boolean
}

final case class RequiredFieldSection(id: Long, scorecardId: Long, index: Long, fieldType: FieldType)
  extends FieldSection {
  override val isOptional = false
}

final case class OptionalFieldSection(id: Long, scorecardId: Long, index: Long, fieldType: FieldType,nullWhen:NullWhen,
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

sealed trait FieldType {

  case class Count() extends FieldType

  case class Rating() extends FieldType

  case class Boolean() extends FieldType

}


sealed trait NullWhen {

  case class Checked() extends NullWhen

  case class UnChecked() extends NullWhen

}

