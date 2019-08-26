package com.example.event

case class Event(eventType: String, address: String)

object Event {

  def up(address: String) = Event("UP", address)

  def down(address: String) = Event("UP", address)
}
