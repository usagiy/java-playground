package hr.zlatko.rabbitmq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class TestRabbitMQPerformance {
	
	
private final static Logger logger = LoggerFactory.getLogger(TestRabbitMQPerformance.class);
	
	private final static String QUEUE_NAME = "PERFORMANCE_TEST";
	//private final static int NUM_MESSAGES = 100000;
	private final static int NUM_MESSAGES = 10;
	private final static String TEST_MESSAGE = "{\"eventType\":\"TELEVEND_MESSAGE\",\"evendData\":{\"@class\":\"com.intis.devicemanager.common.message.televend.TelevendMessages$DAudit\",\"pid\":\"11\",\"data\":\"LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONG AUDIT DATA BLAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"id\":\"11\",\"audit_number\":\"auditNumber\",\"externalMessageId\":null,\"command\":\"D_AUDIT\"}}";
	
	//100000 poruka za 5 sec ako nije durable
	//100000 poruka za 12 sec ako je durable
	private static void writeMethod1(Channel channel) {
		try {
			channel.basicPublish("", 
					QUEUE_NAME, 
					  //MessageProperties.TEXT_PLAIN,
					 new BasicProperties("application/json",
			                    null,
			                    null,
			                    2,
			                    0, null, null, null,
			                    null, null, null, null,
			                    null, null),
					TEST_MESSAGE.getBytes("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//100000 poruka za 14 sec
	private static void writeMethod2(Channel channel) {
		try {
			channel.basicPublish("", 
					QUEUE_NAME, 
					  //MessageProperties.TEXT_PLAIN,
					 new BasicProperties("application/json",
			                    null,
			                    null,
			                    2,
			                    0, null, null, null,
			                    null, null, null, null,
			                    null, null),
					TEST_MESSAGE.getBytes("UTF-8"));			
				 channel.waitForConfirmsOrDie();			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, TimeoutException {
		
		Runtime.getRuntime().addShutdownHook(new Thread(){            
															public void run() { System.out.println("Shutdown Hook is running !");}        
														});       		
		ExecutorService executor = Executors.newFixedThreadPool(4); //newSingleThreadExecutor();
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		logger.info("Is auto-recovery  enabled:{} ", factory.isAutomaticRecoveryEnabled());
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		boolean durable = true;
		//declaring durable queue
		channel.queueDeclare(QUEUE_NAME, durable, false, false, null);		
		channel.confirmSelect();				
		logger.info("Start writing test {} messages", NUM_MESSAGES);		
		long startTime = System.currentTimeMillis();		
		IntStream.range(0, NUM_MESSAGES).forEach(i -> writeMethod1(channel));		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		logger.info("Test finished for {} miliseconds", totalTime);
		channel.close();
		connection.close();		
    }
	
	
	
	
	
}
