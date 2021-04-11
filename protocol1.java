import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class assignment2 {
	
	public static void main(String[] args) throws InterruptedException {
		
		int i;
		int guestNum;
		
		AtomicInteger counter = new AtomicInteger(0); /*used for counting each individual visits*/
		AtomicBoolean finished = new AtomicBoolean(false); /* used for finishing when counter is done*/
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter number of guest: ");
		guestNum = input.nextInt();
		
		LabyrinthQueue labqueue = new LabyrinthQueue(counter, finished);  /*queue for visiting labyrinth*/
		
		Thread thread[] = new Thread[guestNum];
		
		for(i = 0; i < guestNum; i++) {
			thread[i] = new Thread(new Guests(labqueue, finished, guestNum), "Guest " + i);
			thread[i].start();
		}
		
		for(i = 0; i < guestNum; i++) {
			thread[i].join();
		}
	
		input.close();
	}

}

class LabyrinthQueue {
	
	private int num = 0;
	private final Lock lock = new ReentrantLock();
	public AtomicInteger counter = new AtomicInteger();
	ArrayList<String> visited = new ArrayList<String>();
	public static AtomicBoolean cupcake = new AtomicBoolean(true);
	public AtomicBoolean finished = new AtomicBoolean(false);
	
	public LabyrinthQueue(AtomicInteger counter, AtomicBoolean finished) {
		this.counter = new AtomicInteger(0);
		this.finished = finished;
	}
	
	public void operation(Object queue, AtomicInteger guest) {
			
		String leader = new String("Guest 0");
		String guestName = new String(Thread.currentThread().getName());

		lock.lock();
		
		try {
			
			/*If leader, then it can check the cupcake and increment the number of visits*/
			if(guestName.equals(leader)) {

					/*If cupcake is eaten, then someone made their first visit*/
					if(!cupcake.get()) {
						num = counter.incrementAndGet(); 
						counter.set(num);
						cupcake.set(true); // requests a new cake
						
						System.out.println("Leader: There are " + counter.get() + " number of guests visits the labryinth");
												
					}else 
						System.out.println("Leader: There are " + counter.get() + " number of guests visits the labryinth");
				
					// If counter is equal to total guest number, leader can finish the program and make announcement 
					if(num == guest.get() - 1) 
					{
						finished.set(true);
						System.out.println("Leader's Announcement: All guests are visited the labyrinth");
					}
					
			} 
			else {	
					/*If not leader, then it can eat the cake if its their first visit when there is cake*/
					if(cupcake.get() && !visited.contains(guestName)) {
						
						System.out.printf("%s: Eat the cake\n", guestName);
						
						visited.add(guestName); // program can add their name to visited, so for next time, they won't eat the cake 
						cupcake.set(false);			
					}
			}

		} 
		finally {
			System.out.printf("%s: Left the labyrinth\n", guestName);
			lock.unlock();
		}
	
	}
	
}



//Guest class which used for going into labyrinth
class Guests implements Runnable {
	
	AtomicBoolean finished;
	private AtomicInteger guest;
	private LabyrinthQueue labqueue;
	String leader = new String("Guest 0");
	
	public Guests(LabyrinthQueue labqueue, AtomicBoolean finished, int guestNum) {
		this.labqueue = labqueue;
		this.guest = new AtomicInteger(guestNum);
		this.finished = finished;
	
	}
	
	public void run() {
		// if all guests go into labyrinth and eat the cake, program can terminate
		while(!finished.get()) {
			System.out.printf("%s: Going into the labyrinth\n", Thread.currentThread().getName());
			labqueue.operation(new Object(), guest);
		} 
	}
}

