package hr.zlatko.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigFactory;

import akka.Done;

import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.ProducerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Producer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TestKafka {

	
	
	//classic kafka consumer with autocommit
	/*
	 *Setting offset when group is first time created
	 *When the group is first created, the position will be set according 
	 *to the reset policy (which is typically either set to the earliest or latest offset for each partition)
	 * 
	 */
	
	
	public static class SimpleAutocommitConsumer {

		public SimpleAutocommitConsumer(String topicName) throws Exception {
		    
		      //Kafka consumer configuration settings
		      //String topicName = args[0].toString();
		      Properties props = new Properties();
		      
		      //reset policy kad je grupa prvi put kreirana
		      //defaultno - auto.offset.reset = latest
		      props.put("auto.offset.reset", "earliest");
		      props.put("bootstrap.servers", "192.168.99.100:9092");
		      //grupa
		      props.put("group.id", "simpleConsumer1");
		      //enables offset autocommit
		      props.put("enable.auto.commit", "true");
		      props.put("auto.commit.interval.ms", "1000");
		      props.put("session.timeout.ms", "30000");
		      props.put("key.deserializer", 
		         "org.apache.kafka.common.serialization.StringDeserializer");
		      props.put("value.deserializer", 
		         "org.apache.kafka.common.serialization.StringDeserializer");
		      KafkaConsumer<String, String> consumer = new KafkaConsumer
		         <String, String>(props);
		      
		      //Kafka Consumer subscribes list of topics here.
		      consumer.subscribe(Arrays.asList(topicName));
		      
		      
		      //consumer.beginningOffsets(
		      
		      
		      
		      //print the topic name
		      System.out.println("Subscribed to topic " + topicName);
		      int i = 0;
		      
		      while (true) {
		         ConsumerRecords<String, String> records = consumer.poll(100);
		         for (ConsumerRecord<String, String> record : records)
		         
		         // print the offset,key and value for the consumer records.
		         System.out.printf("offset = %d, key = %s, value = %s\n", 
		            record.offset(), record.key(), record.value());
		      }
		   }
		}
	
	public static class SimpleManualCommitConsumer {
		
		public SimpleManualCommitConsumer(String topicName) {
			
			Properties props = new Properties();
		     props.put("bootstrap.servers", "192.168.99.100:9092");
		     props.put("group.id", "simpleManualConsumer2");
		     props.put("enable.auto.commit", "false");
		     props.put("auto.offset.reset", "earliest");
		     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		     KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		     consumer.subscribe(Arrays.asList(topicName));
		     final int minBatchSize = 50;
		     List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
		     while (true) {
		         ConsumerRecords<String, String> records = consumer.poll(100);
		         for (ConsumerRecord<String, String> record : records) {
		             buffer.add(record);
		         }
		         if (buffer.size() >= minBatchSize) {
		         //nikad ne commita jer uzme vise
		         //if (buffer.size() == minBatchSize) {
		        	 System.out.printf("---- Read batch size = %d ---- \n",buffer.size());
		             consumer.commitSync();
		             buffer.clear();
		         }
		     }
		}		
	}
	
	
	
	
	
	private final static Logger logger = LoggerFactory.getLogger(TestKafka.class);
	// ActorSystem system = ActorSystem.create("testSystem",
	// ConfigFactory.defaultReference());
	// load all config
	// static ActorSystem system = ActorSystem.create("testSystem",
	// ConfigFactory.load());
	static ActorSystem system;
	static ActorMaterializer materializer;
	static ProducerSettings<byte[], String> producerSettings;	
	static ConsumerSettings<byte[], String> consumerSettings;
	
	
	//helper classes for consumer
	
	static class DB {
		  private final AtomicLong offset = new AtomicLong();

		  public CompletionStage<Done> save(ConsumerRecord<byte[], String> record) {
		    System.out.println("DB.save: " + record.value());
		    offset.set(record.offset());
		    return CompletableFuture.completedFuture(Done.getInstance());
		  }

		  public CompletionStage<Long> loadOffset() {
		    return CompletableFuture.completedFuture(offset.get());
		  }

		  public CompletionStage<Done> update(String data) {
		    System.out.println("DB.update: " + data);
		    return CompletableFuture.completedFuture(Done.getInstance());
		  }
	}
	
	
	static class Rocket {
		public CompletionStage<Done> launch(String destination) {
			System.out.println("Rocket launched to " + destination);
			return CompletableFuture.completedFuture(Done.getInstance());
		}
	}
	
	
	
	
	
	
	
	
	
	
	@BeforeClass
	public static void setup() {
	
		logger.info("BeforeClass");

		system = ActorSystem.create("testSystem");
		materializer = ActorMaterializer.create(system);
		
		producerSettings = ProducerSettings
				  .create(system, new ByteArraySerializer(), new StringSerializer())
				  .withBootstrapServers("192.168.99.100:9092");
		
		consumerSettings =
			    ConsumerSettings.create(system, new ByteArrayDeserializer(), new StringDeserializer())
			  .withBootstrapServers("192.168.99.100:9092")
			  .withGroupId("akkaTest4")
			  .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		
		
	}
	
	
	@Test
	public void writeToKafkaTest() throws InterruptedException {

		CountDownLatch cdl = new CountDownLatch(1); 
		
		CompletionStage<Done> done =
				  Source.range(1, 100)
				    .map(n -> "ZLATKO" + n.toString()).map(elem -> new ProducerRecord<byte[], String>("TestTopic", elem))
				    .runWith(Producer.plainSink(producerSettings), materializer);
		
		done.whenComplete((d,e) -> {
									logger.debug("Writing finished Done: {}, Exception",d,e);
									cdl.countDown();
								});
		cdl.await();
	}
	
	
	
	@Test
	public void readFromKafkaWithoutCommitingTest() throws InterruptedException {
						
		CountDownLatch cdl = new CountDownLatch(1); 		
		final DB db = new DB();
		Consumer.committableSource(consumerSettings, Subscriptions.topics("SendToTelevend"))
		  .mapAsync(1, msg -> db.update(msg.record().value())
		    .thenApply(done -> {
		    	logger.debug("Successfully stored to db: {}", done);
		    	return msg;
		   }))
		  .mapAsync(1, msg -> {
			  logger.debug("Commiting message msg: {} with offset: {}", msg, msg.committableOffset());
			  //ne commitaj za vece od 170
			  long offset = msg.committableOffset().partitionOffset().offset();
			  if (offset >= 170){
				  logger.debug("Skip commiting for offset: {}", offset);
				  return CompletableFuture.completedFuture(Done.getInstance());
			  }
			  else 
				  return msg.committableOffset().commitJavadsl();
		   })
		  .runWith(Sink.ignore(), materializer);
		cdl.await();
	}
	
	
	@Test
	public void classicAutocommitClientTest() throws Exception {
		
		CountDownLatch cdl = new CountDownLatch(1); 
		//SimpleConsumer simpleConsumer = new SimpleConsumer("TestTopic");
		SimpleAutocommitConsumer simpleConsumer = new SimpleAutocommitConsumer("SendToTelevend");				
		cdl.await();
	}
	
	
	@Test
	public void classicManualClientTest() throws Exception {
		
		//String in log for setting offset
		//Resetting offset for partition SendToTelevend-0 to offset 0.
		
		CountDownLatch cdl = new CountDownLatch(1); 
		//SimpleConsumer simpleConsumer = new SimpleConsumer("TestTopic");
		SimpleManualCommitConsumer simpleConsumer = new SimpleManualCommitConsumer("SendToTelevend");				
		cdl.await();
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
