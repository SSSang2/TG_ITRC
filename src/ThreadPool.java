import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ThreadPool {
	private static ThreadPool threadPool= null;
	private static ExecutorService threadService =null;
	private static HashMap<String, Future> listFuture = null;
	private static HashMap<String, Runnable> listRunnable = null;
	
	private ThreadPool(){};
	
	public static ThreadPool getInstance(){
		if(threadPool == null){
			threadPool = new ThreadPool();
			threadService = Executors.newCachedThreadPool();
		}
		
		return threadPool;
	}
	
	public void runWorker(String id, Runnable task){
		if(listFuture == null){
			listFuture = new HashMap<String, Future>();
			listRunnable = new HashMap<String, Runnable>();
		}
		listFuture.put(id, threadService.submit(task));
		listRunnable.put(id, task);
	}
	
	public Runnable getThread(String id){
		return listRunnable.get(id);
	}
	
	public SCListener getWorker(String id) throws InterruptedException, ExecutionException{
		return (SCListener) listFuture.get(id).get();
	}
	
	public void stopWorkerFuture(String id){
		Future temp = listFuture.get(id);
		if(!temp.isCancelled() && temp.cancel(true)){
			listFuture.remove(id);
		} else {
			System.out.println("Thread " + id + "remove failed...");
		}
	}
	
	public void stopThreadpool()
	{
		threadService.shutdown();
	}
	

}

