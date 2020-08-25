/**
 * 
 */
package com.zephreo.reactorgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zephreo.reactorgen.Block.BlockType;
import com.zephreo.reactorgen.Cooler.CoolerType;
import com.zephreo.reactorgen.RL.Action;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Zephreo
 *
 */
public class ReactorGenerator {
	
	static final int TARGET_HEAT = 360;
	static final int THREAD_COUNT = 10;
	static final int ITERATIONS = 1 * 1000;
	static final Location SIZE = new Location(4, 4, 4);
	
	static final int REFRESH_RATE = 200;
	
	static {
		Reactor.DISABLED_COOLERS.addAll(Arrays.asList(
				CoolerType.ACTIVE_CRYOTHIUM,
				CoolerType.ACTIVE_WATER));
		
		CoolerType.setup(Reactor.DISABLED_COOLERS);
		
		//Setup reactor defaults
		Reactor.size = SIZE;
		for(int x = 0; x < SIZE.x; x++) {
			for(int y = 0; y < SIZE.y; y++) {
				for(int z = 0; z < SIZE.z; z++) {
					Location loc = new Location(x, y, z);
					Reactor.empty.put(loc, BlockType.AIR.toBlock());
				}
			}
		}
	}
	
	//Score multipliers
	static final int MUTLTIPLIER_AIR = -1;
	static final float MUTLTIPLIER_POWER = 1; 
	static final float MUTLTIPLIER_HEAT = 0.2f;
	static final float MUTLTIPLIER_EFFICIENCY = 5;
	static final float MUTLTIPLIER_SYMMETRY = 5;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		
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
		
		Reactor old = bestReactor.clone();
		
		String folder = "C:\\Users\\hdent\\Desktop\\Minecraft\\Apps\\Reactor Planner\\";
		
		String strRLTable = null;
		try {
			strRLTable = Files.readAllLines(Paths.get(folder, "RLTable.txt")).get(0);
			RL.read(strRLTable);
		} catch(Exception e) {
			
		}
		
		int RLiterations = 5000;
		QLocation all = bestReactor.getAll(); //.collapse(0.8);
		for(int i = 0; i < RLiterations; i++) {
			Action action = RL.RLcalc(bestReactor, all, TARGET_HEAT);
			action.submit(bestReactor);
			pr(round((i / (float) RLiterations) * 100, 1) + "%");
		}
		
		old.print(TARGET_HEAT);
		bestReactor.print(TARGET_HEAT);
		
		pr(RL.cache + " - " + RL.calc);
		
	    write(folder, "unoptimized.json", toJson(old));
	    write(folder, "optimized.json", toJson(bestReactor));
	    
	    write(folder, "RLTable.txt", RL.RLTable());
	}
	
	static void write(String folder, String fileName, Object data) throws IOException {
		Path path = Paths.get(folder, fileName);
	    byte[] strToBytes = data.toString().getBytes();
	    Files.write(path, strToBytes);
	}
	
	static float round(float num, int places) {
		return Math.round(num * 10 * places) / (10 * (float) places);
	}
	
	public static class Generator implements Runnable {
	     private volatile Reactor bestReactor = null;
	     private volatile int progress = 0;
	     ReactorGenerator generator;

	     @Override
	     public void run() {
	 		float bestScore = -9999;
	 		
	 		for(int j = 0; j < ITERATIONS; j++) {
	 			Reactor reactor = new Reactor();
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
	
	@SuppressWarnings("unchecked")
	public static JSONObject toJson(Reactor reactor) {
		JSONObject out = new JSONObject();
		
		JSONObject saveVersion = new JSONObject();
		saveVersion.put("Major", 1);
		saveVersion.put("Minor", 2);
		saveVersion.put("Build", 18);
		saveVersion.put("Revision", 0);
		saveVersion.put("MajorRevision", 0);
		saveVersion.put("MinorRevision", 0);
		
		out.put("SaveVersion", saveVersion);
		
		JSONArray blocks = new JSONArray();
		for(CoolerType type : CoolerType.values()) {
			blockToJSON(blocks, reactor, type.toBlock());
		}
		blockToJSON(blocks, reactor, BlockType.FUEL_CELL.toBlock());
		blockToJSON(blocks, reactor, BlockType.MODERATOR.toBlock());
		
		out.put("CompressedReactor", blocks);
		 
		out.put("InteriorDimensions", Reactor.size.toString(true));
		
		JSONObject fuel = new JSONObject();
		fuel.put("Name", "LEU-235 Oxide");
		fuel.put("BasePower", 168);
		fuel.put("BaseHeat", 62.5);
		fuel.put("FuelTime", 72000);
		
		out.put("UsedFuel", fuel);
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	static void blockToJSON(JSONArray blocks, Reactor reactor, Block block) {
		int count = 0;
		
		JSONObject blockName = new JSONObject();
		JSONArray coolerArray = new JSONArray();
		for(Location loc : reactor.getBlock(block).posbLocations.keySet()) {
			coolerArray.add(loc.toString());
			count++;
		}
		
		if(count == 0) {
			return;
		}
		
		blockName.put(block.toString(), coolerArray);
		blocks.add(blockName);
	}
	
	public static void pr(Object message) {
		System.out.print(message + "\n");
	}
	
	/**
	 * Whole string is lowercase except those after spaces or underscores (including first character)
	 * 
	 * @param input
	 * @return
	 */
	public static String toTitleCase(String input) {
		String out = "";
		for(String str : split(input, " |_")) {
			out += str.toString().substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
		}
		return out;
	}
	
	/**
     * Splits a String according to a regex, keeping the splitter at the end of each substring
     * 
     * @param input The input String
     * @param regex The regular expression upon which to split the input
     * @return An array of Strings
     */
	public static String[] split(String input, String regex) {
        ArrayList<String> res = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        int pos = 0;
        while (m.find()) {
            res.add(input.substring(pos, m.end()));
            pos = m.end();
        }
        if(pos < input.length()) {
        	res.add(input.substring(pos));
        }
        return res.toArray(new String[res.size()]);
    }
}


