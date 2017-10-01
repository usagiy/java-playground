package hr.zlatko.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hr.zlatko.rabbitmq.Messages.ResponseMessage;
import hr.zlatko.rabbitmq.Messages.QueueAck;
import hr.zlatko.rabbitmq.Messages.QueueNoAck;
import hr.zlatko.rabbitmq.Messages.QueueNoAckDelayed;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import akka.actor.AbstractActor;
import akka.actor.AbstractActor.Receive;
import akka.dispatch.MessageDispatcher;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;



/**
 *  
 * @author zlatko
 * 
 *  Abstract RabbitMQ consumer
 *  Concrete consumers should extend this one
 *
 *
 */
public abstract class RabbitMQConsumer extends AbstractActor {
		
	public static final String ACTOR_NAME = "RabbitMQConsumer";
	private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	//final SettingsImpl settings = Settings.SettingsProvider.get(context().system());
	final Connection connection;
	final Channel channelIncomingMessages;	
	final Consumer incomingConsumer;	
	final String queueName;
	//store last delivery tag
	//TODO better store it in temporary actor
	long deliveryTag = -1;
	final Map<String, Long> correlationMap;
	//delay 
	int noAckDelay = 1;
	//MessageDispatcher blockingDispatcher = context().system().dispatchers().lookup("blocking-dispatcher");
	ExecutorService executorService = Executors.newFixedThreadPool(5, r -> {Thread t = new Thread(r);
																	 t.setName("rabbitmq-zlatko-test");
																	 return t;});
	
	
	
	/**
	 * In case of exception supervisor should restart actor
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQConsumer(String rabbitMQHost, String queueName) throws IOException, TimeoutException {
		
		this.queueName = queueName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitMQHost);
		//factory.setUsername(settings.RABBITMQ_USERNAME);
		//factory.setPassword(settings.RABBITMQ_PASSWORD);
		//connection = factory.newConnection();
		connection = factory.newConnection(executorService);
		channelIncomingMessages = connection.createChannel();
		//number of unacknowledged messages in the same time
		//it will not preserve order in case of some messages fail 
		//Consumer Prefetch - number of unacknowledged messages		
		channelIncomingMessages.basicQos(10);
		//will guarantee order - it will retry to send single message  
		//in case of my exception it will stuck
		//TODO ovo koristi
		//channelIncomingMessages.basicQos(1);
		//queue is durable
		boolean durable = true;
		//correlation id map
		correlationMap = new HashMap<String, Long>();
		
		
		

		//declaring durable queue
		channelIncomingMessages.queueDeclare(queueName, durable, false, false, null);
		//enable acknowledge on sending
		//channelIncomingMessages.confirmSelect();

		
		incomingConsumer = new DefaultConsumer(channelIncomingMessages) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
		        String message = new String(body, "UTF-8");
		        handleMessageDelivery(message, envelope.getDeliveryTag());		        
		      }
		    };	
	}
	
	
	public abstract void handleMessageDelivery(String messageBody, long deliveryTag) throws JsonParseException, JsonMappingException, IOException; 
	
	
	@Override
	public void preStart() throws Exception {
		channelIncomingMessages.basicConsume(queueName, false, incomingConsumer);
		super.preStart();
	}
	
	
	@Override
	public Receive createReceive() {
		 return receiveBuilder().			
			match(QueueAck.class, m -> {
				logger.debug("RabbitMQConsumer received QueueAck with delivery tag {}", m.getDeliveryTag());
				receiveAcknowledge(m.getDeliveryTag());
				//ok reset delay
				noAckDelay = 1;
			}).
			match(QueueNoAck.class, m -> {				
				//receiveDeny(m.getDeliveryTag());
				if (noAckDelay < 64){
					noAckDelay = noAckDelay * 2;
				}
				logger.debug("{} consumer delay {}", ACTOR_NAME, noAckDelay);
				context().system().scheduler().scheduleOnce(Duration.create(noAckDelay, TimeUnit.SECONDS),
						  self(), new QueueNoAckDelayed(m), context().system().dispatcher(),sender());

			}).
			match(QueueNoAckDelayed.class, m -> {				
				receiveDeny(m.getDeliveryTag());				 				 
			}).
			
			match(ResponseMessage.class, m -> {				
				if (correlationMap.containsKey(m.getMessageId())){
					if (m.getStatusCode().isSuccess()){
						receiveAcknowledge(correlationMap.get(m.getMessageId()).longValue());
						correlationMap.remove(m.getMessageId());
					}
					else{
						receiveDeny(correlationMap.get(m.getMessageId()).longValue());
					}
				}
				else{
					logger.error("{} received ResponseMessage with unknown messageId");
				}
				
				
			}).
			
			matchAny(m -> logger.error("RabbitMQConsumer received unhandled message: {}",m )).build();	
	}
	
	protected void addDeliveryTagForMessageId(String messageId, Long deliveryTag){
		correlationMap.put(messageId, deliveryTag);
	}
	
	
	
	
	
	/**
	 * Acknowledge consumed messages
	 * @param televendResponse
	 * @throws IOException 
	 * @throws  
	 */
	private void receiveAcknowledge(long deliveryTag) throws IOException{
	
				
		logger.debug("Message Queue Consumer  Acknowledge with tag: {}",deliveryTag);
		channelIncomingMessages.basicAck(deliveryTag, false);

	}
	
	/**
	 * Deny receiving of message
	 * @param gueueNoAck
	 * @throws IOException
	 */
	private void receiveDeny(long deliveryTag) throws IOException {
				
		logger.debug("Message Queue Consumer  decline acknowledge with tag: {}", deliveryTag);
		channelIncomingMessages.basicNack(deliveryTag, false, true);
	}
	
	
	@Override
	public void postStop() throws Exception {
		//close channel and connection
		channelIncomingMessages.close();
	    connection.close();
		super.postStop();
	}	
}
