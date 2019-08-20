package aspects;

import akka.actor.ActorCell;
import akka.actor.ActorRef;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class MonitorAspect {

  @AfterReturning(pointcut = "execution (* akka.actor.ActorRefFactory.actorOf(..))", returning = "ref", argNames = "ref")
  public void actorSpawned(ActorRef ref) {
    System.out.println("start");
  }

  @Before(value = "execution (* akka.actor.ActorCell.stop()) && this(cell)", argNames = "cell")
  public void beforeStop(ActorCell cell) {
    System.out.println("stop");
  }

  @Before(value = "execution (* com.example.Foo.foo())")
  public void beforeStop2() {
    System.out.println("Foo");
  }
}