package com.example.json

import com.example.event.Event
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

object Protocol extends DefaultJsonProtocol {
  implicit val eventFormat: RootJsonFormat[Event] = jsonFormat3(Event.apply)
}