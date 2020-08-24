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
	
	static final int TARGET_HEAT = 360;
	static final int THREAD_COUNT = 10;
	static final int ITERATIONS = 10 * 1000;
	static final Location SIZE = new Location(3, 3, 3);
	
	static final int REFRESH_RATE = 200;
	
	static {
		Reactor.DISABLED_COOLERS.addAll(Arrays.asList(
				CoolerType.ACTIVE_CRYOTHIUM,
				CoolerType.ACTIVE_WATER,
				CoolerType.ENDERIUM));
		
		CoolerType.setup();
	}
	
	//Score multipliers
	static final int MUTLTIPLIER_AIR = -1;
	static final float MUTLTIPLIER_POWER = 1; 
	static final float MUTLTIPLIER_HEAT = 0.2f;
	static final float MUTLTIPLIER_EFFICIENCY = 5;
	static final float MUTLTIPLIER_SYMMETRY = 5;

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		HashMap<Thread, Generator> threads = new HashMap<Thread, Generator>();
		Reactor bestReactor = null;
		float bestScore = -9999;
		
		for(int i = 0; i < THREAD_COUNT; i++) {
			Generator gen = new Generator();
			Thread thread = new Thread(gen);
			thread.start();
			threads.put(thread, gen);
		}
		
		float overallProgress = 0;
		while(overallProgress < 0.99) {
			Thread.sleep(REFRESH_RATE);
			overallProgress = 0;
			for(Thread thread : threads.keySet()) {
				overallProgress += threads.get(thread).getProgress() / threads.size();
			}
			pr(round(overallProgress * 100, 1) + "%");
		}
		
		for(Thread thread : threads.keySet()) {
			thread.join();
			Reactor reactor = threads.get(thread).getValue();
			float score = reactor.evaluate(TARGET_HEAT).score;
			if(score > bestScore) {
				bestScore = score;
				bestReactor = reactor;
			}
		}
		
		bestReactor.print(TARGET_HEAT);
	}
	
	static float round(float num, int places) {
		return Math.round(num * 10 * places) / (10 * (float) places);
	}
	
	public static class Generator implements Runnable {
	     private volatile Reactor bestReactor = null;
	     private volatile int progress = 0;

	     @Override
	     public void run() {
	 		float bestScore = -9999;
	 		
	 		for(int j = 0; j < ITERATIONS; j++) {
	 			Reactor reactor = new Reactor(SIZE);
	 			reactor.addRandomCells();
	 			reactor.addRandomBlocks(BlockType.MODERATOR.toBlock());
	 			reactor.addRandomCoolers(SIZE.count() * 4);
	 			float score = reactor.evaluate(TARGET_HEAT).score;
	 			if(score > bestScore) {
	 				bestScore = score;
	 				bestReactor = reactor;
	 			}
	 			progress++;
	 		}
	     }

	     public Reactor getValue() {
	         return bestReactor;
	     }
	     
	     public float getProgress() {
	    	 return (float) progress / ITERATIONS;
	     }
	 }
	
	
	
	public static void pr(Object message) {
		System.out.print(message + "\n");
	}
}


