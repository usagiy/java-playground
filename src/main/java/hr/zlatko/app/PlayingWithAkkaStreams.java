package hr.zlatko.app;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
//import akka.http.javadsl.model.ws.Message;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;


public class PlayingWithAkkaStreams {

	
	private final static Logger logger = LoggerFactory.getLogger(PlayingWithAkkaStreams.class);

	static final ActorSystem system = ActorSystem.create("QuickStart");
	static final Materializer materializer = ActorMaterializer.create(system);


	public static void run(){
		
		//final ActorSystem system = ActorSystem.create("QuickStart");
		
		
		final Source<Integer, NotUsed> source = Source.range(1, 100);
		final CompletionStage<Done> done1 = source.runForeach(i -> logger.info("Ispis iz sourca: {}",i), materializer);
		done1.thenRun(() -> System.out.println("Ispisano"));
		
		
		final Source<BigInteger, NotUsed> factorials = source.scan(BigInteger.ONE, (acc, next) -> acc.add(BigInteger.valueOf(next)));
		//piswe u file
		final CompletionStage<IOResult> result = factorials.map(num -> ByteString.fromString(num.toString() + "\n")).runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer);
		//factorials.runForeach(i -> logger.info("Ispis iz sourca 2 : {}",i), materializer);
		
		
		//Throttle
		final CompletionStage<Done> done =
				  factorials
				    .zipWith(Source.range(0, 10), (num, idx) -> String.format("%d! = %s", idx, num))
				    .throttle(1, Duration.create(1, TimeUnit.SECONDS), 1, ThrottleMode.shaping())
				    .runForeach(s -> System.out.println(s), materializer);
		
		
		
		/*
		 * after constructing the RunnableGraph by connecting all the source, sink and different processing stages, 
		 * no data will flow through it until it is materialized
		 */
		
		final Source<Integer, NotUsed> source1 =
			    Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
			// note that the Future is scala.concurrent.Future
			final Sink<Integer, CompletionStage<Integer>> sink1 = Sink.<Integer, Integer> fold(0, (aggr, next) -> aggr + next);
			 
			// connect the Source to the Sink, obtaining a RunnableFlow
			final RunnableGraph<CompletionStage<Integer>> runnable = source1.toMat(sink1, Keep.right());
			 
			// materialize the flow
			final CompletionStage<Integer> sum = runnable.run(materializer);
			
			
			sum.thenAcceptAsync(new Consumer<Integer>() {
				@Override
				public void accept(Integer t) {
					logger.info("Calculated summ: {}", t);
					
				}
			});
			
			sum.thenAcceptAsync((t) -> { logger.info("Calculated summ lambda way: {}", t);});
			
			
			
			//example 2
			final Source<Integer, NotUsed> source2 =
				    Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
				final Sink<Integer, CompletionStage<Integer>> sink =
				    Sink.<Integer, Integer> fold(0, (aggr, next) -> aggr + next);
				 
				// materialize the flow, getting the Sinks materialized value
				final CompletionStage<Integer> sum2 = source.runWith(sink, materializer);
				
				
				sum.thenAcceptAsync((t) -> { logger.info("Calculated sum2 : {}", t);});
				
				
				// Explicitly creating and wiring up a Source, Sink and Flow
				Source.from(Arrays.asList(1, 2, 3, 4))
				  .via(Flow.of(Integer.class).map(elem -> elem * 2))
				  .to(Sink.foreach(System.out::println)).run(materializer);
				 
				// Starting from a Source
				final Source<Integer, NotUsed> source4 = Source.from(Arrays.asList(1, 2, 3, 4))
				    .map(elem -> elem * 2);
				source4.to(Sink.foreach(System.out::println)).run(materializer);
				 
				// Starting from a Sink
				final Sink<Integer, NotUsed> sink4 = Flow.of(Integer.class)
				    .map(elem -> elem * 2).to(Sink.foreach(System.out::println));
				Source.from(Arrays.asList(1, 2, 3, 4)).to(sink);
			
				//Buffers and working with rate
				Source.from(Arrays.asList(1, 2, 3))
				  .map(i -> {System.out.println("A: " + i); return i;}).async()
				  .map(i -> {System.out.println("B: " + i); return i;}).async()
				  .map(i -> {System.out.println("C: " + i); return i;}).async()
				  .runWith(Sink.ignore(), materializer);
				
				
				
				
				
				// ------------- NOVI PRIMJERI ---------------
				//--------------------------------------------
				Source.single("----- single element -----").to(Sink.foreach(System.out::println)).run(materializer);
				//beskonacno ispisuje 5
				//Source.repeat(5).to(Sink.foreach(System.out::println)).run(materializer);
				//ispise 10 petica
				//source se spaja na sink pomocu to
				Source.repeat(5).take(10).to(Sink.foreach(n -> System.out.println("Ispisujem:" + n ))).run(materializer);
				
				//forwardiranje vrijednosti u actor
				ActorRef greetActor = system.actorOf(Props.create(GreetPrinter.class));				
				Sink<GreetingMessage, ?> actorSink = Sink.actorRef(greetActor, PoisonPill.getInstance());
				
				Source<GreetingMessage, NotUsed> sourceActor =
					    Source.from(Arrays.asList(new GreetingMessage("message 1"), new GreetingMessage("message 2")));				
				sourceActor.to(actorSink).run(materializer);
				
				
				//Flow - transformacija !!!!!
				Source<Integer, NotUsed> source5 = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
				Sink<Integer, ?> sink5 = Sink.foreach(System.out::println);
				Flow<Integer, Integer, NotUsed> doubler = Flow.of(Integer.class).map(elem -> elem * 2);
				Flow<Integer, Integer, NotUsed> invertor = Flow.of(Integer.class).map(elem -> elem * -1);
				source5.via(doubler).via(invertor).via(doubler).to(sink5).run(materializer);
				
			
				
				// ------------- TWEET - STREAM BROADCASTING ---------------
				//----------------------------------------------------------
				


				Source<Tweet, NotUsed> tweets =  Source.range(1, 20).map(num -> new Tweet(new Author("AUTHOR" + num.toString()), new Hashtag("HASHTAG" + num.toString())));
				
				//for test
				//Sink<Tweet, ?> tweetsSink = Sink.foreach(i -> System.out.println(i.getAuthor().getName() + " " + i.getHashtag().getHashTag()));
				//tweets.to(tweetsSink).run(materializer);
				
				//Broadcasting in two streams
				Sink<Author, ?> writeAuthors = Sink.foreach(System.out::println);
				//zelimo ispisat samo autore
				Sink<Hashtag, ?> writeHashtags = Sink.ignore(); //Sink.foreach(System.out::println);
				RunnableGraph.fromGraph(GraphDSL.create(b -> {
				  final UniformFanOutShape<Tweet, Tweet> bcast = b.add(Broadcast.create(2));
				  final FlowShape<Tweet, Author> toAuthor =
					  b.add(Flow.of(Tweet.class).map(t -> t.getAuthor()));
				  final FlowShape<Tweet, Hashtag> toTags =
						  b.add(Flow.of(Tweet.class).map(t -> t.getHashtag()));
				  /*
				  final FlowShape<Tweet, Hashtag> toTags =
				      b.add(Flow.of(Tweet.class).mapConcat(t -> new ArrayList<Hashtag>(t.hashtags())));
				  */
				  final SinkShape<Author> authors = b.add(writeAuthors);
				  final SinkShape<Hashtag> hashtags = b.add(writeHashtags);

				  b.from(b.add(tweets)).viaFanOut(bcast).via(toAuthor).to(authors);
				                             b.from(bcast).via(toTags).to(hashtags);
				  return ClosedShape.getInstance();
				})).run(materializer);
				
				
				
				
				
				
				
				
		
		
		
	}
	
	public Sink<String, CompletionStage<IOResult>> lineSink(String filename) {
		  return Flow.of(String.class)
		    .map(s -> ByteString.fromString(s.toString() + "\n"))
		    .toMat(FileIO.toPath(Paths.get(filename)), Keep.right());
		}
	
	
	 
	//simple actor
	 public static class GreetPrinter extends AbstractActor {	       
			@Override
			public Receive createReceive() {
				// TODO Auto-generated method stub
				return receiveBuilder().
						 match(GreetingMessage.class, message -> System.out.println("Actor recieved :" + message.message)).
		                    build();
			}
	  }
	 
	 //message to actor
	 public static class GreetingMessage implements Serializable {
	        public static final long serialVersionUID = 1;
	        public final String message;
	        public GreetingMessage(String message) {
	            this.message = message;
	        }
	    }
	 
	 
	 
	 
	 
	 
	 
	 //for tweet broadcasting ----------------------------
	 public static class Author {
		  public final String name;

		public Author(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "Author [name=" + name + "]";
		}
		
		
	 }
	 
	 public static class Hashtag {
		  public final String tag;

		public Hashtag(String tag) {
			this.tag = tag;
		}

		public String getHashTag() {
			return tag;
		}

		@Override
		public String toString() {
			return "Hashtag [tag=" + tag + "]";
		}
	 }
	 
	 
	 
	 public static class Tweet {
		public final Author author;		  
		public final Hashtag body;
		  
		public Tweet(Author author, Hashtag body) {
			this.author = author;
			this.body = body;
		}

		public Author getAuthor() {
			return author;
		}

		public Hashtag getHashtag() {
			return body;
		}

		@Override
		public String toString() {
			return "Tweet [author=" + author.getName() + ", body=" + body.getHashTag() + "]";
		}
	 }	  
		  
	 
	 
	 
	 
	 
	 
	
	
}
