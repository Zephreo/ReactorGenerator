package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.location.QInt;
import com.zephreo.reactorgen.location.QLocation;
import com.zephreo.reactorgen.location.QInt.QIntType;
import com.zephreo.reactorgen.material.Block;
import com.zephreo.reactorgen.material.Cooler;
import com.zephreo.reactorgen.material.Block.BlockType;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

public class Reactor {
	
	Generator generator;
	
	HashMap<Location, Block> blocks = new HashMap<Location, Block>();
	
	Random rnd = new Random();
	
	public static final HashSet<CoolerType> DISABLED_COOLERS = new HashSet<CoolerType>();
	
	public void fillEmpty() {
		//Setup reactor defaults
		for(int x = 0; x < generator.size.x; x++) {
			for(int y = 0; y < generator.size.y; y++) {
				for(int z = 0; z < generator.size.z; z++) {
					Location loc = new Location(x, y, z);
					blocks.put(loc, BlockType.AIR.toBlock());
				}
			}
		}
	}
	
	public Reactor(Generator generator) {
		this.generator = generator;
		fillEmpty();
		//rnd.setSeed(123);
	}
	
	public Reactor(Generator generator, HashMap<Location, Block> blocks) {
		this.generator = generator;
		this.blocks = blocks;
	}
	
	public void addBlocks(HashMap<Location, Block> blocks) {
		this.blocks.putAll(blocks);
	}
	
	public void addRandomCoolers(int count) {
		for(int i = 0; i < count; i++) {
			Cooler cooler = Cooler.getRandom(rnd);
			addRandomBlocks(0, 5, cooler);
		}
	}
	
	public void addRandomCells() {
		addRandomBlocks(BlockType.FUEL_CELL.toBlock());
	}
	
	public void addRandomCells(int min, int max) {
		addRandomBlocks(min, max, BlockType.FUEL_CELL.toBlock());
	}
	
	public void addRandomBlocks(Block block) {
		addRandomBlocks(0, generator.size.count(), block);
	}
	
	public void addRandomBlocks(int min, int max, Block block) {
		int num = rnd.nextInt(max - min + 1) + min;
		for(; num >= 0; num--) {
			Location rndLoc = getRandomLoc();
			if(rndLoc == null) {
				return;
			}
			if(validate(rndLoc, block)) {
				addBlock(block, rndLoc);
			}
		}
	}
	
	QLocation suggestLoc(Block block) {
		switch(block.getType()) {
		case AIR:
			return getAll();
		case COOLER:
			switch(((Cooler) block).getCoolerType()) {
			case ACTIVE_CRYOTHIUM:
				return getEdges();
			case ACTIVE_WATER:
				return getEdges();
			case COPPER:
				return getBlockAdjTo(CoolerType.GLOWSTONE);
			case CRYOTHEUM:
				return getBlockAdjTo(BlockType.FUEL_CELL.toBlock(), 2);
			case DIAMOND:
				return getBlockAdjTo(CoolerType.WATER).and(getBlockAdjTo(CoolerType.QUARTZ));
			case EMERALD:
				return getBlockAdjTo(BlockType.FUEL_CELL).and(getBlockAdjTo(BlockType.MODERATOR));
			case ENDERIUM:
				return getCorners();
			case GLOWSTONE:
				return getBlockAdjTo(BlockType.MODERATOR.toBlock(), 2);
			case GOLD:
				return getBlockAdjTo(CoolerType.WATER).and(getBlockAdjTo(CoolerType.REDSTONE));
			case IRON:
				return getBlockAdjTo(CoolerType.GOLD);
			case LAPIS:
				return getBlockAdjTo(BlockType.FUEL_CELL).and(getEdges());
			case HELIUM:
				return getEdges().and(getBlock(CoolerType.REDSTONE.toBlock()).getAdjacent(false).discard(new QInt(1)));
			case MAGNESIUM:
				return getEdges().and(getBlockAdjTo(BlockType.MODERATOR));
			case QUARTZ:
				return getBlockAdjTo(BlockType.MODERATOR);
			case REDSTONE:
				return getBlockAdjTo(BlockType.FUEL_CELL);
			case TIN:
				break;
			case WATER:
				return getBlockAdjTo(BlockType.FUEL_CELL).add(getBlockAdjTo(BlockType.MODERATOR), true);
			default:
				break;
			}
			break;
		case MODERATOR:
			break;
		case FUEL_CELL:
			break;
		default:
			break;
		}
		return new QLocation(generator.size);
	}
	
	QLocation getAll() {
		return new QLocation(generator.size, new QInt(QIntType.RANGE, 0, generator.size.x), new QInt(QIntType.RANGE, 0, generator.size.y), new QInt(QIntType.RANGE, 0, generator.size.z));
	}
	
	QLocation getEdges() {
		//X-Y Edges
		QLocation edges = new QLocation(generator.size, new QInt(QIntType.RANGE, 0, generator.size.x), new QInt(QIntType.RANGE, 0, generator.size.y), new QInt(0, generator.size.z - 1));
		//X-Z Edges
		edges.add(new QLocation(generator.size, new QInt(QIntType.RANGE, 0, generator.size.x), new QInt(0, generator.size.y - 1), new QInt(QIntType.RANGE, 0, generator.size.z)), true);
		//Y-Z Edges
		edges.add(new QLocation(generator.size, new QInt(0, generator.size.x - 1), new QInt(QIntType.RANGE, 0, generator.size.y), new QInt(QIntType.RANGE, 0, generator.size.z)), true);
		
		return edges;
	}
	
	QLocation getCorners() {
		return new QLocation(generator.size, new QInt(0, generator.size.x - 1), new QInt(0, generator.size.y - 1), new QInt(0, generator.size.z - 1));
	}
	
	QLocation getBlockAdjTo(CoolerType block) {
		return getBlock(block.toBlock()).getAdjacent(true);
	}
	
	QLocation getBlockAdjTo(BlockType block) {
		return getBlock(block.toBlock()).getAdjacent(true);
	}
	
	QLocation getBlockAdjTo(Block block) {
		return getBlock(block).getAdjacent(true);
	}
	
	QLocation getBlockAdjTo(Block block, int num) {
		return getBlock(block).getAdjacent(false).discard(num);
	}
	
	QLocation getBlock(Block block) {
		QLocation out = new QLocation(generator.size);
		for(Location loc : blocks.keySet()) {
			if(blocks.get(loc).equals(block)) {
				out.add(loc);
			}
		}
		return out;
	}
	
	public Location getRandomLoc() {
		return new Location(rnd.nextInt(generator.size.x), rnd.nextInt(generator.size.y), rnd.nextInt(generator.size.z));
	}
	
	public void addBlock(Block block, Location loc) {
		blocks.put(loc, block);
		validateAdj(loc);
	}
	
	public void validateAdj(Location loc) {
		for(Location adjLoc : loc.getAdjacent(generator.size)) {
			if(adjLoc.withinBounds(generator.size) && !validate(adjLoc)) {
				addBlock(BlockType.AIR.toBlock(), adjLoc);
			}
		}
	}
	
	boolean validate(Location loc, Block block) {
		if(block == null) {
			return false;
		}
		switch(block.getType()) {
		case AIR:
			return true;
		case COOLER:
			return validateCooler(loc, (Cooler) block);
		case MODERATOR:
			return adjacentTo(loc, 1, BlockType.FUEL_CELL);
		case FUEL_CELL:
			return true;
		case CASING:
			return true;
		}
		return false;
	}
	
	public boolean validate(Location loc) {
		return validate(loc, blocks.get(loc));
	}
	
	public boolean validateCooler(Location loc, Cooler cooler) {
		if(DISABLED_COOLERS.contains(cooler.getCoolerType())) {
			return false;
		}
		switch(cooler.getCoolerType()) {
		case ACTIVE_CRYOTHIUM:
			return false;
		case ACTIVE_WATER:
			return false; //adjacentTo(loc, 1, BlockType.AIR);
		case COPPER:
			return adjacentTo(loc, 1, CoolerType.GLOWSTONE);
		case CRYOTHEUM:
			return adjacentTo(loc, 2, BlockType.FUEL_CELL);
		case DIAMOND:
			return adjacentTo(loc, 1, CoolerType.WATER) && adjacentTo(loc, 1, CoolerType.QUARTZ);
		case EMERALD:
			return adjacentTo(loc, 1, BlockType.FUEL_CELL) && adjacentTo(loc, 1, BlockType.MODERATOR);
		case ENDERIUM:
			return adjacentTo(loc, BlockType.CASING) == 3;
		case GLOWSTONE:
			return adjacentTo(loc, 2, BlockType.MODERATOR);
		case GOLD:
			return adjacentTo(loc, 1, CoolerType.WATER) && adjacentTo(loc, 1, CoolerType.REDSTONE);
		case IRON:
			return adjacentTo(loc, 1, CoolerType.GOLD);
		case LAPIS:
			return adjacentTo(loc, 1, BlockType.FUEL_CELL) && adjacentTo(loc, 1, BlockType.CASING);
		case HELIUM:
			return adjacentTo(loc, CoolerType.REDSTONE) == 1 && !adjacentTo(loc, 2, CoolerType.REDSTONE) && adjacentTo(loc, 1, BlockType.CASING);
		case MAGNESIUM:
			return adjacentTo(loc, 1, BlockType.MODERATOR) && adjacentTo(loc, 1, BlockType.CASING);
		case QUARTZ:
			return adjacentTo(loc, 1, BlockType.MODERATOR);
		case REDSTONE:
			return adjacentTo(loc, 1, BlockType.FUEL_CELL);
		case TIN:
			return inlineWith(loc, CoolerType.LAPIS.toBlock());
		case WATER:
			return adjacentTo(loc, 1, BlockType.FUEL_CELL) || adjacentTo(loc, 1, BlockType.MODERATOR);
		}
		return false;
	}
	
	public boolean adjacentTo(Location loc, int num, CoolerType type) {
		return adjacentTo(loc, num, type.toBlock());
	}
	
	public boolean adjacentTo(Location loc, int num, BlockType type) {
		return adjacentTo(loc, num, type.toBlock());
	}
	
	public boolean adjacentTo(Location loc, int num, Block block) {
		return adjacentTo(loc, block) >= num;
	}
	
	public int adjacentTo(Location loc, CoolerType type) {
		return adjacentTo(loc, type.toBlock());
	}
	
	public int adjacentTo(Location loc, BlockType type) {
		return adjacentTo(loc, type.toBlock());
	}
	
	public int adjacentTo(Location loc, Block block) {
		int num = 0;
		for(Location adjPos : loc.getAdjacent(generator.size)) {
			if(blocks.getOrDefault(adjPos, BlockType.CASING.toBlock()).equals(block)) {
				num++;
			}
		}
		return num;
	}
	
	private int adjacentToFuel(Location loc) {
		int count = 0;
		for(Location direction : Location.RELATIVE_ADJ) {
			for(int i = 1; i < 6; i++) {
				Location adjLoc = loc.clone().add(direction.clone().multiply(i));
				if(adjLoc.withinBounds(generator.size)) {
					if(blocks.get(adjLoc).getType() == BlockType.FUEL_CELL) {
						count++;
						break;
					}
					if(blocks.get(adjLoc).getType() != BlockType.MODERATOR) {
						break;
					}
				}
			}
		}
		return count;
	}
	
	boolean inlineWith(Location loc, Block block) {
		return isHere(block, loc.add(Location.RELATIVE_X)) || isHere(block, loc.add(Location.RELATIVE_Y)) || isHere(block, loc.add(Location.RELATIVE_Z));
	}
	
	boolean isHere(Block block, ArrayList<Location> area) {
		for(Location loc : area) {
			if(!isHere(block, loc)) {
				return false;
			}
		}
		return true;
	}
	
	boolean isHere(Block block, Location loc) {
		return blocks.get(loc) == block;
	}
	
	class ReactorResult {
		int reactorCells = 0;
		int air = 0;
		
		int totalCooling = 0;
		float genericPower = 0; 
		float genericHeat = 0;
		
		float efficiency = 0;
		float symmetryFactor = 0;
		float maxHeat = 0;
		
		float score = 0;
		
		void print() {
			Util.pr("totalCooling: " + totalCooling + "\n");
			Util.pr("genericPower: " + genericPower + "\n");
			Util.pr("genericHeat: " + genericHeat + "\n");
			Util.pr("efficiency: " + efficiency + "\n");
			Util.pr("symmetryFactor: " + symmetryFactor + "\n");
			Util.pr("maxHeat: " + maxHeat + "\n");
			Util.pr("score: " + score + "\n");
		}
	}
	
	ReactorResult evaluate(int targetHeat) {
		ReactorResult res = new ReactorResult();
		
		//Count cells and air
		//Calculate genericPower, genericHeat, totalCooling
		for(Location loc : blocks.keySet()) {
			Block block = blocks.get(loc);
			switch(block.getType()) {
			case FUEL_CELL:
				int adjCells = adjacentToFuel(loc);
				int adjMods = adjacentTo(loc, BlockType.MODERATOR);
				res.genericPower += (1 + adjCells) + (1 + adjCells) * (adjMods / 6.0);
				res.genericHeat += (adjCells + 1) * (adjCells + 2) / 2.0 + (1 + adjCells) * (adjMods / 3.0);
				res.reactorCells += 1;
				break;
			case COOLER:
				Cooler cooler = (Cooler) block;
				res.totalCooling += cooler.getCoolerType().strength;
				break;
			case MODERATOR:
				if(!adjacentTo(loc, 1, BlockType.FUEL_CELL)) {
					res.genericHeat += 1;
				}
				break;
			case AIR:
				res.air++;
				break;
			default:
				break;
			}
		}
        
		//Symmetry
		for(int x = 0; x < generator.size.x; x++) {
			for(int y = 0; y < generator.size.y; y++) {
				for(int z = 0; z < generator.size.z; z++) {
					Location xyz = new Location(x, y, z);
					//X
					if(blocks.get(xyz) == blocks.get(new Location(generator.size.x - 1 - x, y, z))) {
						res.symmetryFactor += 1;
					}
					//Y
					if(blocks.get(xyz) == blocks.get(new Location(x, generator.size.y - 1 - y, z))) {
						res.symmetryFactor += 1;
					}
					//Zgenerator.size.x - 1 - x
					if(blocks.get(xyz) == blocks.get(new Location(x, y, generator.size.z - 1 - z))) {
						res.symmetryFactor += 1;
					}
					//Swap x - y
					if(blocks.get(xyz) == blocks.get(new Location(y, x, z))) {
						res.symmetryFactor += 1;
					}
					//Swap x - z
					if(blocks.get(xyz) == blocks.get(new Location(z, y, x))) {
						res.symmetryFactor += 1;
					}
					//Swap y - z
					if(blocks.get(xyz) == blocks.get(new Location(x, z, y))) {
						res.symmetryFactor += 1;
					}
                }
            }
        }
		//Normalise
		res.symmetryFactor = res.symmetryFactor / (generator.size.count() * 6);
		
		//The maximum base heat a fuel can have for safe operation
		res.maxHeat = res.totalCooling / res.genericHeat;

		//NaN if reactor cells are 0.
		res.efficiency = (float) (res.genericPower / res.reactorCells);
		
		res.score = res.efficiency * ReactorGenerator.MUTLTIPLIER_EFFICIENCY
				+ res.symmetryFactor * ReactorGenerator.MUTLTIPLIER_SYMMETRY
				+ (res.genericPower / 10) * ReactorGenerator.MUTLTIPLIER_POWER
				+ (res.genericHeat / 10) * ReactorGenerator.MUTLTIPLIER_HEAT
				+ res.air * ReactorGenerator.MUTLTIPLIER_AIR;
		
		if(res.genericPower < ReactorGenerator.MIN_POWER) {
			res.score -= ReactorGenerator.MIN_POWER - res.genericPower;
		}
		
		if(res.genericHeat < ReactorGenerator.MIN_HEAT) {
			res.score -= ReactorGenerator.MIN_HEAT - res.genericHeat;
		}
		
		if(res.efficiency < ReactorGenerator.MIN_EFFICIENCY) {
			res.score -= (ReactorGenerator.MIN_EFFICIENCY - res.efficiency) * (ReactorGenerator.MUTLTIPLIER_EFFICIENCY + 1);
		}
		
		if(res.symmetryFactor < ReactorGenerator.MIN_SYMMETRY) {
			res.score -= ReactorGenerator.MIN_SYMMETRY - res.symmetryFactor;
		}
		
		if(res.air > ReactorGenerator.MAX_AIR) {
			res.score -= res.air - ReactorGenerator.MAX_AIR;
		}
		
		//Penalise heat being below target
		if(targetHeat > res.maxHeat) {
			res.score -= targetHeat - res.maxHeat;
		}
		
		return res;
	}

	public void print(int targetHeat) {
		Util.pr("START-----------------------------------------------------------\n");
		for(int x = 0; x < generator.size.x; x++) {
			for(int y = 0; y < generator.size.y; y++) {
				for(int z = 0; z < generator.size.z; z++) {
					Block block = blocks.get(new Location(x, y, z));
					System.out.format("%15s", block.toString());
				}
				Util.pr("\n");
			}
			Util.pr("-----------------------------------------------------------\n");
		}
		evaluate(targetHeat).print();
		Util.pr(JSON.toJson(this) + "\n");
	}
	
	public Reactor clone() {
		HashMap<Location, Block> blocks = new HashMap<Location, Block>();
		blocks.putAll(this.blocks);
		return new Reactor(generator, blocks);
	}
	
	@Override    
    public boolean equals(Object o) { 
		if(o instanceof Reactor) {
			Reactor r = (Reactor) o;
			return blocks == r.blocks;
		}
		return false;
	}    
	
	/**
	 * Unique given 0 <= [x, y, z] < 24
	 */
    @Override    
    public int hashCode() {   
    	return blocks.hashCode();
    }
}
