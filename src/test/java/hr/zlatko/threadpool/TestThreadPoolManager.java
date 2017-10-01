package hr.zlatko.threadpool;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.zlatko.app.PlayingWithLambda;

public class TestThreadPoolManager {
	
	private final static Logger logger = LoggerFactory.getLogger(PlayingWithLambda.class);
	
	//ovo ne sljaka 
	/*
	@Test
    public void basicTest() {
		
	}
	*/
	
	public static void main(String[] args) {
    
    	ThreadPoolManager poolManager = new ThreadPoolManager(4);
         
        /*
    	//now lets submit task
        poolManager.submitTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Task A....");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Task A Completed....");
            }
        });
         
        poolManager.submitTask(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Task B....");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Task B Completed....");
            }
        });
        */
    	
    	
    	
    	class Task implements Runnable{

    		private final Logger logger = LoggerFactory.getLogger(Task.class);
    		private final int i;
    		Task(int i){
				this.i = i;
			}
    		
    		@Override
			public void run() {
    			 logger.info("Starting Task {}....",i);
                 try {
                     Thread.sleep(2000);
                     long a =0;
                     for(int i=0;i<1000000000;i++){
                    	 a+=i;
                     }
                     logger.info("a: {}....",a);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 logger.info("Ending Task {}....",i);				
			}
    		
    	}
    	
    	for (int i=0;i<100;i++){
    		poolManager.submitTask(new Task(i));
    	}
    	
    	
    	
    }
    
}
