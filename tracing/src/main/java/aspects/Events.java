package aspects;

import akka.actor.ActorRef;
import akka.serialization.Serialization;

public class Events {

  static public String Up(ActorRef ref) {
    return "UP|" + pathWithAddress(ref);
  }

  static public String Down(ActorRef ref) {
    return "DN|" + pathWithAddress(ref);
  }

  private static String pathWithAddress(ActorRef ref) {
    return Serialization.serializedActorPath(ref);
  }
}
