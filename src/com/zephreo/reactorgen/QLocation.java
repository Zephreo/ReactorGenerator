package com.zephreo.reactorgen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public class QLocation {
	
	static Random rnd = new Random();
	
	Location max;
	HashMap<Location, Integer> posbLocations = new HashMap<Location, Integer>();

	public QLocation(Location max, QInt posbX, QInt posbY, QInt posbZ) {
		this.max = max;
		
		for(int x : posbX.posbInts) {
			for(int y : posbY.posbInts) {
				for(int z : posbZ.posbInts) {
					add(x, y, z);
				}
			}
		}
	}
	
	public QLocation(Location max) {
		this.max = max;
	}
	
	public void add(Location loc) {
		add(loc, false);
	}
	
	public void add(Location loc, boolean noWeight) {
		if(loc == null) {
			return;
		}
		
		if((noWeight || !posbLocations.containsKey(loc))) {
			posbLocations.put(loc, 1);
		} else {
			posbLocations.put(loc, posbLocations.get(loc) + 1);
		}
	}
	
	public QLocation and(QLocation other) {
		/*
		QLocation out = new QLocation(locations, max);
		for(Location loc : posbLocations.keySet()) {
			if(other.posbLocations.containsKey(loc)) {
				out.add(loc);
			}
		} //*/
		posbLocations.keySet().retainAll(other.posbLocations.keySet());
		return this;
	}
	 
	public void add(int x, int y, int z) {
		add(new Location(x, y, z));
	}
	
	public void add(Collection<Location> toAdd) {
		add(toAdd, false);
	}
	
	public QLocation add(QLocation toAdd, boolean noWeight) {
		return add(toAdd.posbLocations.keySet(), noWeight);
	}
	
	public QLocation add(Collection<Location> toAdd, boolean noWeight) {
		for(Location loc : toAdd) {
			add(loc, noWeight);
		}
		return this;
	}

	public Location collapse() {
		if(posbLocations.size() > 0) {
			return (Location) posbLocations.keySet().toArray()[rnd.nextInt(posbLocations.size())];
		}
		return null;
	}
	
	/**
	 * Returns a new QLocation with a maximum of 'num' posbLocations
	 * Returns at least one location unless no locations are present
	 * 
	 * @param num	number of locations to keep
	 * @return
	 */
	public QLocation collapse(int num) {
		QLocation out = new QLocation(max);
		for(Location loc : posbLocations.keySet()) {
			out.add(loc);
			num--;
			if(num < 1) {
				return out;
			}
		}
		return out;
	}
	
	public QLocation collapse(double percent) {
		return collapse((int) Math.round(posbLocations.size() * percent));
	}
	
	public QLocation getAdjacent(boolean noWeight) {
		QLocation out = new QLocation(max);
		for(Location loc : posbLocations.keySet()) {
			out.add(loc.getAdjacent(max), noWeight);
		}
		return out;
	}
	
	/**
	 * Returns a new QLocation with locations over a certain weight
	 * 
	 * @param weight
	 */
	public QLocation discard(int weight) {
		QLocation out = new QLocation(max);
		for(Location loc : posbLocations.keySet()) {
			if(posbLocations.get(loc) >= weight) {
				out.add(loc);
			}
		}
		return out;
	}
	
	/**
	 * Returns a new QLocation with weights that correspond to QInt values
	 * 
	 * @param weight
	 */
	public QLocation discard(QInt weight) {
		QLocation out = new QLocation(max);
		for(Location loc : posbLocations.keySet()) {
			if(weight.contains(posbLocations.get(loc))) {
				out.add(loc);
			}
		}
		return out;
	}
	
	
	
	/**
	 * Discards any possible location under a certain weight exclusive
	 * 
	 * @param weight
	 */ /*
	public QLocation discard(int weight) {
		HashSet<Location> toDiscard = new HashSet<Location>();
		for(Location loc : posbLocations.keySet()) {
			if(posbLocations.get(loc) < weight) {
				toDiscard.add(loc);
			}
		}
		for(Location loc : toDiscard) {
			posbLocations.remove(loc);
			posbLocations1.remove(loc);
		}
		return this;
	} //*/
	
}
