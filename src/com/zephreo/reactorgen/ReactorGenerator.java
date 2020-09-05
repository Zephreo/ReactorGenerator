/**
 * 
 */
package com.zephreo.reactorgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.zephreo.reactorgen.RL.ReactorOptimiser;
import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

/**
 * @author Zephreo
 *
 */
public class ReactorGenerator {
	
	static int TARGET_HEAT;
	static int THREAD_COUNT;
	static int RANDOM_ITERATIONS;
	static int OPTIMISATION_ITERATIONS;
	static int OPTIMISATION_DEPTH;
	
	static boolean RL_LEARNING_MODE = false;
	
	static Location SIZE;
	
	static double ACCURACY;
	
	static int REFRESH_RATE = 200;
	
	//Score multipliers
	static float MULTIPLIER_AIR;
	static float MULTIPLIER_POWER; 
	static float MULTIPLIER_HEAT;
	static float MULTIPLIER_EFFICIENCY;
	static float MULTIPLIER_SYMMETRY;
	
	//Score multipliers
	static int MAX_AIR;
	static float MIN_POWER; 
	static float MIN_HEAT;
	static float MIN_EFFICIENCY;
	static float MIN_SYMMETRY;
	
	static String INPUT_FILE;
	static String OUTPUT_FILE;
	
	public static void main(String[] args) throws Exception {
		Command.parseCommand(args);
		CoolerType.setup(Reactor.DISABLED_COOLERS);
		
		Generator generator = new Generator(SIZE);
		
		Reactor reactor;
		 
		if(INPUT_FILE == null) {
			reactor = generator.generateRandom();
		} else {
			reactor = JSON.fromJSON(INPUT_FILE);
		}
		
		reactor.print(TARGET_HEAT);
		
		Reactor old = reactor.clone();
		
		String strRLTable = null;
		try {
			strRLTable = Files.readAllLines(Paths.get("./RLTable.txt")).get(0);
			RL.read(strRLTable);
		} catch(Exception e) {
			
		}
		
		float progress = 0;
		ReactorOptimiser optimiser = new ReactorOptimiser(reactor);
		Thread thread = new Thread(optimiser);
		thread.start();
		while(progress < 1) {
			Thread.sleep(REFRESH_RATE);
			progress = optimiser.getProgress();
			Util.prl(Util.round(progress, 2) + "%");
		}
		thread.join();
		
		reactor = optimiser.getResult();
		
		old.print(TARGET_HEAT);
		reactor.print(TARGET_HEAT);
		
	    //write("./unoptimized.json", JSON.toJson(old));
	    write(OUTPUT_FILE, JSON.toJson(reactor));
	    
	    write("./RLTable.txt", RL.RLTable());
	}

	static void write(String filePath, Object data) throws IOException {
		Path path = Paths.get(filePath);
	    byte[] strToBytes = data.toString().getBytes();
	    Files.write(path, strToBytes);
	}
}


