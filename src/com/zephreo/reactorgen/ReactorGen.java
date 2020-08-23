/**
 * 
 */
package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.HashMap;

import com.zephreo.reactorgen.Block.BlockType;

/**
 * @author Zephreo
 *
 */
public class ReactorGen {
	
	static final int targetHeat = 150;
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		class Generator implements Runnable {
		     private volatile Reactor bestReactor = null;

		     @Override
		     public void run() {
		    	 Location size = new Location(3, 3, 3);
		 		
		 		float bestScore = -9999;
		 		
		 		for(int j = 0; j < 100; j++) {
		 			for(int i = 0; i < 1000; i++) {
		 				Reactor reactor = new Reactor(size);
		 				reactor.addRandomCells();
		 				reactor.addRandomBlocks(BlockType.MODERATOR.toBlock());
		 				reactor.addRandomCoolers(100);
		 				reactor.addRandomCells(2, 4);
		 				reactor.addRandomCoolers(100);
		 				float score = reactor.evaluate(targetHeat);
		 				if(score > bestScore) {
		 					bestScore = score;
		 					bestReactor = reactor;
		 				}
		 			}
		 			pr(j * 1000);
		 		}
		     }

		     public Reactor getValue() {
		         return bestReactor;
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
		
		for(Thread thread : threads.keySet()) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Reactor reactor = threads.get(thread).getValue();
			float score = reactor.evaluate(targetHeat);
			if(score > bestScore) {
				bestScore = score;
				bestReactor = reactor;
			}
		}
		
		bestReactor.print();
		pr("score: " + bestScore);
	}
	
	

	
	
	public static void pr(Object message) {
		System.out.print(message + "\n");
	}
}
