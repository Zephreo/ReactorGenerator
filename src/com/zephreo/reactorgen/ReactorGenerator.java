/**
 * 
 */
package com.zephreo.reactorgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import com.zephreo.reactorgen.RL.Action;
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
	
	static boolean RL_LEARNING_MODE = true;
	
	static Location SIZE = new Location(3, 3, 3);
	
	static double ACCURACY;
	
	static int REFRESH_RATE = 200;
	
	static {
		Reactor.DISABLED_COOLERS.addAll(Arrays.asList(
				CoolerType.ACTIVE_CRYOTHIUM,
				CoolerType.ACTIVE_WATER,
				CoolerType.ENDERIUM,
				CoolerType.CRYOTHEUM));
		CoolerType.setup(Reactor.DISABLED_COOLERS);
	}
	
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
			strRLTable = Files.readAllLines(Paths.get("./Builds/", "RLTable.txt")).get(0);
			RL.read(strRLTable);
		} catch(Exception e) {
			
		}
		
		for(int i = 0; i < OPTIMISATION_ITERATIONS; i++) {
			Action action = RL.RLcalc(reactor, TARGET_HEAT, ACCURACY, OPTIMISATION_DEPTH);
			action.submit(reactor);
			Util.prl(Util.round((i / (float) OPTIMISATION_ITERATIONS) * 100, 1) + "%");
		}
		
		old.print(TARGET_HEAT);
		reactor.print(TARGET_HEAT);
		
	    write("./Builds/unoptimized.json", JSON.toJson(old));
	    write(OUTPUT_FILE, JSON.toJson(reactor));
	    
	    write("./Builds/RLTable.txt", RL.RLTable());
	}

	static void write(String filePath, Object data) throws IOException {
		Path path = Paths.get(filePath);
	    byte[] strToBytes = data.toString().getBytes();
	    Files.write(path, strToBytes);
	}
}


