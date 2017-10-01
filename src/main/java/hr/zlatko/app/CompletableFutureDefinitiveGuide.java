package hr.zlatko.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CompletableFutureDefinitiveGuide {
	private final static Logger logger = LoggerFactory.getLogger(CompletableFutureDefinitiveGuide.class);

	public static void run() throws InterruptedException, ExecutionException{
		
		CompletableFuture<String> fs = ask();		
		
		//stop waiting mora bit prije jel je isti thread
		fs.complete("dosta cekanja");
		
		//wait forever blocks here if not completed get is blocking way of consuming futures 
		System.out.println(fs.get());
		
	
		//DRUGE OPCIJE ZA KREIRANJE FUTURE-a
		
		//koristi defaultni  ForkJoinPool.commonPool() (global, general purpose pool introduces in JDK 8)
		final CompletableFuture<String> f1 = CompletableFuture.supplyAsync(new Supplier<String>() {
				@Override
				public String get() {
				//...long running...
					return "42";
				}
			});		
		//System.out.println(f1.get());
		
		
		//We will create an ExecutorService rather than depending on ForkJoinCommonPool
		ExecutorService executor = Executors.newCachedThreadPool();		
		final CompletableFuture<String> f2 = CompletableFuture.supplyAsync(new Supplier<String>() {
			@Override
			public String get() {
			//...long running...
				return "f2 42";
			}
		}, executor);	
		System.out.println(f2.get());

		//LABDA WAY
		//-------------------------------------------
		final CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {
				//...long running...
				return "42";
			}, executor);
		System.out.println(f3.get());
		
		
		//CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> longRunningTask(params), executor);
		
		//Transforming and acting on ONE future
		/*
		 *transformations are neither executed immediately nor blocking. 
		 *They are simply remembered and when original f1 completes they are executed for you. 
		 *If some of the transformations are time-consuming, you can supply your own Executor 
		 *to run them asynchronously. Notice that this operation is equivalent to monadic map in Scala 
		 */
		//--------------------------------------------
		CompletableFuture<Integer> f4 = f1.thenApply(Integer::parseInt);
		CompletableFuture<Double> f5 = f4.thenApply(r -> r * r * Math.PI);
		
		//CompletableFuture is a monad - mogu se kombinirati
		CompletableFuture<Double> f6 = f1.thenApply(Integer::parseInt).thenApply(r -> r * r * Math.PI);

		//HANDLING EXCEPTIONS
		CompletableFuture<String> f7 = f3.exceptionally(ex -> "We have a problem: " + ex.getMessage());
		CompletableFuture<Object> safe = f6.handle((ok, ex) -> {
		    if (ok != null) {
		        return ok;
		    } else {
		        logger.error("Problem wrong number format", ex);
		        return -1;
		    }
		});
		

		
		
		/*CONSUMING FUTURES Running code on completion (thenAccept/thenRun) - final stage in future pipeline
		 * consume future value when it's ready
		 ------------------------------------------------------------------------------------------------------
		 */		
		f6.thenAcceptAsync(new Consumer<Double>() {
			public void accept(Double result) {
				logger.info("Consuming future: {}", result);
			};
		}, executor);
		
		f6.thenAcceptAsync((result) -> { logger.info("Consuming future lambda way: {}", result);}, executor);

		
		
		
		
		/**
		 * Combining two CompletableFuture together
		 * ---------------------------------------------------------
		 * ---------------------------------------------------------
		 */
		
		//Combining (chaining) two futures (thenCompose())
		/*
		 * thenCompose() is an essential method that allows building robust, asynchronous pipelines, 
		 * without blocking or waiting for intermediate steps.
		 */
		
		
		//Transforming values of two futures (thenCombine())
		 CompletableFuture<Integer> comb1 = CompletableFuture.supplyAsync(() -> {return 7;}, executor);
		 CompletableFuture<Integer> comb2 = CompletableFuture.supplyAsync(() -> {return 8;}, executor);
		 //using function reference
		 CompletableFuture<Integer> combRes = comb1.thenCombine(comb2, CompletableFutureDefinitiveGuide::calculateSum);
		 combRes.thenAcceptAsync((result) -> {logger.info("Result of combining futures s executorom: {}", result);},executor);
		 combRes.thenAcceptAsync((result) -> {logger.info("Result of combining futures bez executora: {}", result);});
		 
		 //Waiting for both CompletableFutures to complete - thenAcceptBoth()/runAfterBoth() family of methods
		 CompletableFuture<Void> runAfter = comb1.thenAcceptBoth(comb2, CompletableFutureDefinitiveGuide::calculateSum);
		 runAfter.thenAcceptAsync((result) -> {logger.info("Result of running afetr both: {}", result);},executor);
		 
		 //Waiting for first CompletableFuture to complete - acceptEither/runAfterEither, 
		 
		 //Transforming first completed - applyToEither
		 
		 //Combining multiple CompletableFuture together - allOf/anyOf
		 
		 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static CompletableFuture<String> ask() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		return future;
	}
	
	public static Integer calculateSum(Integer i1, Integer i2) {		
		return i1 + i2;
	}
	
	public static void runAfterBoth(Integer i1, Integer i2) {		
		logger.info("Both are finished");
	}
	
	
	
	
}


