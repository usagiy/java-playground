package hr.zlatko.threadpool;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * @author zlatko
 *
 *CompletionStage interface can be created from CompletableFuture 
 *svaki od taskova se izvrsava na thread poolu kao zasebni runnavble 
 *
 *
 */





public class ThreadPoolCompletionStage {
	

	
	
	
	
	
	
private final static Logger logger = LoggerFactory.getLogger(ThreadPoolCompletionStage.class);
	
	
	public static BigInteger longRunnigTask(){
		BigInteger veryBig = new BigInteger(3000, new Random());
		veryBig = veryBig.nextProbablePrime();
    	logger.debug("veryBig:{}", veryBig);
    	return veryBig;
	}

	public static void main(String[] args) {
		
		Runtime.getRuntime().addShutdownHook(new Thread(){            
															public void run() { System.out.println("Shutdown Hook is running !");}        
														});       

		
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(20); //newSingleThreadExecutor();
		
		longRunnigTask();
		/*
		CompletableFuture<BigInteger>.supplyAsync(( ) -> {
			// big task
			return 10 / 0; // exception division by zero
			// return 10 / 2;
		} ); 
		*/
		//10 puta stavi u executor
		CompletionStage<BigInteger> longRunning = CompletableFuture.supplyAsync( ( ) -> {
				return longRunnigTask(); 
			},executor );
		//longRunning.whenCompleteAsync((bi,ex) -> longRunning );//( (BigInteger bi) -> {return longRunnigTask();});
		
		/**
		 * SVE NASTAVLJA U ISTOM THREAD-u
		 */
		/*
		longRunning.thenApply(r -> longRunnigTask()).		
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask()).
		thenApply(r -> longRunnigTask());
		*/
		
		/**
		 * SVE NASTAVLJA U FORK-JOIN POOLU - ForkJoinPool.commonPool-worker-n
		 */
		/*
		longRunning.thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask()).
		thenApplyAsync(r -> longRunnigTask());
		*/
		
		/**
		 * SVE NASTAVLJA U ISTOM EXECUTORU pool-1-thread-n
		 */
		
		longRunning.thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor).
		thenApplyAsync(r -> longRunnigTask(), executor);
			
    }
	
	
	
	
	
}
