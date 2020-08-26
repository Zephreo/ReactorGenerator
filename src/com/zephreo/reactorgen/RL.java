package com.zephreo.reactorgen;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.location.QLocation;
import com.zephreo.reactorgen.material.Block;
import com.zephreo.reactorgen.material.Block.BlockType;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

public class RL {
	
	static HashMap<HashMap<Location, Block>, Float> cachedScores = new HashMap<HashMap<Location, Block>, Float>();
	static HashMap<HashMap<Action, Float>, Float> cachedPosbActions = new HashMap<HashMap<Action, Float>, Float>();
	
	static HashMap<HashMap<Location, Block>, HashMap<Action, Float>> RLTable = new HashMap<HashMap<Location, Block>, HashMap<Action, Float>>();
	
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

	static Action RLcalc(Reactor reactor, int targetHeat, double accuracy, int depth) {
		MultiAction posbActions = getPosbActions(reactor, targetHeat, accuracy, depth);
		
		if(depth <= 1) {
			return getBestAction(posbActions.posbActions);
		}
		
		return posbActions.bestFutureAction.previousAction;
	}
	
	static HashMap<MultiActionParamater, MultiAction> cachedMultiActions = new HashMap<MultiActionParamater, MultiAction>();
	
	static class MultiActionParamater {
		Reactor reactor;
		Integer targetHeat;
		Double accuracy;
		Integer depth;
		
		MultiActionParamater(Reactor reactor, int targetHeat, double accuracy, int depth) {
			this.reactor = reactor;
			this.targetHeat = targetHeat;
			this.accuracy = accuracy;
			this.depth = depth;
		}
		
		@Override    
	    public int hashCode() { 
	    	return reactor.blocks.hashCode() + targetHeat.hashCode() + accuracy.hashCode() + depth.hashCode();
	    }
	}
	
	static MultiAction getPosbActions(Reactor reactor, int targetHeat, double accuracy, int depth) {
		MultiAction cache = cachedMultiActions.get(new MultiActionParamater(reactor, targetHeat, accuracy, depth));
		if(cache != null) {
			return cache;
		}
		MultiAction out = new MultiAction();
		out.posbActions = getPosbActions(reactor, targetHeat, accuracy);
		out.score = getBestScore(out.posbActions);
		if(depth > 1) {
			out.futurePosbActions = new HashSet<MultiAction>();
			MultiAction bestFutureAction = null;
			float bestSubScore = -Float.MAX_VALUE;
			for(Action action : out.posbActions.keySet()) {
				Reactor futureReactor = reactor.clone();
				action.submit(futureReactor);
				MultiAction futureAction = getPosbActions(futureReactor, targetHeat, accuracy, depth - 1);
				out.futurePosbActions.add(futureAction);
				if(futureAction.score > bestSubScore) {
					bestSubScore = futureAction.score;
					bestFutureAction = futureAction;
				}
				futureAction.previousAction = action;
			}
			out.score += bestSubScore;
			out.bestFutureAction = bestFutureAction;
		}
		return out;
	}
	
	static Action getBestAction(HashMap<Action, Float> posbActions) {
		return Collections.max(posbActions.entrySet(), Map.Entry.comparingByValue()).getKey();
	}
	
	static float getBestScore(HashMap<Action, Float> posbActions) {
		Float score = cachedPosbActions.get(posbActions);
		if(score == null) {
			score = Collections.max(posbActions.values());
			//cachedPosbActions.put(posbActions, score);
		}
		return score;
	}
	
	static HashMap<Action, Float> getPosbActions(Reactor reactor, int targetHeat, double accuracy) {
		QLocation locationsToCheck = reactor.getAll();
		
		if(accuracy < 1) {
			locationsToCheck = locationsToCheck.collapse(accuracy);
		}
		
		if(!RLTable.containsKey(reactor.blocks)) {
			HashMap<Action, Float> posbActions = new HashMap<Action, Float>();
			for(Location loc : locationsToCheck.toSet()) {
				for(Block block : allBlocks) {
					if(reactor.validate(loc, block)) {
						Action action = new Action(loc, block);
						posbActions.put(action, action.newScore(reactor, targetHeat));
					}
				}
			}
			RLTable.put(reactor.blocks, posbActions);
		} else {
			if(ReactorGenerator.RL_LEARNING_MODE) {
				for(Entry<Action, Float> entry : RLTable.get(reactor.blocks).entrySet()) {
					entry.setValue((entry.getValue() + entry.getKey().newScore(reactor, targetHeat)) / 2);
				}
			}
		}
		
		return RLTable.get(reactor.blocks);
	}
	
	static String RLTable() {
		String out = ""; 
		for(Entry<HashMap<Location, Block>, HashMap<Action, Float>> entry : RLTable.entrySet()) {
			out += entry.toString() + "&";
			
		}
		return out;
	}
	
	//{1,3,1=Helium, ...  4,2,3=Redstone}={Helium_1,3,1=0.0, Air_1,3,1=-959.5158, FuelCell_1,3,1=-4352.0, Gold_1,3,1=0.0}&
	static void read(String file) throws Exception {
		for(String entry : file.split("\\&")) {
			String[] strs = entry.split("\\{|\\}");
			
			//reactor
			HashMap<Location, Block> reactor = new HashMap<Location, Block>();
			String[] rData = strs[1].split("=|,");
			for(int i = 0; i < rData.length; i += 4) {
				Location loc = new Location(Integer.parseInt(rData[i]), Integer.parseInt(rData[i + 1]), Integer.parseInt(rData[i + 2]));
				reactor.put(loc, Block.parse(rData[i + 3]));
			}
			
			//Action data
			HashMap<Action, Float> acc = new HashMap<Action, Float>();
			String[] actionData = strs[3].split("_|,|=");
			for(int i = 0; i < actionData.length; i += 5) {
				Block block = Block.parse(actionData[i]);
				Location loc = new Location(Integer.parseInt(actionData[i + 1]), Integer.parseInt(actionData[i + 2]), Integer.parseInt(actionData[i + 3]));
				acc.put(new Action(loc, block), Float.parseFloat(actionData[i + 4]));
			}
			
			RLTable.put(reactor, acc);
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
	
	static class MultiAction {
		Action previousAction;
		MultiAction bestFutureAction;
		HashSet<MultiAction> futurePosbActions = null;
		HashMap<Action, Float> posbActions;
		Float score;
		
		@Override    
	    public int hashCode() {   
			int out = posbActions.keySet().hashCode() + score.hashCode();
			if(!(futurePosbActions == null)) {
				out += futurePosbActions.hashCode();
			}
	    	return out;
	    }
	    
	    @Override
	    public String toString() {
	    	String out = posbActions.keySet().toString() + score;
			if(!(futurePosbActions == null)) {
				out += futurePosbActions.toString();
			}
	    	return out;
	    }
	}
	
	static class Action {
		Location loc;
		Block blockChanged;
		
		Action(Location loc, Block block) {
			this.loc = loc;
			this.blockChanged = block;
		}
		
		float newScore(Reactor reactor, int targetHeat) {
			Reactor r = reactor.clone();
			r.addBlock(blockChanged, loc);
			float score = getScore(r, targetHeat) - getScore(reactor, targetHeat);
			return score;
		}
		
		void submit(Reactor r) {
			r.addBlock(blockChanged, loc);
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
	    	return blockChanged.hashCode() + loc.hashCode();
	    }
	    
	    @Override
	    public String toString() {
	    	return blockChanged.toString() + "_" + loc.toString();
	    }
	}
}
