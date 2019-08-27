package com.example.event

case class Event(eventType: String, address: String) {

}

object Event {

  def up(address: String): Event = {
    val regex = """akka://(\w+)@(\d+\.{3}\d+:\d+)(.*)""".r
    address match {
      case regex(system, host, rest) =>
        Event("UP", address)
    }

  }

  def down(address: String) = Event("UP", address)
}
