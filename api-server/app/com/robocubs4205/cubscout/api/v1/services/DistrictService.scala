package com.robocubs4205.cubscout.api.v1.services

import javax.inject.Inject

import com.robocubs4205.cubscout.CubScoutDb
import com.robocubs4205.cubscout.model.District

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 8/5/17.
  */
class DistrictService @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) {

  import csdb._
  import dbConfig._
  import profile.api._

  def findById(id: Long) = districts.filter(district => district.id === id).result.headOption

  def checkNoInsertConflict(district: District) = {
    for {
      sameCodeDistrict <- districts.filter(_.code === district.code).result.headOption
      sameNameDistrict <- districts.filter(_.name === district.name).result.headOption
    } yield if (sameCodeDistrict.isDefined) DBIO.failed(DistrictWithCodeExistsException())
    else if (sameNameDistrict.isDefined) DBIO.failed(DistrictWithNameExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoReplaceConflict(district: District, id: Long) = {
    for {
      sameCodeDistrict <- districts.filter(_.code === district.code).filter(_.id =!= id).result.headOption
      sameNameDistrict <- districts.filter(_.name === district.name).filter(_.id =!= id).result.headOption
    } yield if (sameCodeDistrict.isDefined) DBIO.failed(DistrictWithCodeExistsException())
    else if (sameNameDistrict.isDefined) DBIO.failed(DistrictWithNameExistsException())
    else DBIO.successful(())
  }.flatten

  def checkNoDeleteConflict(id: Long) = {
    for {
      events <- events.filter(_.districtId === id).result
      teams <- teams.filter(_.districtId === id).result
    } yield if (events.nonEmpty) DBIO.failed(EventsInDistrictException())
    else if (teams.nonEmpty) DBIO.failed(TeamsInDistrictException())
    else DBIO.successful(())
  }.flatten

  def doInsert(district: District) =
    (districts returning districts.map(_.id) into ((district, id) => district.copy(id = id))) += district

  def doReplace(district: District, id: Long) =
    districts.filter(_.id === id).update(district.copy(id = id)).map(_ => Some(district.copy(id = id)))

  def doDelete(id: Long) = for {
    district <- findById(id)
    _ <- districts.filter(_.id === id).delete
  } yield district

  private case class EventsInDistrictException()
    extends RuntimeException("There are events in that district, which prevents its deletion")

  private case class TeamsInDistrictException()
    extends RuntimeException("There are teams in that district, which prevents its deletion")

  private case class DistrictWithCodeExistsException()
    extends RuntimeException("A district with that code already exists")

  private case class DistrictWithNameExistsException()
    extends RuntimeException("A district with that name already exists")

}
