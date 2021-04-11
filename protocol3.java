import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class cop4520_hw4 {
	
	public static void main(String[] args) throws InterruptedException {
		
		int i;
		int servants = 4;
		int totalPresents = 500000;
		long starttime, endtime, executiontime;	 
		long beforeUsedMem, afterUsedMem,actualMemUsed;
		
		List<Integer> gifts;	
		int[] st = IntStream.rangeClosed(1, totalPresents).toArray();
		gifts = Arrays.stream(st).boxed().collect(Collectors.toList());
		Collections.shuffle(gifts);
		
		AtomicBoolean finished = new AtomicBoolean(false); /* used for finishing when counter is done*/
		
		starttime = System.currentTimeMillis();
		beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		
		GiftList list = new GiftList(finished);
		GiftQueue giftBag = new GiftQueue(totalPresents, gifts);
		giftBag.addAll();
		
		Thread[] thread = new Thread[servants];
		
		
		for(i = 0; i < servants; i++) {
			thread[i] = new Thread(new Servant(list, giftBag, finished, totalPresents), "Servant " + i);
		}
		
		for(i = 0; i < servants; i++) {
			thread[i].start();
		}
		
		for(i = 0; i < servants; i++) {
			thread[i].join();
		}	
	
		if(finished.get() == true) {
			System.out.println("\n**THANKS FOR ALL PRESENTS**");
		}
		
		afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		endtime = System.currentTimeMillis();
		
		executiontime = endtime - starttime;
		actualMemUsed=afterUsedMem-beforeUsedMem;
		
		System.out.println("\n___________________________________________");
		System.out.println("Exetution time: " + (int)(executiontime)/1000 + "s");
		System.out.println("Memory usage: " + actualMemUsed);
		System.out.println("___________________________________________");
		
	}

}

class GiftQueue {
	int giftNum;
	Lock lock;
	List<Integer> giftBag;
	Queue<Integer> qgift;
	
	public GiftQueue(int giftNum, List<Integer> giftBag) {
		this.giftNum = giftNum;
		this.giftBag = giftBag;
		qgift = new LinkedList<Integer>();
		lock = new ReentrantLock();
	}
	
	// from list to lock based queue
	public void addAll() {
		for(int i = 0; i < giftNum; i++) {
			int e = (int)giftBag.remove(0);
			qgift.add(e);
		}
	}
	
	// enq() operation
	public void enq(int item) {
		lock.lock();
		
		try {		
			 if (!qgift.contains(item)) 
				 qgift.add(item);
				 
		}finally {
			lock.unlock();
		}
	}
	
	// deq() operation
	public int deq() {
		lock.lock();
		
		try {
		     return qgift.poll();
		      
		}finally {
			lock.unlock();
		}
	}
	 
	// size() operation
	public int size() {
		return qgift.size();
    }
	
	// isEmpty() operation
	public boolean isEmpty() {
		return (qgift.size() == 0) ? true : false;
    }
	
	// contains() operation
	public Boolean contains(int item) {
		lock.lock();

		try {
		    return qgift.contains(item);
								
		}finally {
			lock.unlock();
		}
	}
	
	public String display() {
		return qgift.toString();
	}
}

//Guest class which used for going into room
class Servant implements Runnable {
	
	AtomicBoolean finished;
	private AtomicInteger guestNum;
	private GiftList list;
	private GiftQueue giftBag;
	
	public Servant(GiftList list, GiftQueue giftBag, AtomicBoolean finished, int guestNum) {
		this.list = list;
		this.giftBag = giftBag;
		this.guestNum = new AtomicInteger(guestNum);
		this.finished = finished;
	}
	
	public void run() {
		while(!finished.get()) {						
			try {
				list.operation(list, giftBag, guestNum);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


class Node {
	
	int tag;
	Node next;
	
	public Node(int tag) {
		this.tag = tag;
		this.next = null;
	}
}


class GiftList {
	
	int num = 0;
	Node head;
	Random rand = new Random(); 
	Lock lock = new ReentrantLock();
	AtomicInteger action = new AtomicInteger();
	public AtomicInteger counter = new AtomicInteger(0);
	public AtomicInteger size = new AtomicInteger(0);
	public AtomicBoolean finished = new AtomicBoolean(false);
	String servant = new String(Thread.currentThread().getName());
	
	public GiftList(AtomicBoolean finished) {
		head = new Node(Integer.MIN_VALUE);
		head.next = new Node(Integer.MAX_VALUE);
		this.finished = finished;
	}
	
	public void add(int item) {
		
		servant = new String(Thread.currentThread().getName());
		
		lock.lock();
		try {
			if(isEmpty()) {
				Node newNode = new Node(item);
				head = newNode;
				head.next = null;
				System.out.printf("%s - Tag%d: Present is added.\n", servant, item);	
				size.incrementAndGet();
			}
			else {
				Node pred = head;
							
				while(pred.next != null && pred.next.tag < item) {
					pred = pred.next;	
				}
				
				Node newNode = new Node(item);
				newNode.next = pred.next;
				pred.next = newNode;
					
					
				size.incrementAndGet();
				System.out.printf("%s - Tag %d: Present is added.\n", servant, item);	
			}
			
			
		}finally {
			lock.unlock();
		}
	}
	
	
	public void remove() {

		servant = new String(Thread.currentThread().getName());
		
		lock.lock();
		
		try {			
			if(!isEmpty()) {
				
				Node pred = head;					
				
				if(pred.next != null) 
				{
					if(pred.tag != Integer.MIN_VALUE && pred.tag != Integer.MAX_VALUE) 
						System.out.printf("%s - THANK YOU FOR PRESENT %d.\n", servant, pred.tag);
					head = pred.next;
				}
				else {
					if(pred.tag != Integer.MIN_VALUE && pred.tag != Integer.MAX_VALUE) 
						System.out.printf("%s - THANK YOU FOR PRESENT %d.\n", servant, pred.tag);
					
					head = null;
					
				}
				
				counter.getAndIncrement();					
			}
		} finally {
			lock.unlock();
		}
		
	}
	
	
	public void contains(int item) {

		lock.lock();
		
		try {			
			servant = new String(Thread.currentThread().getName());
			
			if(isEmpty()) {	
				System.out.printf("%s - Gift %d is not presented in the list.\n", servant, item);
			}				
			else {
				Node pred = head;
				
				if(pred.tag == item)
					System.out.printf("%s - Gift %d is presented in the list.\n", servant, item);
				else {
					while(pred.next != null && pred.next.tag != item) {
						pred = pred.next;	
					}
					
					if(pred.tag == item)
						System.out.printf("%s - Gift %d is presented in the list.\n", servant, item);
					else
						System.out.printf("%s - Gift %d is not presented in the list.\n", servant, item);		
				}
			}
		} finally {
			lock.unlock();
		}
		
	}

	
	public boolean isEmpty() {
		
		lock.lock();
		
		try {
			return (head == null) ? true : false; 
			
		} finally {
			lock.unlock();
		}
		
	}
		
	
	public void operation(GiftList list, GiftQueue giftBag, AtomicInteger guest) throws InterruptedException {

		int giftTag;

		action.set(rand.nextInt(10));
		
		if(!giftBag.isEmpty() && action.get() < 4)
		{	
			giftTag = giftBag.deq();
			list.add(giftTag);
		}
		else if(!list.isEmpty() && action.get() < 8) {
			list.remove();
		}
		else
		{
			if(!list.isEmpty() && action.get() < 10) { 
				list.contains(rand.nextInt(500000));
			}
		}
				
		if(counter.get() == guest.get()) 
			finished.set(true);
	}
}
