package com.zephreo.reactorgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.zephreo.reactorgen.Block.BlockType;
import com.zephreo.reactorgen.Cooler.CoolerType;

public class Reactor {
	
	Location size;
	
	Location locations[][][];
	HashMap<Location, Block> blocks = new HashMap<Location, Block>();
	
	Random rnd = new Random();
	
	public static final HashSet<CoolerType> disabledCoolers = new HashSet<CoolerType>();
	
	public Reactor(Location size) {
		this.size = size;
		locations = new Location[size.x][size.y][size.z];
		for(int x = 0; x < size.y; x++) {
			for(int y = 0; y < size.x; y++) {
				for(int z = 0; z < size.z; z++) {
					Location loc = new Location(x, y, z);
					blocks.put(loc, BlockType.AIR.toBlock());
					locations[x][y][z] = loc;
				}
			}
		}
		//rnd.setSeed(123);
	}
	
	public void addRandomCoolers(int count) {
		for(int i = 0; i < count; i++) {
			Cooler cooler = Cooler.getRandom(rnd);
			while(disabledCoolers.contains(cooler.getCoolerType())) {
				cooler = Cooler.getRandom(rnd);
			}
			addRandomBlocks(0, 5, cooler);
		}
	}
	
	public void addRandomCells() {
		addRandomBlocks(BlockType.REACTOR_CELL.toBlock());
	}
	
	public void addRandomCells(int min, int max) {
		addRandomBlocks(min, max, BlockType.REACTOR_CELL.toBlock());
	}
	
	public void addRandomBlocks(Block block) {
		addRandomBlocks(0, size.count(), block);
	}
	
	public void addRandomBlocks(int min, int max, Block block) {
		int num = rnd.nextInt(max - min + 1) + min;
		for(; num >= 0; num--) {
			Location rndLoc = getRandomLoc();
			if(validate(rndLoc, block)) {
				addBlock(block, rndLoc);
				validateAdj(rndLoc);
			}
		}
	}
	
	public Location getRandomLoc() {
		return locations[rnd.nextInt(size.x)][rnd.nextInt(size.y)][rnd.nextInt(size.z)];
	}
	
	public void addBlock(Block block, Location loc) {
		blocks.put(loc, block);
	}
	
	public void validateAdj(Location loc) {
		for(Location adjLoc : loc.getAdjacent(locations, size)) {
			if(adjLoc != null && !validate(adjLoc)) {
				addBlock(BlockType.AIR.toBlock(), adjLoc);
				validateAdj(adjLoc);
			}
		}
	}
	
	public boolean validate(Location loc, Block block) {
		switch(block.getType()) {
		case AIR:
			return true;
		case COOLER:
			return validateCooler(loc, (Cooler) block);
		case MODERATOR:
			return adjacentTo(loc, 1, BlockType.REACTOR_CELL);
		case REACTOR_CELL:
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
		if(disabledCoolers.contains(cooler.getCoolerType())) {
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
			return adjacentTo(loc, 2, BlockType.REACTOR_CELL);
		case DIAMOND:
			return adjacentTo(loc, 1, CoolerType.WATER) && adjacentTo(loc, 1, CoolerType.QUARTZ);
		case EMERALD:
			return adjacentTo(loc, 1, BlockType.REACTOR_CELL) && adjacentTo(loc, 1, BlockType.MODERATOR);
		case ENDERIUM:
			return adjacentTo(loc, BlockType.CASING) == 3;
		case GLOWSTONE:
			return adjacentTo(loc, 2, BlockType.MODERATOR);
		case GOLD:
			return adjacentTo(loc, 1, CoolerType.WATER) && adjacentTo(loc, 1, CoolerType.REDSTONE);
		case IRON:
			return adjacentTo(loc, 1, CoolerType.GOLD);
		case LAPIS:
			return adjacentTo(loc, 1, BlockType.REACTOR_CELL) && adjacentTo(loc, 1, BlockType.CASING);
		case LIQUID_HELIUM:
			return adjacentTo(loc, CoolerType.REDSTONE) == 1 && adjacentTo(loc, 1, BlockType.CASING);
		case MAGNESIUM:
			return adjacentTo(loc, 1, BlockType.MODERATOR) && adjacentTo(loc, 1, BlockType.CASING);
		case QUARTZ:
			return adjacentTo(loc, 1, BlockType.MODERATOR);
		case REDSTONE:
			return adjacentTo(loc, 1, BlockType.REACTOR_CELL);
		case TIN:
			return inlineWith(loc, CoolerType.LAPIS.toBlock());
		case WATER:
			return adjacentTo(loc, 1, BlockType.REACTOR_CELL) || adjacentTo(loc, 1, BlockType.MODERATOR);
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
		for(Location adjPos : loc.getAdjacent(locations, size)) {
			if(adjPos != null) {
				if(blocks.get(adjPos).equals(block)) {
					num++;
				}
			} else {
				if(block.getType() == BlockType.CASING) {
					num++;
				}
			}
		}
		return num;
	}
	
	boolean inlineWith(Location loc, Block block) {
		return isHere(block, loc.add(Location.RELATIVE_X, locations, size)) || isHere(block, loc.add(Location.RELATIVE_Y, locations, size)) || isHere(block, loc.add(Location.RELATIVE_Z, locations, size));
	}
	
	boolean isHere(Block block, HashSet<Location> area) {
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
		
		float totalCooling = 0;
		float genericPower = 0; 
		float genericHeat = 0;
		
		float efficiency = 0;
		float symmetryFactor = 0;
		float maxHeat = 0;
		
		float score = 0;
		
		void print() {
			pr("totalCooling: " + totalCooling + "\n");
			pr("genericPower: " + genericPower + "\n");
			pr("genericHeat: " + genericHeat + "\n");
			pr("efficiency: " + efficiency + "\n");
			pr("symmetryFactor: " + symmetryFactor + "\n");
			pr("maxHeat: " + maxHeat + "\n");
			pr("score: " + score + "\n");
		}
	}
	
	ReactorResult evaluate(int targetHeat) {
		ReactorResult res = new ReactorResult();
		
		//Count cells and air
		//Calculate genericPower, genericHeat, totalCooling
		for(Location loc : blocks.keySet()) {
			Block block = blocks.get(loc);
			switch(block.getType()) {
			case REACTOR_CELL:
				int adjCells = adjacentTo(loc, BlockType.REACTOR_CELL);
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
				if(!adjacentTo(loc, 1, BlockType.REACTOR_CELL)) {
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
		for(int x = 0; x < size.x; x++) {
			for(int y = 0; y < size.y; y++) {
				for(int z = 0; z < size.z; z++) {
					//X
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[size.x - 1 - x][y][z])) {
						res.symmetryFactor += 1;
					}
					//Y
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[x][size.y - 1 - y][z])) {
						res.symmetryFactor += 1;
					}
					//Z
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[x][y][size.z - 1 - z])) {
						res.symmetryFactor += 1;
					}
					//Swap x - y
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[y][x][z])) {
						res.symmetryFactor += 1;
					}
					//Swap x - z
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[z][y][x])) {
						res.symmetryFactor += 1;
					}
					//Swap y - z
					if(blocks.get(locations[x][y][z]) == blocks.get(locations[x][z][y])) {
						res.symmetryFactor += 1;
					}
                }
            }
        }
		//Normalise
		res.symmetryFactor = res.symmetryFactor / (size.count() * 6);
		
		//The maximum base heat a fuel can have for safe operation
		res.maxHeat = res.totalCooling / res.genericHeat;

		//NaN if reactor cells are 0.
		res.efficiency = (float) (res.genericPower / res.reactorCells);
		
		res.score = res.efficiency
				+ res.symmetryFactor * 10
				+ res.genericPower / 10 + res.genericHeat / 50
				- res.air;
		
		//Penalise heat being below target
		if(targetHeat > res.maxHeat) {
			res.score -= targetHeat - res.maxHeat;
		}
		
		return res;
	}
	
	public void print(int targetHeat) {
		pr("START-----------------------------------------------------------\n");
		for(int x = 0; x < size.y; x++) {
			for(int y = 0; y < size.x; y++) {
				for(int z = 0; z < size.z; z++) {
					Block block = blocks.get(locations[x][y][z]);
					System.out.format("%15s", block.toString());
				}
				pr("\n");
			}
			pr("-----------------------------------------------------------\n");
		}
		evaluate(targetHeat).print();
	}
	
	public static void pr(Object message) {
		System.out.print(message);
	}
}
