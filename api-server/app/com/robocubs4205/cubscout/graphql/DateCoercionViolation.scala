package com.robocubs4205.cubscout.graphql

import sangria.validation.ValueCoercionViolation

case object DateCoercionViolation extends ValueCoercionViolation("Date value expected")


