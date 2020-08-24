package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.HashSet;

public class Location {
	
	protected static final HashSet<Location> RELATIVE_X = new HashSet<Location>();
	static {
		RELATIVE_X.add(new Location(1, 0, 0));
		RELATIVE_X.add(new Location(-1, 0, 0));
	}
	
	protected static final HashSet<Location> RELATIVE_Y = new HashSet<Location>();
	static {
		RELATIVE_Y.add(new Location(0, 1, 0));
		RELATIVE_Y.add(new Location(0, -1, 0));
	}
	
	protected static final HashSet<Location> RELATIVE_Z = new HashSet<Location>();
	static {
		RELATIVE_Z.add(new Location(0, 0, 1));
		RELATIVE_Z.add(new Location(0, 0, -1));
	}
	
	protected static final HashSet<Location> RELATIVE_ADJ = new HashSet<Location>();
	static {
		RELATIVE_ADJ.addAll(RELATIVE_X);
		RELATIVE_ADJ.addAll(RELATIVE_Y);
		RELATIVE_ADJ.addAll(RELATIVE_Z);
	}
	
	int x;
	int y;
	int z;
	
	public Location(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ArrayList<Location> getAdjacent(Location[][][] locations, Location max) {
		return add(RELATIVE_ADJ, locations, max);
	}
	
	public ArrayList<Location> add(HashSet<Location> locs, Location[][][] locations, Location max) {
		ArrayList<Location> out = new ArrayList<Location>();
		for(Location loc : locs) {
			int newX = loc.x + x;
			int newY = loc.y + y;
			int newZ = loc.z + z;
			if(withinBounds(newX, newY, newZ, max)) {
				out.add(locations[newX][newY][newZ]);
			} else {
				out.add(null);
			}
		}
		return out;
	}
	
	public String toString() {
		return "[" + x + ", " + y + ", " + z + "]";
	}
	
	public static boolean withinBounds(int val, int max) {
		return val >= 0 && val < max;
	}
	
	public static boolean withinBounds(int x, int y, int z, Location max) {
		return withinBounds(x, max.x) && withinBounds(y, max.y) && withinBounds(z, max.z);
	}
	
	public boolean withinBounds(Location max) {
		return withinBounds(x, max.x) && withinBounds(y, max.y) && withinBounds(z, max.z);
	}
	
	int count() {
		return x * y * z;
	}
}
