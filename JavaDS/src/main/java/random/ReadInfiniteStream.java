package random;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Last K average of an infinite stream.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class ReadInfiniteStream {
    // 1,2,3,4 ......
    private AtomicInteger sum;
    private int[] consumer;
    private int counter = 0;
    private boolean onePassComplete;
    private int k;

    public ReadInfiniteStream(Queue<Integer> stream, int k) {
	consumer = new int[k];
	this.k = k;
	sum = new AtomicInteger(0);

	Thread th = new Thread(new Runnable() {
	    @Override
	    public void run() {
		System.out.println("Waiting for data..");
		while (true) {

		    if (stream.isEmpty()) {
			try {
			    Thread.sleep(1);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    } else {
			int newVal = stream.poll();
			counter++;
			if (counter == k) {
			    counter = 0;
			    onePassComplete = true;
			}
			int oldval = consumer[counter % k];
			sum.addAndGet(-oldval);
			sum.addAndGet(newVal);
			consumer[counter % k] = newVal;

		    }

		}

	    }
	});
	th.setName(k + " Average thread");
	th.setDaemon(true);
	th.start();
    }

    public double getLastKAvg() {
	if (onePassComplete)
	    return (double) sum.get() / k;
	else if (consumer.length > 0)
	    return (double) sum.get() / counter;
	else
	    return Double.NaN;

    }

    public static void main(String args[]) throws InterruptedException {
	Queue<Integer> q = new LinkedList<>();
	ReadInfiniteStream rfis = new ReadInfiniteStream(q, 3);
	for (int i = 1; i < 100; i++) {
	    q.add(i);
	    Thread.sleep(1000);
	    if (i % 2 == 0)
		System.out.println(rfis.getLastKAvg() + " " + Arrays.toString(rfis.consumer));
	}
    }

}
