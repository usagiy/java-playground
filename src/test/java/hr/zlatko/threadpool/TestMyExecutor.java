package hr.zlatko.threadpool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.zlatko.app.PlayingWithLambda;

public class TestMyExecutor {
	
	private final static Logger logger = LoggerFactory.getLogger(PlayingWithLambda.class);
	
	//ovo ne sljaka - test zavrsi prije nego threadovi 
	/*
	@Test
    public void basicTest() {
		
	}
	*/
	
	public static void main(String[] args) {
    
    	MyExecutor myExecutor = new MyExecutor(2);
    	
    	class MySupplier implements Supplier<String>{

    		private final Logger logger = LoggerFactory.getLogger(MySupplier.class);
    		private final String s;
    		MySupplier(String s){
				this.s = s;
			}
			@Override
			public String get() {
				logger.info("Starting Supplier {}....",s);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Ending Supplier {}....",s);
				return s;
			}
    		
    		/*
    		@Override
			public void run() {
    			 logger.info("Starting Task {}....",i);
                 try {
                     Thread.sleep(2000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 logger.info("Ending Task {}....",i);				
			}
    		*/
    	}
    	
    	for (int i=0;i<4;i++){
    		CompletableFuture.supplyAsync(new MySupplier("s"+Integer.toString(i)), myExecutor).
    		thenAcceptAsync(s -> logger.info("Finished future: {}", s), myExecutor);
    	}
    	
    	
    	
    }
    
}
