package hr.zlatko.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;



abstract class BaseActor extends AbstractActor{

	LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	
	public static final Object Poruka1 = new Object() {
		@Override
		public String toString() {
			return "Poruka1";
		}
	};

	public static final Object Poruka2 = new Object() {
		@Override
		public String toString() {
			return "Poruka2";
		}
	};
	
	public static final Object Nehendlana = new Object() {
		@Override
		public String toString() {
			return "Nehendlana";
		}
	};
	
	public Receive baseReceive(){
		return receiveBuilder().	             	        		
                //first message after recovery 
                matchEquals(Poruka1, msg -> {
                	logger.debug("Stigla Poruka1 u baznu klasu");
                	System.out.println("Stigla Poruka1 u baznu klasu");
                }).
                build();
	}
	
	abstract public Receive inheritedReceive();
	
	@Override
	public Receive createReceive() {
		return baseReceive().orElse(inheritedReceive());
	}
	
}



public class ActorInheritance extends BaseActor{

	@Override
	public Receive inheritedReceive() {
		return receiveBuilder().	             	        		
                //first message after recovery 
                matchEquals(Poruka2, msg -> {
                	logger.debug("Stigla Poruka2 u nasljedjenu klasu klasu");
                	System.out.println("Stigla Poruka2 u nasljedjenu klasu");
                }).build();
	}
	
	public static void main(String[] args) {
		System.out.println("Starting");
		ActorSystem system = ActorSystem.create();
		
		ActorRef inheritedActor = system.actorOf(Props.create(ActorInheritance.class));
		inheritedActor.tell(Poruka1, ActorRef.noSender());
		inheritedActor.tell(Poruka2, ActorRef.noSender());		
		System.out.println("Ending");
		
	}
	
}




