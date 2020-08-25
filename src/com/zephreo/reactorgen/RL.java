package com.zephreo.reactorgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.location.QLocation;
import com.zephreo.reactorgen.material.Block;
import com.zephreo.reactorgen.material.Block.BlockType;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

public class RL {
	
	static HashMap<HashMap<Location, Block>, Float> cachedScores = new HashMap<HashMap<Location, Block>, Float>();
	static HashMap<String, HashMap<Action, Float>> RLTable = new HashMap<String, HashMap<Action, Float>>();
	
	static HashSet<Block> allBlocks = new HashSet<Block>();
	static {
		for(BlockType blockType : BlockType.values()) {
			switch (blockType) {
			case CASING:
				break;
			case COOLER:
				for(CoolerType cooler : CoolerType.values()) {
					allBlocks.add(cooler.toBlock());
				}
				break;
			default:
				allBlocks.add(blockType.toBlock());
				break;
			}
		}

		for(CoolerType disabled : Reactor.DISABLED_COOLERS) {
			allBlocks.remove(disabled.toBlock());
		}
	}

	static Action RLcalc(Reactor reactor, QLocation posbLocs, int targetHeat) {
		float bestScore = -Float.MAX_VALUE;
		Action bestAction = null;
		
		for(Location loc : posbLocs.posbLocations.keySet()) {
			State state = new State(reactor.clone(), targetHeat, loc);
			
			if(RLTable.containsKey(state.toString())) {
				HashMap<Action, Float> posbActions = RLTable.get(state.toString());
				
				for(Action action : posbActions.keySet()) {
					if(reactor.validate(loc, action.blockChanged)) {
						float score = posbActions.get(action) + action.newScore(state);
						posbActions.put(action, score);
						if(score > bestScore) {
							bestScore = score;
							bestAction = action;
							bestAction.state = state;
						}
					}
				}
			} else {
				HashMap<Action, Float> posbActions = new HashMap<Action, Float>();
				for(Block block : allBlocks) {
					if(reactor.validate(loc, block)) {
						Action action = new Action(state, block);
						posbActions.put(action, action.score);
						if(action.score > bestScore) {
							bestScore = action.score;
							bestAction = action;
						}
					}
				}
				RLTable.put(state.toString(), posbActions);
			}
		}
		return bestAction;
	}
	
	static String RLTable() {
		String out = ""; 
		for(Entry<String, HashMap<Action, Float>> entry : RLTable.entrySet()) {
			out += entry.toString() + "&";
		}
		return out;
	}
	
	//{Lapis=1, Redstone=1, Casing=1, Air=3}={Helium=499.98666, Air=0.0, FuelCell=565.6831}&
	static void read(String file) throws Exception {
		for(String entry : file.split("\\&")) {
			String[] strs = entry.split("\\{|\\}");
			
			//State
			HashMap<Block, Integer> surrounding = new HashMap<Block, Integer>();
			String[] stateData = strs[1].split("=|,");
			for(int i = 0; i < stateData.length; i += 2) {
				surrounding.put(Block.fromString(stateData[i]), Integer.parseInt(stateData[i + 1]));
			}
			
			//Action data
			HashMap<Action, Float> acc = new HashMap<Action, Float>();
			String[] actionData = strs[3].split(",");
			for(int i = 0; i < actionData.length; i++) {
				String[] spl = actionData[i].split("=");
				Block block = Block.fromString(spl[0]);
				acc.put(new Action(block), Float.parseFloat(spl[1]));
			}
			
			RLTable.put(surrounding.toString(), acc);
		}
	}
	
	static float getScore(Reactor r, int targetHeat) {
		if(!cachedScores.containsKey(r.blocks)) {
			float score = r.evaluate(targetHeat).score;
			cachedScores.put(r.blocks, score);
			return score;
		}
		return cachedScores.get(r.blocks);
	}
	
	static class Action {
		float score;
		State state;
		Block blockChanged;
		
		Action(State state, Block block) {
			this.state = state;
			this.blockChanged = block;
			
			Reactor r = state.r.clone();
			r.addBlock(block, state.loc);
			score = getScore(r, state.targetHeat) - getScore(state.r, state.targetHeat);
		}
		
		Action(Block block) {
			this.blockChanged = block;
		}
		
		float newScore(State state) {
			Reactor r = state.r.clone();
			r.addBlock(blockChanged, state.loc);
			score = getScore(r, state.targetHeat) - getScore(state.r, state.targetHeat);
			return score;
		}
		
		void submit(Reactor r) {
			r.addBlock(blockChanged, state.loc);
		}
		
		@Override    
	    public boolean equals(Object o) { 
			if(o instanceof Action) {
				Action act = (Action) o;
				return blockChanged.equals(act.blockChanged);
			}
			return false;
		}    
		
	    @Override    
	    public int hashCode() {   
	    	return blockChanged.hashCode();
	    }
	    
	    @Override
	    public String toString() {
	    	return blockChanged.toString();
	    }
	}
	
	static class State {
		HashMap<Block, Integer> surrounding = new HashMap<Block, Integer>();
		Location loc;
		Reactor r;
		int targetHeat;
		
		State(Reactor reactor, int targetHeat, Location loc) {
			this.loc = loc;
			this.targetHeat = targetHeat;
			r = reactor;
			calcSurrounding(reactor, loc);
		}
		
		void calcSurrounding(Reactor r, Location loc) {
			for(Location adjPos : loc.getAdjacent(r.generator.size)) {
				Block block;
				if(adjPos.withinBounds(r.generator.size)) {
					block = r.blocks.get(adjPos);
				} else {
					block = BlockType.CASING.toBlock();
				}
				surrounding.put(block, surrounding.getOrDefault(block, 0) + 1);
			}
		}
		
		@Override    
	    public boolean equals(Object o) { 
			if(o instanceof State) {
				State st = (State) o;
				return st.surrounding == surrounding; //&& st.res.reactorCells == res.reactorCells;
			}
			return false;
		}    
		
	    @Override    
	    public int hashCode() {   
	    	return surrounding.hashCode(); // + res.reactorCells
	    }
	    
	    public String toString() {
	    	return surrounding.toString();
	    }
	}

}
