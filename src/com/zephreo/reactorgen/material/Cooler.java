package com.zephreo.reactorgen.material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.zephreo.reactorgen.Util;

public interface Cooler extends Block {
	
	public enum CoolerType {
		WATER(60),
		REDSTONE(90),
		QUARTZ(90),
		GOLD(120),
		GLOWSTONE(130),
		LAPIS(120),
		DIAMOND(150),
		HELIUM(140),
		ENDERIUM(120),
		CRYOTHEUM(160),
		IRON(80),
		EMERALD(160),
		COPPER(80),
		TIN(120),
		MAGNESIUM(110),
		ACTIVE_WATER(150),
		ACTIVE_CRYOTHIUM(6400);
		
		final Cooler cooler;
		public int strength;
		public static final ArrayList<CoolerType> VALUES = new ArrayList<CoolerType>();
		public static int SIZE;
		private int weight = 1;
		
		CoolerType(int strength, int weight) {
			this.strength = strength;
			this.weight = weight;
			cooler = new CoolerFactory(this);
		}
		
		CoolerType(int strength) {
			this.strength = strength;
			cooler = new CoolerFactory(this);
		}
		
		public Cooler toBlock() {
			return cooler;
		}

		public static CoolerType fromString(String str) {
			
			for(CoolerType enumValue : CoolerType.values()) {
		        if(enumValue.toBlock().toString().equalsIgnoreCase(str) || enumValue.name().equalsIgnoreCase(str)) {
		            return enumValue;
		        }
		    }
			return null;
		}
		
		public static void setup(HashSet<CoolerType> disabledCoolers) {
			for(CoolerType type : values()) {
				if(!disabledCoolers.contains(type)) {
					for(int i = 0; i < type.weight; i++) {
						VALUES.add(type);
					}
				}
			}
			SIZE = VALUES.size();
		}
		
		private class CoolerFactory implements Cooler {

			CoolerType type;
			protected CoolerFactory(CoolerType type) {
				this.type = type;
			}
			
			@Override
			public CoolerType getCoolerType() {
				return type;
			}

			@Override
			public BlockType getType() {
				return BlockType.COOLER;
			}
			
			@Override
			public String toString() {
				return Util.toTitleCase(getCoolerType().name()).replace("_", "");
			}
			
			@Override    
		    public int hashCode() {   
		    	return type.hashCode();
		    }
		}
	}
	
	public CoolerType getCoolerType();
	
	default boolean equals(Cooler cooler) {
		return cooler.getCoolerType() == getCoolerType();
	}
	
	default boolean equals(Block block) {
		if(block instanceof Cooler) {
			return this.equals((Cooler) block);
		}
		return false;
	}
	
	static Cooler getRandom(Random rnd) {
		return CoolerType.VALUES.get(rnd.nextInt(CoolerType.SIZE)).toBlock();
	}
}
