package aspects;

import akka.actor.ActorCell;
import akka.actor.ActorPath;
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
    forwardUserActors(ref);
  }

  private void forwardUserActors(ActorRef ref) {
    ActorPath actorPath = ref.path();
    String path = actorPath.toString();
    if (path.contains("/user/"))
      tell(actorPath);
  }

  private void tell(ActorPath path) {
    if (f != null) {
      pendingPaths.forEach(s -> f.tell(s, ActorRef.noSender()));
      pendingPaths.clear();
      f.tell(path.toString(), ActorRef.noSender());
    } else {
      String pathString = path.toString();
      pendingPaths.add(pathString);
    }
  }

  @Before(value = "execution (* akka.actor.ActorCell.stop()) && this(cell)", argNames = "cell")
  public void beforeStop(ActorCell cell) {
    forwardUserActors(cell.actor().self());
  }
}