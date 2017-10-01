package hr.zlatko.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;


/**
 * 
 * Base class for Rabbit MQ producers
 *
 */

public class RabbitMQProducer extends AbstractActor {
	
	LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	final Connection connection;
	final Channel channel;
	final String queueName;
	//final SettingsImpl settings = Settings.SettingsProvider.get(context().system());
	

	
	/**
	 * In case of exception supervisor should restart actor
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public RabbitMQProducer(String rabbitMQHost, String queueName) throws IOException, TimeoutException {
		
		this.queueName = queueName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitMQHost);
		//factory.setUsername(settings.RABBITMQ_USERNAME);
		//factory.setPassword(settings.RABBITMQ_PASSWORD);
		connection = factory.newConnection();
		channel = connection.createChannel();
		//queue is durable
		boolean durable = true;
		//declaring durable queue
		channel.queueDeclare(queueName, durable, false, false, null);		
		channel.confirmSelect();
	}
	
	@Override
	public void postStop() throws Exception {
		//close channel and connection
		channel.close();
	    connection.close();
		super.postStop();
	}

	@Override
	public Receive createReceive() {
		// TODO Auto-generated method stub
		return receiveBuilder().
				matchAny(o -> logger.error("Received unknown message: {} ", o.getClass().getName())).build();
	}
	
	
}
