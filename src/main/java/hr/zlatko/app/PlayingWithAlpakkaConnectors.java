package hr.zlatko.app;

import akka.stream.alpakka.amqp.AmqpConnectionDetails;
import akka.stream.alpakka.amqp.AmqpConnectionSettings;
import akka.stream.alpakka.amqp.AmqpConnectionUri;
import akka.stream.alpakka.amqp.AmqpSinkSettings;
import akka.stream.alpakka.amqp.DefaultAmqpConnection;
import akka.stream.alpakka.amqp.IncomingMessage;
import akka.stream.alpakka.amqp.NamedQueueSourceSettings;
import akka.stream.alpakka.amqp.QueueDeclaration;
import akka.stream.alpakka.amqp.javadsl.AmqpSink;
import akka.stream.alpakka.amqp.javadsl.AmqpSource;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import hr.zlatko.app.PlayingWithAkkaStreams.Author;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.japi.function.Predicate;


//PRIMJER
//https://github.com/akka/alpakka/blob/master/amqp/src/test/java/akka/stream/alpakka/amqp/javadsl/AmqpConnectorsTest.java

public class PlayingWithAlpakkaConnectors {

	
	private final static Logger logger = LoggerFactory.getLogger(PlayingWithAlpakkaConnectors.class);

	static final ActorSystem system = ActorSystem.create("Alpakka");
	static final Materializer materializer = ActorMaterializer.create(system);
	
	//declare queue
	final static String queueName = "AlpakkaTest";
	final static QueueDeclaration queueDeclaration = QueueDeclaration.create(queueName);
	//final static String queueName = "dm-events";
	//final static QueueDeclaration queueDeclaration = QueueDeclaration.create(queueName);

	
	public static void run(){
				
		//write to queue
		writeToQueue();	
		//read from queue
		readFromQueue();
	}
	
	
	
		
	public static void writeToQueue() {
		//create sink
		//AmqpConnectionSettings amqpConnectionDetails = new AmqpConnectionDetails(hostAndPortList, credentials, virtualHost, sslProtocol, requestedHeartbeat, connectionTimeout, handshakeTimeout, shutdownTimeout, networkRecoveryInterval, automaticRecoveryEnabled, topologyRecoveryEnabled, exceptionHandler)
		
		//AmqpConnectionSettings amqpConnectionDetails = new AmqpConnectionUri("localhost:15672");
		AmqpConnectionDetails amqpConnectionDetails = AmqpConnectionDetails.create("localhost", 5672);
		
		//radi
		/* 
		AmqpConnectionDetails amqpConnectionDetails = AmqpConnectionDetails.create("localhost", 5672)
		          .withHostsAndPorts(Pair.create("localhost", 5672));
		*/		
		
		final Sink<ByteString, CompletionStage<Done>> amqpSink = AmqpSink.createSimple(
				  AmqpSinkSettings.create(amqpConnectionDetails)
				    .withRoutingKey(queueName)
				    .withDeclarations(queueDeclaration)
				);
		
		final List<String> input = Arrays.asList("one", "two", "three", "four", "five");
		CompletionStage<Done> done = Source.from(input).map(ByteString::fromString).runWith(amqpSink, materializer);
		//done.whenComplete((a,e) -> system.terminate());
	}
	
	
	
	
	public static void readFromQueue() {
		//prefetch count
		final Integer bufferSize = 10;
		final Source<IncomingMessage, NotUsed> amqpSource = AmqpSource.create(
		  NamedQueueSourceSettings.create(
		    DefaultAmqpConnection.getInstance(),
		    queueName
		  ).withDeclarations(queueDeclaration),
		  bufferSize
		);
		
		
		
		
		//Sink<IncomingMessage, ?> writeQueueMessages = Sink.foreach(System.out::println);
		//consumer
		//amqpSource.to(writeQueueMessages).run(materializer);
		
		Sink<String, ?> writeQueueMessages = Sink.foreach(System.out::println);		
		Flow<IncomingMessage, String, NotUsed> filterMessages = Flow.of(IncomingMessage.class).map(m -> m.bytes().utf8String());
															
															//filter(elem -> true/*(elem.bytes().toString().equals("two"))? true:false*/ ).
															//map(elem -> elem.bytes().toString());
		
		amqpSource.via(filterMessages).to(writeQueueMessages).run(materializer);
		
		
		//TODO neka ispise samo neke a ostale ne npr one, two
		
		
		//TODO dynamic throttling 
		
		
		//TODO requeue, rpc acknowledge itd ....
		
		//RPC 
			
		
	}
	

	
	
}
