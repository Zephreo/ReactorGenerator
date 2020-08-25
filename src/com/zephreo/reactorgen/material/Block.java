package com.zephreo.reactorgen.material;

import com.zephreo.reactorgen.Util;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

public interface Block {
	
	public enum BlockType {
		AIR,
		FUEL_CELL,
		MODERATOR,
		COOLER,
		CASING;
		
		final Block block;
		
		BlockType() {
			block = new BlockFactory(this);
		}
		
		public Block toBlock() {
			return block;
		}
		
		public String toString() {
			if(this == BlockType.MODERATOR) {
				return "Graphite";
			}
			return Util.toTitleCase(this.name()).replace("_", "");
		}
		
		private class BlockFactory implements Block {

			BlockType type;
			protected BlockFactory(BlockType type) {
				this.type = type;
			}
			
			@Override
			public BlockType getType() {
				return type;
			}
			
			public String toString() {
				return type.toString();
			}
			
			
		}
	}
	
	public static Block fromString(String string) throws Exception {
		string = string.replaceAll("(.)([A-Z])", "$1 $2").trim().replace(" ", "");
	    for(BlockType enumValue : BlockType.values()) {
	        if(enumValue.toString().equalsIgnoreCase(string)) {
	            return enumValue.toBlock();
	        }
	    }
	    for(CoolerType enumValue : CoolerType.values()) {
	        if(enumValue.toString().equalsIgnoreCase(string)) {
	            return enumValue.toBlock();
	        }
	    }
	    return null;
	}
	
	public BlockType getType();
	
	default boolean equals(Block block) {
		return block.getType() == getType();
	}
}
