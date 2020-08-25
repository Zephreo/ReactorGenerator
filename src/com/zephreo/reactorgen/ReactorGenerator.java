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
import com.zephreo.reactorgen.material.Block.BlockType;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

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
	
	public static void main(String[] args) throws InterruptedException, IOException {
		Generator generator = new Generator(SIZE);
		
		Reactor reactor = generator.generateRandom();
		
		reactor.print(ReactorGenerator.TARGET_HEAT);
		
		Reactor old = reactor.clone();
		
		String folder = "C:\\Users\\hdent\\Desktop\\Minecraft\\Apps\\Reactor Planner\\";
		
		String strRLTable = null;
		try {
			strRLTable = Files.readAllLines(Paths.get(folder, "RLTable.txt")).get(0);
			RL.read(strRLTable);
		} catch(Exception e) {
			
		}
		
		int RLiterations = 5000;
		QLocation all = reactor.getAll(); //.collapse(0.8);
		for(int i = 0; i < RLiterations; i++) {
			Action action = RL.RLcalc(reactor, all, ReactorGenerator.TARGET_HEAT);
			action.submit(reactor);
			Util.prl(Util.round((i / (float) RLiterations) * 100, 1) + "%");
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


