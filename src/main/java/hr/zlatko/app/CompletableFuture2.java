package hr.zlatko.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFuture2 {

	public static void run(){
		
		System.out.println("----- TEST2 ----");		
		
		
		
		//Dobijanje vrijednosti iz future-a
		CompletableFuture completableFuture = CompletableFuture.supplyAsync( ( ) -> {
			try {
				Thread.sleep(4000L);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return "100";
		} );
		
		try {
			//wait forever
			//System.out.println( "get  " + completableFuture.get() );
			//System.out.println( "get in 3 seconds " + completableFuture.get( 3, TimeUnit.SECONDS ) );
			System.out.println( "get now " + completableFuture.getNow( "fallback vrijednost" ));
			System.out.println( "get in 5 seconds " + completableFuture.get( 5, TimeUnit.SECONDS ) );
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//We create a completable that is not going to end (it calculates, computes…for ever):
		CompletableFuture completableFutureToBeCompleted2 = CompletableFuture.supplyAsync( ( ) -> {
			for( int i = 0; i < 10; i-- )
			{
				System.out.println( "i " + i );
			}
			return 10;
		} );
		
		
		//we create a new CompletableFuture that is going to complete the first one:
		CompletableFuture completor = CompletableFuture.supplyAsync( ( ) -> {
			System.out.println( "completing the other" );
			completableFutureToBeCompleted2.complete( 222 );
			return 10;
		} );
		
		
		try {
			
			System.out.println( completor.get() );
			//without completer we would waiting forever
			System.out.println( "get  " + completableFutureToBeCompleted2.get() );
			
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		//exception handling
		CompletableFuture completableFutureHandleOk = CompletableFuture.supplyAsync( ( ) -> {
			// big task
				return 10 / 0; // exception division by zero
				// return 10 / 2;
			} );

			CompletableFuture handleOkError = completableFutureHandleOk.handle( ( ok, ex ) -> {
			if( ok != null )
			{
				// return the value if everything ok
				return ok;
			}
			else
			{
				// in case of an exception print the stack trace and return null
				//ex.printStackTrace();
				return null;
			}});
			
			try {
				System.out.println( "ok or error ? " + handleOkError.get() );
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
		
		
		
	}	
	
	
}
