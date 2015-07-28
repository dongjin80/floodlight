package net.floodlightcontroller.core.coap.util;

import java.lang.Thread.State;
import java.util.HashMap;

/**
 * Monitor status of the threads created for the COAP server.
 * 
 * @author "Ashish Patro"
 *
 */
public class ThreadMonitor implements Runnable {

	HashMap<Thread, String> threadApMap = new HashMap<Thread, String>();
	
	@Override
	public void run() {
		while (true) {
			try {
				System.out.println("Starting thread monitor loop");
				Thread.sleep(5000);
				
				synchronized (threadApMap) {
					for (Thread thread: threadApMap.keySet()) {
						Thread.State state = thread.getState();
						if (state == State.TERMINATED) {
							System.out.println(thread + " for thread " + threadApMap.get(thread));
							threadApMap.remove(thread);
							break;
						}
					}
				}
			} catch (InterruptedException e) {
				System.err.println(e.getMessage() + " " + Thread.currentThread().getId());
				return;
			}
		}
	}

	public synchronized void addThreadToMonitor(Thread thread, String str) {
		threadApMap.put(thread, str);
	}
}
