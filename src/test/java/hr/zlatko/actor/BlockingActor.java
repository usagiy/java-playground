package hr.zlatko.actor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.LogEntry;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.javadsl.Flow;
import kamon.Kamon;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.server.AllDirectives;
import scala.languageFeature.postfixOps;
import static akka.event.Logging.InfoLevel;


class Receiver extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder().
				matchAny(m -> BlockingActor.longRunnigTask()).build();
	}
	
}

class RouteFactory extends AllDirectives{
	
	private final ActorSystem system;
	
	
	public RouteFactory(ActorSystem system) {
		this.system = system;
		
	}
	
	Function<HttpRequest, LogEntry> requestMethodAsInfo = (request) -> LogEntry.create("Zlatko " + request.method().name() + " " + request, InfoLevel());
	BiFunction<String, String, String> justForFun = (f,s) ->  f + s; 
	
	public Route createRoute(){
		
		MessageDispatcher blockingDispatcher = system.dispatchers().lookup("blocking-dispatcher");
		
		
		return logRequest(requestMethodAsInfo, () -> route(
						 get(() -> complete(StatusCodes.OK)),
						 post(() -> {
							 try { 
								//TimeUnit.SECONDS.sleep(5);
								 //blocking
								 /*
								 BlockingActor.longRunnigTask();
								 return complete(StatusCodes.OK);
								 */
								 CompletionStage<HttpResponse> cs = CompletableFuture.supplyAsync( ( ) -> {
										BlockingActor.longRunnigTask();
										return HttpResponse.create().withStatus(StatusCodes.OK);
									},blockingDispatcher );
								 return completeWithFuture(cs);							 
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return complete(StatusCodes.INTERNAL_SERVER_ERROR);
							}
							 //return complete(StatusCodes.OK);
							 
						 })
				));
	}
	
}


public class BlockingActor {
	
	private final static Logger logger = LoggerFactory.getLogger(BlockingActor.class);
	 
	public static BigInteger longRunnigTask(){
		BigInteger veryBig = new BigInteger(3000, new Random());
		veryBig = veryBig.nextProbablePrime();
    	logger.debug("veryBig:{}", veryBig);
    	return veryBig;
	}
	
	
	
			//post; 
	
	
	//reactive stream writing u rabbit clanak u mail-a 
	//Stream
	
	//wait thread implementation
	
	//DDD actors, reactive process vugh vernon
	
	//cluster
	

	
	
	public static void main(String[] args) throws IOException, TimeoutException {
		
		//Start kamon monitoring 
		//Kamon.start();
		
		ActorSystem system = ActorSystem.create();
		//"api-materializer" prefix imena actora koje dispatcher kreira 
		ActorMaterializer materializer = ActorMaterializer.create(ActorMaterializerSettings.create(system), system, "api-materializer"); //(system);
		
		//test blocking actor - radi samo u 1 threadu koi je na actoru
		/*
		ActorRef blockingActor = system.actorOf(Props.create(Receiver.class));
		IntStream.range(0, 100).forEach(i -> blockingActor.tell("msg", ActorRef.noSender()));
		*/
		//blocking api
		
		//ako napravinm vise upita
	   
	   RouteFactory routeFactory = new RouteFactory(system);
	   Http http = Http.get(system);
	   final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routeFactory.createRoute().flow(system, materializer);
	   
	    //za svaki request se kreira actor - default/user/StreamSupervisor/api-materializer... koji se vrti u jednom thredu dispatcher-a	   
	    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
	        ConnectHttp.toHost("localhost", 8080), materializer);

	    System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
	    System.in.read(); // let it run until user presses return

	    binding
	        .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
	        .thenAccept(unbound -> system.terminate()); // and shutdown when done
	    

		//Stop kamon monitoring 
		//Kamon.shutdown();
	  }
		
		
		
		
    }
	
	
	
	
	

