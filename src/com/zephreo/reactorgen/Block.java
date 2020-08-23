package com.zephreo.reactorgen;

public interface Block {
	
	public enum BlockType {
		AIR,
		REACTOR_CELL,
		MODERATOR,
		COOLER,
		CASING;
		
		final Block block;
		
		BlockType() {
			block = new BlockFactory(this);
		}
		
		Block toBlock() {
			return block;
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
				return getType().toString();
			}
		}
	}
	
	public BlockType getType();
	
	default boolean equals(Block block) {
		return block.getType() == getType();
	}
}
