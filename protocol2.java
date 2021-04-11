import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*NOTE: TEST THE PROGRAM WITH AT LEAST 4 THREADS*/

public class assignmentpart2 {
	public static void main(String[] args) throws InterruptedException {
		int i;
		int guestNum;
		
		Random rand = new Random(); 
		AtomicInteger counter = new AtomicInteger(0);  /*used for counting each individual visits*/
		AtomicBoolean finished = new AtomicBoolean(false); /* used for finishing when counter is done*/
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter number of guest: ");
		guestNum = input.nextInt();
		
		// used to avoid infinite loop, finishes program when there is certain number of visits
		final int totalvisit = rand.nextInt(guestNum) + guestNum;
	
		LockBasedQueue queue = new LockBasedQueue(guestNum, counter, finished); /*queue for visiting room*/
		
		Thread[] thread = new Thread[guestNum];
		
		for(i = 0; i < guestNum; i++) {
			thread[i] = new Thread(new Guest(queue, finished, totalvisit), "Guest " + i);
		}
		
		for(i = 0; i < guestNum; i++) {
			thread[i].start();
		}
		
		for(i = 0; i < guestNum; i++) {
			thread[i].join();
		}
	
	
		input.close();
	
	}
	

}


// Guest class which used for going into room
class Guest implements Runnable {
	
	AtomicBoolean finished;
	private AtomicInteger guest;
	private LockBasedQueue queue;
	
	public Guest(LockBasedQueue queue, AtomicBoolean finished, int guestNum) {
		this.queue = queue;
		this.guest = new AtomicInteger(guestNum);
		this.finished = finished;
	
	}
	
	public void run() {
		// if all guests visit the room and reach certain number of visits, program can terminate
		while(!finished.get()) {
			try {
				queue.operation(new Object(), guest);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}
}


// LockBasedQueue class which let threads/guests to go in the room without overlapping with other threads
// uses lock methods to ensures sequential operation
// each thread is stored in a lock based, linked list queue.
// Duration is used to ensure threads are randomly gets into the room.
class LockBasedQueue {
		
	int num = 0;
	Lock lock;
	Queue<Thread> qguest;
	Long duration = (long) (Math.random() * 1000);
	public AtomicInteger counter = new AtomicInteger();
	public AtomicBoolean finished = new AtomicBoolean(false);
	String guestName = new String(Thread.currentThread().getName());

	
	public LockBasedQueue(int capacity, AtomicInteger counter, AtomicBoolean finished) {
		lock = new ReentrantLock();
		qguest = new LinkedList<Thread>();
		this.counter = new AtomicInteger(0);
		this.finished = finished;
	}
	
	
	// enq() operation
	public void enq(Thread t) {
		lock.lock();
		
		try {
			
			 if (!qguest.contains(t)) 
			 {
				 qguest.add(t);
				 System.out.printf("%s: in the queue for going into the room\n", t.getName());
			 }
			 
		}finally {
			lock.unlock();
		}
	}
	
	// deq() operation
	public Thread deq() {
		lock.lock();
		
		try {
			 Thread t = qguest.poll();
			 
			 System.out.printf("%s: saw the vase and left room\n", t.getName());
		      return t;
		      
		}finally {
			lock.unlock();
		}
	}
	 
	// isEmpty() operation
	public boolean isEmpty() {
        lock.lock();
		
		try {
			boolean empty = qguest.isEmpty();
	        return empty;
			
		}finally {
			lock.unlock();
		}
    }
	
	// contains() operation
	public Boolean contains(Thread t) {
		lock.lock();
		
		try {

		    boolean con = qguest.contains(t);
		    return con;
						
			
		}finally {
			lock.unlock();
		}
	}
	

	
	public void operation(Object queue, AtomicInteger guest) throws InterruptedException {
		
		guestName = new String(Thread.currentThread().getName());
	
		// Thread.sleep() will ensure that threads randomly visit the room.
		try {
            if(guestName.equalsIgnoreCase("Guest 0") || guestName.equalsIgnoreCase("Guest 2"))
            	duration /= 100;
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		enq(Thread.currentThread());
		num = counter.incrementAndGet(); 
		counter.set(num);
				
		
		if(contains(Thread.currentThread())) {
			deq();
		}
		
		System.out.printf("***************number of enters: %d\n", num);
		if(num == guest.get())
			finished.set(true);
		
	}
	
}



