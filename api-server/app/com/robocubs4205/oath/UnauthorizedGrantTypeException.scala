package com.robocubs4205.oath

case object UnauthorizedGrantTypeException extends RuntimeException("Client is not authorized for the given grant type")
