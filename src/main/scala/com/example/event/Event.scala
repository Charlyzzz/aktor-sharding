package com.example.event

case class Event(host: String, branches: List[String], eventType: String)

object Event {

  def up(address: String): Event = fromAddress(address, "UP")

  def down(address: String): Event = fromAddress(address, "Down")

  private def fromAddress(address: String, eventType: String): Event = {
    val regex = """akka://(\w+)@(\d.+{3}\d+:\d+)(.*)""".r
    val (h, b) = address match {
      case regex(system, host, fullPath) =>
        val path: String = fullPath.split("#").collectFirst({ case x => x }).get
        val "" :: branches = path.split("/").toList
        (host, branches)
    }
    Event(h, b, eventType)
  }
}
