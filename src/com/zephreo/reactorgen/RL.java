package com.zephreo.reactorgen;

import java.util.HashMap;
import java.util.HashSet;

import com.zephreo.reactorgen.Block.BlockType;
import com.zephreo.reactorgen.Cooler.CoolerType;
import com.zephreo.reactorgen.Reactor.ReactorResult;

public class RL {
	
	static HashMap<State, HashMap<Action, Float>> RLTable = new HashMap<State, HashMap<Action, Float>>();

	static Action RLcalc(Reactor reactor, QLocation posbLocs, int targetHeat) throws Exception {
		float bestScore = -Float.MAX_VALUE;
		Action bestAction = null;
		
		for(Location loc : posbLocs.posbLocations.keySet()) {
			State state = new State(reactor.clone(), targetHeat, loc);
			
			if(RLTable.containsKey(state)) {
				HashMap<Action, Float> posbActions = RLTable.get(state);
				for(Action action : posbActions.keySet()) {
					float score = posbActions.get(action) + action.newScore(state);
					posbActions.put(action, score);
					if(score > bestScore) {
						bestScore = score;
						bestAction = action;
					}
					ReactorGenerator.pr(action);
				}
				ReactorGenerator.pr("---------FROMTABLE::::: " + bestAction);
			} else {
				HashMap<Action, Float> posbActions = new HashMap<Action, Float>();
				for(BlockType blockType : BlockType.values()) {
					HashSet<Block> blocks = new HashSet<Block>();
					switch (blockType) {
					case CASING:
						break;
					case COOLER:
						for(CoolerType cooler : CoolerType.VALUES) {
							blocks.add(cooler.toBlock());
						}
						break;
					default:
						blocks.add(blockType.toBlock());
						break;
					}
					for(Block block : blocks) {
						Action action = new Action(state, block);
						posbActions.put(action, action.score);
						if(action.score > bestScore) {
							bestScore = action.score;
							bestAction = action;
						}
					}
				}
				RLTable.put(state, posbActions);
			}
		}
		
		
		return bestAction;
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
			score = r.evaluate(state.targetHeat).score - state.res.score;
		}
		
		float newScore(State state) {
			Reactor r = state.r.clone();
			r.addBlock(blockChanged, state.loc);
			score = r.evaluate(state.targetHeat).score - state.res.score;
			return score;
		}
		
		void submit(Reactor r) {
			r.addBlock(blockChanged, state.loc);
		}
		
		@Override    
	    public boolean equals(Object o) { 
			if(o instanceof Action) {
				Action act = (Action) o;
				return state.equals(act.state) && blockChanged.equals(act.blockChanged);
			}
			return false;
		}    
		
	    @Override    
	    public int hashCode() {   
	    	return state.hashCode() + blockChanged.hashCode();
	    }
	    
	    @Override
	    public String toString() {
	    	return "State: " + state + "\n" + 
	    		   "Loc: " + state.loc + "\n" +  
	    		   "Block: " + blockChanged;
	    }
	}
	
	static class State {
		final HashMap<Block, Integer> surrounding = new HashMap<Block, Integer>();
		Location loc;
		ReactorResult res;
		Reactor r;
		int targetHeat;
		
		State(Reactor reactor, int targetHeat, Location loc) {
			this.loc = loc;
			this.targetHeat = targetHeat;
			r = reactor;
			res = reactor.evaluate(targetHeat);
			calcSurrounding(loc);
		}
		
		void calcSurrounding(Location loc) {
			for(Location adjPos : loc.getAdjacent(Reactor.size)) {
				Block block;
				if(adjPos.withinBounds(Reactor.size)) {
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
	}

}
