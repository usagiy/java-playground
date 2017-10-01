package hr.zlatko.actor;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Pair;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.Timeout;
import hr.zlatko.app.PlayingWithAkkaStreams.Tweet;
import akka.stream.*;
import akka.pattern.PatternsCS;



public class ReactiveStreamExperiments {
	
	static Logger logger = LoggerFactory.getLogger(ReactiveStreamExperiments.class);
	static final ActorSystem system = ActorSystem.create("QuickStart");
	static final Materializer materializer = ActorMaterializer.create(system);
	
	public static void streamThreads(){
		
		CompletionStage<Done> done = Source.single("Hello World").
									 map(s -> {logger.debug("{} {}", Thread.currentThread().getName(), s);
									 			return s;}).
									 map(s -> {logger.debug("{} {}", Thread.currentThread().getName(), s);
							 			return s;}). 
									 runWith(Sink.foreach(s -> logger.debug("{} {}", Thread.currentThread().getName(), s)), materializer);
		
		done.thenAccept(d -> system.terminate());
		
		
	}

	
	public static Flow<String, String, NotUsed> createProcessingStage(String name){
		Flow<String, String, NotUsed> processingStage = Flow.of(String.class).map(s -> {
			logger.debug("{} started processing {} on thread {}", name, s, Thread.currentThread().getName());
			TimeUnit.SECONDS.sleep(2);
			logger.debug("finished processing");
			return s;
		});
		return processingStage;
	}
	
	public static void streamThreads2(){
		/*
		Flow<String, String, NotUsed> processingStage = Flow.of(String.class).map(s -> {
															logger.debug("name {} started processing on thread {}", s, Thread.currentThread().getName());
															TimeUnit.SECONDS.sleep(2);
															logger.debug("finished processing");
															return s;
														});
		*/
		
		
		
		CompletionStage<Done> done = Source.from(Arrays.asList("Hello", "Streams", "World!")).
									 via(createProcessingStage("A")).async().
									 via(createProcessingStage("B")).async().
									 via(createProcessingStage("C")).async().
									 runWith(Sink.foreach(s -> logger.debug("Got output {}",s)), materializer);
		
		done.thenAccept(d -> system.terminate());
	}	

	
	
	
	/** Posalji prvom actoru - client actor 
	 	ako vrati ok - acknowledge 
	 	ako vrati error posalji drugom
	 	
	 **/
	
	
	static class MessageWithEnvelope {
		
		public final Integer messageId;
		public final String content;
		public MessageWithEnvelope(Integer messageId, String content) {
			this.messageId = messageId;
			this.content = content;
		}
		
		
	}
	
	static class CloudClient extends AbstractActor {
		  @Override
		  public Receive createReceive() {
		    return receiveBuilder()
		      .match(String.class, word -> {
		        // ... process message
		    	if(word.equals("crno"))
		    		getSender().tell("ERROR", getSelf());
		    	else
		    		getSender().tell("OK", getSelf());
		        //String reply = word.toUpperCase();
		        // reply to the ask
		        
		      })
		      .match(MessageWithEnvelope.class, m -> {
		    	  if (m.content == "OK")
		    		  getSender().tell("200", getSelf());
			    	else
			    		getSender().tell("500", getSelf());
		    	  
		    	  
		      })		      
		      .build();
		  }
		}
	
	public static void streamWithActors(){
		
		ActorRef ref = system.actorOf(Props.create(CloudClient.class));		
		
		
		Source<MessageWithEnvelope, NotUsed> inputMessages =
				  Source.from(Arrays.asList(new MessageWithEnvelope(1, "OK"), new MessageWithEnvelope(2,"ERROR")));
				Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);

				
		final Flow<MessageWithEnvelope, Object, NotUsed> cloudClient1 = Flow.of(MessageWithEnvelope.class).mapAsync(5, elem -> PatternsCS.ask(ref, elem, askTimeout));		
		
		
		
		//final Source<>
		
		
		
		
		
				
				
				//Keep Both Experiment -------------
				
				//RunnableGraph<Pair<Pair<CompletableFuture<Optional<Integer>>, Cancellable>, CompletionStage<Integer>>> r9 =
				//source.viaMat(flow, Keep.both()).toMat(sink, Keep.both());
				//
				final Flow<MessageWithEnvelope, String, NotUsed> flow = Flow.of(MessageWithEnvelope.class).map(m -> m.content);
				// A sink that returns the first element of a stream in the returned Future
				final Sink<String, CompletionStage<String>> sink1 = Sink.head();
				
				
				//RunnableGraph<Pair<MessageWithEnvelope, CompletionStage<String>>> r10 =
				//		inputMessages.viaMat(flow, Keep.right()).toMat(sink1, Keep.both());
				
				
				//----------------------------------
				
				
				//drito 
				inputMessages.mapAsync(5, elem -> PatternsCS.ask(ref, elem, askTimeout))
				  .map(elem -> (String) elem)
				  // continue processing of the replies from the actor
				  .map(elem -> elem.toLowerCase())
				  .runWith(Sink.foreach(s -> logger.debug("Got output {}",s)), materializer);
				
				
				//kreiramo graph ----------------------
				final Sink<List<String>, CompletionStage<List<String>>> sink = Sink.head();
				//poziv clienta
				final Flow<String, Object, NotUsed> cloudClient = Flow.of(String.class).mapAsync(5, elem -> PatternsCS.ask(ref, elem, askTimeout));
				//filtrira OK i to treba acknowledgat
				final Flow<String, String, NotUsed> filterOK = Flow.of(String.class).filter(elem -> elem.equalsIgnoreCase("OK"));
				
				//filtrira NotOK i to treba acknowledgat
				final Flow<String, String, NotUsed> filterNotOK = Flow.of(String.class).filter(elem -> !elem.equalsIgnoreCase("OK"));
				
				
				
				/*
				
				//
				final RunnableGraph<CompletionStage<List<String>>> result =
				RunnableGraph.fromGraph(GraphDSL.create(sink, (builder,out) -> {
					
					 final UniformFanOutShape<String, String> bcast = builder.add(Broadcast.create(2));
			         final UniformFanInShape<String, String> merge = builder.add(Merge.create(2));
					 
			         //source stringova
			         final Outlet<String> source = builder.add(words).out();
			         builder.from(source).via(builder.add(cloudClient)).
			         	viaFanOut(bcast)
			         
					
					
					
					
					return ClosedShape.getInstance();
				}));
				
				*/
				
				
				
				
				
				//ako je reply od clienta ok wrati acknowledge
				//ako nije vrati 
				
				
				//broadcastaj i onda filtriraj odgovor				
				//https://stackoverflow.com/questions/36681879/alternative-flows-based-on-condition-for-akka-stream
				
				//ili partition example
				//http://doc.akka.io/docs/akka/current/java/stream/stages-overview.html#partition
				//https://stackoverflow.com/questions/35225866/how-to-conditionally-pass-a-message-to-a-specific-next-stage-among-other-stages
				
				
				
				
				
				
				
	}
	
	
	
	
	//TU seigraj sa right i left to je rjesenje .....
	//
	//reusable part
	/*
	  Flow je Flow<String, String, NotUsed> - to je flow 
	  ako stavim keep left - Type mismatch: cannot convert from Sink<String,NotUsed> to Sink<String,CompletionStage<IOResult>>
	  ako stavim  Keep.both() - Type mismatch: cannot convert from Sink<String,Pair<NotUsed,CompletionStage<IOResult>>> to 
	 Sink<String,CompletionStage<IOResult>>
	  
	 */
	
	public Sink<String, CompletionStage<IOResult>> lineSink(String filename) {
		  return Flow.of(String.class)
		    .map(s -> ByteString.fromString(s.toString() + "\n"))
		    .toMat(FileIO.toPath(Paths.get(filename)), Keep.right());
		}
	

	
	public static void main(String[] args) {
		//streamThreads();
		//streamThreads2();
		streamWithActors();
	}
	
}
