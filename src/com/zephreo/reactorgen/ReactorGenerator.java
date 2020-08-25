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
import com.zephreo.reactorgen.location.QLocation;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

/**
 * @author Zephreo
 *
 */
public class ReactorGenerator {
	
	static final int TARGET_HEAT = 360;
	static final int THREAD_COUNT = 10;
	static final int ITERATIONS = 30 * 1000;
	static final int RL_ITERATIONS = 1;
	static final Location SIZE = new Location(4, 4, 4);
	
	static final int REFRESH_RATE = 200;
	
	static {
		Reactor.DISABLED_COOLERS.addAll(Arrays.asList(
				CoolerType.ACTIVE_CRYOTHIUM,
				CoolerType.ACTIVE_WATER));
		
		CoolerType.setup(Reactor.DISABLED_COOLERS);
		
	}
	
	//Score multipliers
	static final float MUTLTIPLIER_AIR = 0f;
	static final float MUTLTIPLIER_POWER = 0; 
	static final float MUTLTIPLIER_HEAT = 0f;
	static final float MUTLTIPLIER_EFFICIENCY = 0;
	static final float MUTLTIPLIER_SYMMETRY = -100;
	
	//Score multipliers
	static final int MAX_AIR = 0;
	static final float MIN_POWER = 59; 
	static final float MIN_HEAT = 0f;
	static final float MIN_EFFICIENCY = 0;
	static final float MIN_SYMMETRY = 0;
	
	static String folder = "C:\\Users\\hdent\\Desktop\\Minecraft\\Apps\\Reactor Planner\\";
	
	public static void main(String[] args) throws Exception {
		Generator generator = new Generator(SIZE);
		
		Reactor reactor = generator.generateRandom();
		 
		//Reactor reactor = JSON.fromJSON(folder, "Beast.json");
		
		reactor.print(ReactorGenerator.TARGET_HEAT);
		
		Reactor old = reactor.clone();
		
		String strRLTable = null;
		try {
			strRLTable = Files.readAllLines(Paths.get(folder, "RLTable.txt")).get(0);
			RL.read(strRLTable);
		} catch(Exception e) {
			
		}
		
		
		QLocation all = reactor.getAll(); //.collapse(0.8);
		for(int i = 0; i < RL_ITERATIONS; i++) {
			Action action = RL.RLcalc(reactor, all, ReactorGenerator.TARGET_HEAT);
			action.submit(reactor);
			Util.prl(Util.round((i / (float) RL_ITERATIONS) * 100, 1) + "%");
		}
		
		old.print(ReactorGenerator.TARGET_HEAT);
		reactor.print(ReactorGenerator.TARGET_HEAT);
		
	    write(folder, "unoptimized.json", JSON.toJson(old));
	    write(folder, "optimized.json", JSON.toJson(reactor));
	    
	    write(folder, "RLTable.txt", RL.RLTable());
	}

	static void write(String folder, String fileName, Object data) throws IOException {
		Path path = Paths.get(folder, fileName);
	    byte[] strToBytes = data.toString().getBytes();
	    Files.write(path, strToBytes);
	}
}


