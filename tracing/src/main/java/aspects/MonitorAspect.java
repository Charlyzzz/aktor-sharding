package aspects;

import akka.actor.ActorCell;
import akka.actor.ActorRef;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Aspect
public class MonitorAspect {

  @Inject
  ActorRef f;

  private Queue<String> pendingPaths = new ConcurrentLinkedQueue<>();


  @AfterReturning(pointcut = "execution (* akka.actor.ActorRefFactory.actorOf(..))", returning = "ref", argNames = "ref")
  public void actorSpawned(ActorRef ref) {
    String up = Events.Up(ref.path());
    forwardUserActorsEvent(up);
  }

  private void forwardUserActorsEvent(String event) {
    if (event.contains("/user/"))
      tell(event);
  }

  private void tell(String event) {
    if (f != null) {
      pendingPaths.forEach(s -> f.tell(s, ActorRef.noSender()));
      pendingPaths.clear();
      f.tell(event, ActorRef.noSender());
    } else {
      pendingPaths.add(event);
    }
  }

  @Before(value = "execution (* akka.actor.ActorCell.stop()) && this(cell)", argNames = "cell")
  public void beforeStop(ActorCell cell) {
    String down = Events.Down(cell.actor().self().path());
    forwardUserActorsEvent(down);
  }
}