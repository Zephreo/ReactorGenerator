package com.zephreo.reactorgen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface Cooler extends Block {
	
	public enum CoolerType {
		AIR(0),
		WATER(60),
		REDSTONE(90),
		QUARTZ(90),
		GOLD(120),
		GLOWSTONE(130),
		LAPIS(120),
		DIAMOND(150),
		LIQUID_HELIUM(140),
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
		int strength;
		public static final List<CoolerType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		public static final int SIZE = VALUES.size();
		
		CoolerType(int strength) {
			this.strength = strength;
			cooler = new CoolerFactory(this);
		}
		
		Cooler toBlock() {
			return cooler;
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
			
			public String toString() {
				return getCoolerType().toString();
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
