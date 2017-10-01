package hr.zlatko.app;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * this class shows briefly how to combina completables
 * 
 * @author dgutierrez-diez
 */


/*
Joining
It is possible to join different CompletableFuture and use its results in future calculations using the methods
thenApply() and thenCompose():
*/

public class CombiningCompletableFutures
{

    public static void run() throws InterruptedException, ExecutionException
    {
        CompletableFuture<String> completableFutureBigCompute = CompletableFuture.supplyAsync( ( ) -> {
            // big computation
                return "10";
            } );

        // we can use thenCompose and thenApply to wait until a completable is ready and start with
        // the next one using the value of the first one as parameter

        CompletableFuture<Double> thenCompose = completableFutureBigCompute
                .thenCompose( CombiningCompletableFutures::continueWithVeryImportantThing );

        CompletableFuture<CompletableFuture<Double>> thenApply = completableFutureBigCompute
                .thenApply( CombiningCompletableFutures::continueWithSomethingElse );

        System.out.println( "thenCompose " + thenCompose.get() );

        System.out.println( "thenApply " + thenApply.get() ); // is already completed so the value
                                                              // was used by the compose method

        System.out.println( "thenApply " + thenApply.isDone() ); // is already completed

        CompletableFuture<Double> thenCompose2 = completableFutureBigCompute
                .thenCompose( CombiningCompletableFutures::continueWithVeryImportantThing );

        // difference between compose and apply
        System.out.println( "thenCompose2 " + thenCompose2.get() ); // then compose uses the value
                                                                    // of the source completable,
                                                                    // this is the main difference

        // thenCombine offers the possibility to combine two completables that are totally
        // independent
        String login = "dani", password = "pass", land = "spain";
        CompletableFuture<Boolean> loginCompletable = checkLogin( login, password );
        CompletableFuture<Boolean> checkLandCompletable = checkLand( land );
        CompletableFuture<String> welcomeOrNot = loginCompletable.thenCombine( checkLandCompletable,
                                                                               ( cust, shop ) -> welcome( cust, shop ) );

        System.out.println( welcomeOrNot.get() );
    }

    private static String welcome( Boolean login, Boolean land )
    {

        // checks both and returns
        if( login && land )
            return "welcome";
        else
            return "not welcome";

    }

    private static CompletableFuture<Boolean> checkLand( String land )
    {
        // only Spanish are allowed
        return CompletableFuture.supplyAsync( ( ) -> {
            // big task with back end dependencies
                return "spain".equals( land );
            } );
    }

    private static CompletableFuture<Boolean> checkLogin( String login, String password )
    {
        return CompletableFuture.supplyAsync( ( ) -> {
            // very hard authentication process
                return login != null && password != null;
            } );
    }

    private static CompletableFuture<Double> continueWithSomethingElse( String str )
    {
        if( "10".equals( str ) )
            return CompletableFuture.supplyAsync( ( ) -> {
                System.out.println( "str passed is 10" );
                return 22.4;
            } );
        return CompletableFuture.supplyAsync( ( ) -> {
            System.out.println( "str passed is not 10" );
            return 11.4;
        } );
    }

    private static CompletableFuture<Double> continueWithVeryImportantThing( String str )
    {
        if( "10".equals( str ) )
            return CompletableFuture.supplyAsync( ( ) -> {
                System.out.println( "str passed is 10 in a very important task" );
                return 22.5;
            } );
        return CompletableFuture.supplyAsync( ( ) -> {
            System.out.println( "str passed is not 10 in a very important task" );
            return 11.5;
        } );
    }
}