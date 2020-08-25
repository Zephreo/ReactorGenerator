package com.zephreo.reactorgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.zephreo.reactorgen.RL.Action;
import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.material.Block.BlockType;

public class Generator {
	
	public Location size;

	public static class RandomGenerator implements Runnable {
	     private volatile Reactor bestReactor = null;
	     private volatile int progress = 0;
	     Generator generator;
	
	     @Override
	     public void run() {
	 		float bestScore = -9999;
	 		
	 		for(int j = 0; j < ReactorGenerator.ITERATIONS; j++) {
	 			Reactor reactor = new Reactor(generator);
	 			reactor.addRandomCells();
	 			reactor.addRandomBlocks(BlockType.MODERATOR.toBlock());
	 			reactor.addRandomCoolers(ReactorGenerator.SIZE.count() * 4);
	 			float score = reactor.evaluate(ReactorGenerator.TARGET_HEAT).score;
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
	    	 return (float) progress / ReactorGenerator.ITERATIONS;
	     }
	 }

	public Generator(Location size) {
		this.size = size;
	}

	public Reactor generateRandom() throws InterruptedException, IOException {
		
		HashMap<Thread, RandomGenerator> threads = new HashMap<Thread, RandomGenerator>();
		Reactor bestReactor = null;
		float bestScore = -9999;
		
		for(int i = 0; i < ReactorGenerator.THREAD_COUNT; i++) {
			RandomGenerator gen = new RandomGenerator();
			Thread thread = new Thread(gen);
			thread.start();
			threads.put(thread, gen);
		}
		
		float overallProgress = 0;
		while(overallProgress < 0.99) {
			Thread.sleep(ReactorGenerator.REFRESH_RATE);
			overallProgress = 0;
			for(Thread thread : threads.keySet()) {
				overallProgress += threads.get(thread).getProgress() / threads.size();
			}
			Util.prl(Util.round(overallProgress * 100, 1) + "%");
		}
		
		for(Thread thread : threads.keySet()) {
			thread.join();
			Reactor reactor = threads.get(thread).getValue();
			float score = reactor.evaluate(ReactorGenerator.TARGET_HEAT).score;
			if(score > bestScore) {
				bestScore = score;
				bestReactor = reactor;
			}
		}
		
		return bestReactor;
	}

}
