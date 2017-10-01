package hr.zlatko.app;

import java.util.concurrent.ExecutionException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
{
    	//https://dzone.com/articles/implementing-java-8
    	//Test1.run();
		
    	//https://examples.javacodegeeks.com/core-java/util/concurrent/java-8-concurrency-tutorial/
		//Test2.run();
		/*
    	try {
			CombiningCompletableFutures.run();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    	
    	//http://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html
    	/*
    	try {
			CompletableFutureDefinitiveGuide.run();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    	
    	//http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/Lambda-QuickStart/index.html#overview
    	//PlayingWithLambda.run();
		
		//http://www.tutorialspoint.com/java8/index.htm
		//PlayingWithJava8.run();
    	
    	//http://doc.akka.io/docs/akka/2.4.7/java/stream/index.html
    	//PlayingWithAkkaStreams.run();
    	
    	PlayingWithAlpakkaConnectors.run();
		
	}
}

