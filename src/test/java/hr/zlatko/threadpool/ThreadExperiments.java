package hr.zlatko.threadpool;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Experimenti inspirirani 
 * https://arashmd.blogspot.hr/2013/06/java-threading.html
 *
 */
public class ThreadExperiments {

	static Logger logger = LoggerFactory.getLogger(ThreadExperiments.class);
	public final static Object lockObject = new Object();
	
	//ExecutorService executor = Executors.newFixedThreadPool(20);
	
	public static BigInteger longRunnigTask(){
		BigInteger veryBig = new BigInteger(5000, new Random());
		veryBig = veryBig.nextProbablePrime();
    	logger.debug("veryBig:{}", veryBig);
    	return veryBig;
	}
	
	
	
	public static void threadInterruptExperiment(){
		
		logger.debug("---- THREAD INTERRUPTING ---");
		//samo u slucaju slipa dobije interrupt i budi se
		Thread t2 = new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (Exception e) {

				logger.debug("{} Thread : {} vide ne spava: {}", e.getMessage(), Thread.currentThread().getName(), Thread.currentThread().getState().name());
			}
		}, "T2");
				
		class Runnable3 implements Runnable{
			private Thread otherThread;
			public Runnable3(Thread otherThread) {
				this.otherThread = otherThread;
			}			
			@Override
			public void run() {
				try {
					logger.debug("Thread {} ce interruptat drugi thread: {}", Thread.currentThread().getName(), otherThread.getName()) ;
					TimeUnit.SECONDS.sleep(5);
					otherThread.interrupt();
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}				
			}			
		}		
		Thread t3 = new Thread (new Runnable3(t2));
		t2.start();
		t3.start();
	}
	
	
	
	
	
	
	
	/**
	 * Thread ceka drugi thread da zavrsi
	 * zuta 
	 */
	public static void threadIJoinExperiment(){
		
		Thread t1 = new Thread(() -> {try {
											TimeUnit.SECONDS.sleep(15);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}}, "T1");
		
		
		class Runnable3 implements Runnable{
			private Thread otherThread;
			public Runnable3(Thread otherThread) {
				this.otherThread = otherThread;
			}			
			@Override
			public void run() {
				try {
					
					TimeUnit.SECONDS.sleep(4);
					logger.debug("Thread {} ce cekati drugi thread: {}", Thread.currentThread().getName(), otherThread.getName()) ;
					otherThread.join();
					logger.debug("Thread {} vise ne cekam: ", Thread.currentThread().getName());
					TimeUnit.SECONDS.sleep(4);
				} catch (InterruptedException e) {					
					logger.debug("{} vise ne cekam", e.getMessage());
				}				
			}			
		}		
		Thread t2 = new Thread (new Runnable3(t1), "Cekajuci");
		t1.start();
		t2.start();
	}
	
	//thread zavrsi nakon yealda
	public static void yealdExperiment(){

		Thread yeal = new Thread(() -> {try {
			TimeUnit.SECONDS.sleep(15);
			IntStream.range(0, 100).forEach(i -> {
													logger.debug(Integer.toString(i));
													if (i > 100){
														logger.debug("yeald");
														Thread.yield();
													}
												 });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}, "yeald");
		yeal.start();
		
	}
	
		/**
		 * Monitor
		 * crvena
		 */
		public static void synchronizeExperiment() throws InterruptedException{

			Thread s1 = new Thread(() -> {try {				
				synchronized (lockObject) {
					logger.debug("s1 usao u synchronized block");
					TimeUnit.SECONDS.sleep(10);
					logger.debug("s1 iylayi synchronized block");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}, "s1");
			
			Thread s2 = new Thread(() -> {try {				
				synchronized (lockObject) {
					logger.debug("s2 usao u synchronized block");
					TimeUnit.SECONDS.sleep(10);
					logger.debug("s2 iylayi synchronized block");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}, "s2");

			
			
			
			s1.start();
			s2.start();
			Thread.sleep(5000);
			synchronized (lockObject) {
				logger.debug("main usao u synchronized block");
				TimeUnit.SECONDS.sleep(10);
				logger.debug("main iylayi synchronized block");
			}

			
		}
		
		public static void synchronizeExperiment2() throws InterruptedException{
			
			
			Thread t1 = new Thread(() -> {try {				
				IntStream.range(0, 100).forEach(i -> {
														try {
															logger.debug(Integer.toString(i));
															synchronized (lockObject) {
																lockObject.wait();
															}
															TimeUnit.SECONDS.sleep(5);
															synchronized (lockObject) {
																lockObject.notify();
															}
														} catch (Exception e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													 });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}, "t1");
			
			Thread t2 = new Thread(() -> {try {				
				IntStream.range(0, 100).forEach(i -> {
														try {
															logger.debug(Integer.toString(i));
															synchronized (lockObject) {
																lockObject.wait();
															}
															TimeUnit.SECONDS.sleep(5);
															synchronized (lockObject) {
																lockObject.notify();
															}
														} catch (Exception e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													 });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}, "t2");
			
			t1.start();
			
			synchronized (lockObject) {
				logger.debug("main usao u synchronized block");
				TimeUnit.SECONDS.sleep(10);
				lockObject.notify();
				logger.debug("main iylayi synchronized block");
			}
			t2.start();
			
		}
		
		
		
	public static void threadCallback() throws InterruptedException{
		
		
		class MainWork {
			
			private String s = "Inicijalna vrijednost";
			
			public void start() throws InterruptedException{
				while(true){
					TimeUnit.SECONDS.sleep(2);
					logger.debug("Radu u threadu {} s varijablom {}", Thread.currentThread().getName(), s);
				}
			}
			
			public void callback(){
				logger.debug("Calling callback in thread: {}", Thread.currentThread().getName());
				s = "Nova vrijednost";
			}
		}
		
		
		class MyRunnable implements Runnable {
			
			MainWork work;
			public MyRunnable(MainWork work) {
				this.work = work ;
			}
			
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(10);
					logger.debug("Calling callback in thread: {}", Thread.currentThread().getName());
					work.callback();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
		
		MainWork m = new MainWork();
		
		
		//MainWork mainWork = new MainWork();
		//MyRunnable myRunnable = 
		
		Thread workingThread = new Thread(new MyRunnable(m));
		workingThread.start();
		m.start();
		
		
	}
		
		
		
	
	
	
	
	
	
	public static void main(String[] args) throws InterruptedException {
	 	
		
		
		//longRunnigTask();
		//t1.start();
		/*
	 	Thread current = Thread.currentThread();
	 	logger.debug("Current thread: {} state: {}", current.getName(), current.getState().name());
		Thread t1 = new Thread(() -> {
			longRunnigTask();
		}, 
		"zlatko");
		t1.start();
		t1.sleep(10000);		
		logger.debug("Thread: {} state: {}", t1.getName(), t1.getState().name());
		*/
		//send interaupt signal to thread
		
		//1 threda interrupta drugi --------------------------------------------- 
		//threadInterruptExperiment();
		//threadIJoinExperiment();
		//yealdExperiment();
		//synchronizeExperiment();
		synchronizeExperiment2();
		 //threadCallback();
				
	}
	
	 
		
}
