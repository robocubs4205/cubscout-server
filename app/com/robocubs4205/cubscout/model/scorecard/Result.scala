package com.robocubs4205.cubscout.model.scorecard

/**
  * Created by trevor on 7/29/17.
  */
case class Result(id:Long,matchId:Long,robotId:Long,scorecardId:Long)

case class FieldResult(id:Long,resultId:Long,fieldSectionId:Long,score:Float)
