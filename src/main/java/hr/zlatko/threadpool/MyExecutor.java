package hr.zlatko.threadpool;

import java.util.concurrent.Executor;

public class MyExecutor implements Executor {
	
	private ThreadPoolManager poolManager;
	
	public MyExecutor(int capacity) {
		poolManager = new ThreadPoolManager(capacity);
	}
	
	@Override
	public void execute(Runnable command) {		
		poolManager.submitTask(command);		
	}

}
