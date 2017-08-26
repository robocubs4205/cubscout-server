package com.robocubs4205.cubscout.model.scorecard

/**
  * Since model objects contain only ids of each other, this class is needed to represent a scorecard provided in a
  * Json Request
  */
case class ScorecardJsonTree(scorecard: Scorecard, sections: Seq[ScorecardSection],
                             roles: Seq[(RobotRole, Seq[(ScoreWeight, Long)])], defaultRole: String) {
  def validate: Seq[ScorecardValidationError] = {
    roles.flatMap(_._2).map(_._2).filter(index => !sections.exists(_.index == index))
      .map(OrphanWeightError) ++
      sections.groupBy(_.index).map(p => (p._1, p._2.size)).filter(_._2 > 1).keys
        .map(DuplicateSectionError) ++
      roles.flatMap(_._2).map(_._2).groupBy(x => x).filter(_._2.size > 1).keys.map(DuplicateWeightError) ++
      roles.map(_._1).groupBy(_.name).filter(_._2.size > 1).keys.map(DuplicateRoleNameError) ++
      (if (roles.map(_._1).exists(_.name == defaultRole)) Seq() else Seq(DefaultRoleNotDefined()))
  }
}

sealed trait ScorecardValidationError {
  def message: String
}

final case class DuplicateSectionError(index: Long) extends ScorecardValidationError {
  val message = "multiple scorecard sections had the same index " + index
}

final case class OrphanWeightError(index: Long) extends ScorecardValidationError {
  val message = "a weight specified a non-existent section index" + index
}

final case class DuplicateWeightError(index: Long) extends ScorecardValidationError {
  val message = "multiple weights had the same index " + index
}

final case class DuplicateRoleNameError(name: String) extends ScorecardValidationError {
  val message = "multiple roles had the same name " + name
}

final case class DefaultRoleNotDefined() extends ScorecardValidationError {
  val message = "the role name specified for the default role does not exist in the list of roles"
}
