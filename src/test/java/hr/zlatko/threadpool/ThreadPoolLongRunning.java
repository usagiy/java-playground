package hr.zlatko.threadpool;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolLongRunning {
	
	
private final static Logger logger = LoggerFactory.getLogger(ThreadPoolLongRunning.class);
	

	public static void main(String[] args) {
		
		Runtime.getRuntime().addShutdownHook(new Thread(){            
															public void run() { System.out.println("Shutdown Hook is running !");}        
														});       

		
		
		ExecutorService executor = Executors.newFixedThreadPool(20); //newSingleThreadExecutor();
		Runnable task = () -> {
			 try {
		        	String threadName = Thread.currentThread().getName();
		        	logger.debug("Start Task in Thread: {}",threadName);
		        	
		        	BigInteger veryBig = new BigInteger(3000, new Random());
		        	veryBig.nextProbablePrime();
		        	logger.debug("veryBig:{}", veryBig);
		        	
		        	
		        	
					logger.debug("Stop Task in Thread: {}",threadName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Throwable  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		};
		
		Runnable submitter_task = () -> {
			 try {
				 	TimeUnit.SECONDS.sleep(40);
				 	IntStream.range(0, 10).forEach(i -> 
					{
						logger.debug("Submiter submiting task: {}",i);
						executor.submit(task);
					});
				 	TimeUnit.SECONDS.sleep(60);
		        	logger.debug("Submiter task submiting 10");
		        	IntStream.range(0, 10).forEach(i -> 
					{
						logger.debug("Submiter submiting task: {}",i);
						executor.submit(task);
					});					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		};
		
		//add submitter task
		executor.submit(submitter_task);
	
    }
	
	
	
	
	
}
