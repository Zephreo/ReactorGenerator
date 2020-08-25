package com.zephreo.reactorgen.location;

import java.util.ArrayList;
import java.util.HashSet;

public class Location {
	
	public static final HashSet<Location> RELATIVE_X = new HashSet<Location>();
	static {
		RELATIVE_X.add(new Location(1, 0, 0));
		RELATIVE_X.add(new Location(-1, 0, 0));
	}
	
	public static final HashSet<Location> RELATIVE_Y = new HashSet<Location>();
	static {
		RELATIVE_Y.add(new Location(0, 1, 0));
		RELATIVE_Y.add(new Location(0, -1, 0));
	}
	
	public static final HashSet<Location> RELATIVE_Z = new HashSet<Location>();
	static {
		RELATIVE_Z.add(new Location(0, 0, 1));
		RELATIVE_Z.add(new Location(0, 0, -1));
	}
	
	public static final HashSet<Location> RELATIVE_ADJ = new HashSet<Location>();
	static {
		RELATIVE_ADJ.addAll(RELATIVE_X);
		RELATIVE_ADJ.addAll(RELATIVE_Y);
		RELATIVE_ADJ.addAll(RELATIVE_Z);
	}
	
	public int x;
	public int y;
	public int z;
	
	public Location(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ArrayList<Location> getAdjacent(Location max) {
		return add(RELATIVE_ADJ);
	}
	
	public Location add(Location toAdd) {
		this.x += toAdd.x;
		this.y += toAdd.y;
		this.z += toAdd.z;
		return this;
	}
	
	public ArrayList<Location> add(HashSet<Location> locs) {
		ArrayList<Location> out = new ArrayList<Location>();
		for(Location loc : locs) {
			out.add(this.clone().add(loc));
		}
		return out;
	}
	
	public Location multiply(int multiplier) {
		x *= multiplier;
		y *= multiplier;
		z *= multiplier;
		return this;
	}
	
	public String toString() {
		return (x + 1) + "," + (y + 1) + "," + (z + 1);
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
	
	public int count() {
		return x * y * z;
	}
	
	@Override    
    public boolean equals(Object o) { 
		if(o instanceof Location) {
			Location loc = (Location) o;
			return loc.x == x && loc.y == y && loc.z == z;
		}
		return false;
	}    
	
	/**
	 * Unique given 0 <= [x, y, z] < 24
	 */
    @Override    
    public int hashCode() {   
    	return (int) (x + (y + 1) * 24 + (z + 1) * 24 * 24);
    }
    
    public Location clone() {
    	return new Location(x, y, z);
    }

	public Object toString(boolean b) {
		return x + "," + y + "," + z;
	}
}
