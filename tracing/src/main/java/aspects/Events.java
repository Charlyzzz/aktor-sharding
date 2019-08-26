package aspects;

import akka.actor.ActorPath;

public class Events {

  static public String Up(ActorPath path) {
    return "UP|" + path;
  }

  static public String Down(ActorPath path) {
    return "DN|" + path;
  }
}
