package com.zephreo.reactorgen;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.zephreo.reactorgen.location.Location;
import com.zephreo.reactorgen.material.Block;
import com.zephreo.reactorgen.material.Block.BlockType;
import com.zephreo.reactorgen.material.Cooler.CoolerType;

public class JSON {

	@SuppressWarnings("unchecked")
	public static JSONObject toJson(Reactor reactor) {
		JSONObject out = new JSONObject();
		
		JSONObject saveVersion = new JSONObject();
		saveVersion.put("Major", 1);
		saveVersion.put("Minor", 2);
		saveVersion.put("Build", 18);
		saveVersion.put("Revision", 0);
		saveVersion.put("MajorRevision", 0);
		saveVersion.put("MinorRevision", 0);
		
		out.put("SaveVersion", saveVersion);
		
		JSONArray blocks = new JSONArray();
		for(CoolerType type : CoolerType.values()) {
			JSON.blockToJSON(blocks, reactor, type.toBlock());
		}
		JSON.blockToJSON(blocks, reactor, BlockType.FUEL_CELL.toBlock());
		JSON.blockToJSON(blocks, reactor, BlockType.MODERATOR.toBlock());
		
		out.put("CompressedReactor", blocks);
		 
		out.put("InteriorDimensions", reactor.generator.size.toString(true));
		
		JSONObject fuel = new JSONObject();
		fuel.put("Name", "LEU-235 Oxide");
		fuel.put("BasePower", 168);
		fuel.put("BaseHeat", 62.5);
		fuel.put("FuelTime", 72000);
		
		out.put("UsedFuel", fuel);
		
		return out;
	}

	@SuppressWarnings("unchecked")
	static void blockToJSON(JSONArray blocks, Reactor reactor, Block block) {
		int count = 0;
		
		JSONObject blockName = new JSONObject();
		JSONArray coolerArray = new JSONArray();
		for(Location loc : reactor.getBlock(block).posbLocations.keySet()) {
			coolerArray.add(loc.toString());
			count++;
		}
		
		if(count == 0) {
			return;
		}
		
		blockName.put(block.toString(), coolerArray);
		blocks.add(blockName);
	}

	

}
