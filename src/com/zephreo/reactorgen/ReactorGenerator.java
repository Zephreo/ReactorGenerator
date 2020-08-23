/**
 * 
 */
package com.zephreo.reactorgen;

import java.util.Arrays;
import java.util.HashMap;

import com.zephreo.reactorgen.Block.BlockType;
import com.zephreo.reactorgen.Cooler.CoolerType;

/**
 * @author Zephreo
 *
 */
public class ReactorGenerator {
	
	static final int targetHeat = 360;
	
	static {
		Reactor.disabledCoolers.addAll(Arrays.asList(
				CoolerType.ACTIVE_CRYOTHIUM,
				CoolerType.ACTIVE_WATER,
				CoolerType.WATER,
				CoolerType.LAPIS,
				CoolerType.REDSTONE));
		
		CoolerType.setup();
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		class Generator implements Runnable {
		     private volatile Reactor bestReactor = null;
		     private volatile int progress = 0;
		     
		     final static int iterations = 1000;
		     final static int groupSize = 100;

		     @Override
		     public void run() {
		    	 Location size = new Location(3, 3, 3);
		 		
		 		float bestScore = -9999;
		 		
		 		for(int j = 0; j < iterations; j++) {
		 			for(int i = 0; i < groupSize; i++) {
		 				Reactor reactor = new Reactor(size);
		 				reactor.addRandomCells();
		 				reactor.addRandomBlocks(BlockType.MODERATOR.toBlock());
		 				reactor.addRandomCoolers(size.count() * 4);
		 				float score = reactor.evaluate(targetHeat).score;
		 				if(score > bestScore) {
		 					bestScore = score;
		 					bestReactor = reactor;
		 				}
		 			}
		 			progress++;
		 		}
		     }

		     public Reactor getValue() {
		         return bestReactor;
		     }
		     
		     public float getProgress() {
		    	 return (float) progress / iterations;
		     }
		 }
		
		HashMap<Thread, Generator> threads = new HashMap<Thread, Generator>();
		Reactor bestReactor = null;
		float bestScore = -9999;
		
		for(int i = 0; i < 10; i++) {
			Generator gen = new Generator();
			Thread thread = new Thread(gen);
			thread.start();
			threads.put(thread, gen);
		}
		
		float overallProgress = 0;
		while(overallProgress < 0.99) {
			Thread.sleep(200);
			overallProgress = 0;
			for(Thread thread : threads.keySet()) {
				overallProgress += threads.get(thread).getProgress() / threads.size();
			}
			pr(overallProgress * 100);
		}
		
		for(Thread thread : threads.keySet()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Reactor reactor = threads.get(thread).getValue();
			float score = reactor.evaluate(targetHeat).score;
			if(score > bestScore) {
				bestScore = score;
				bestReactor = reactor;
			}
		}
		
		bestReactor.print(targetHeat);
	}
	
	

	
	
	public static void pr(Object message) {
		System.out.print(message + "\n");
	}
}
