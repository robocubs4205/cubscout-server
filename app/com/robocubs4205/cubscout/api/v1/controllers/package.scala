package com.robocubs4205.cubscout.api.v1

/**
  * Created by trevor on 8/3/17.
  */
package object controllers {

  case class EtagDoesNotMatchException() extends Throwable

  case class GameNotFoundException() extends RuntimeException("The specified game does not exist")

  case class DistrictNotFoundException() extends RuntimeException("The specified district does not exist")

  case class EventNotFoundException() extends RuntimeException("The specified event does not exist")

  case class TeamNotFoundException() extends RuntimeException("The specified team does not exist")
}
