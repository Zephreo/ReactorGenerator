package com.zephreo.reactorgen;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
		 
		out.put("InteriorDimensions", reactor.size.toString(true));
		
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
	
	enum SupportedVersion {
		Ver1_2_18(1, 2, 18),
		Ver1_2_25(1, 2, 25);
		
		int major;
		int minor;
		int build;
		
		SupportedVersion(int major, int minor, int build) {
			this.major = major;
			this.minor = minor;
			this.build = build;
		}
		
		static boolean isSupported(int major, int minor, int build) {
			return fromNum(major, minor, build) != null;
		}
		
		static boolean isSupported(int major, int minor) {
			for(SupportedVersion version : SupportedVersion.values()) {
				if(version.major == major && version.minor == minor) {
					return true;
				}
			}
			return false;
		}
		
		static boolean isSupported(int major) {
			for(SupportedVersion version : SupportedVersion.values()) {
				if(version.major == major) {
					return true;
				}
			}
			return false;
		}
		
		static SupportedVersion fromNum(int major, int minor, int build) {
			for(SupportedVersion version : SupportedVersion.values()) {
				if(version.major == major && version.minor == minor && version.build == build) {
					return version;
				}
			}
			return null;
		}
		
		private static final int MAX_MINOR_VERS = 100;  //The maximum number minor could get up to before incrementing a major version
		private static final int MAX_BUILD_VERS = 1000; //The maximum number build could get up to before incrementing a minor version
		
		static SupportedVersion closest(int major, int minor, int build) {
			SupportedVersion closest = null;
			int bestDiff = Integer.MAX_VALUE;
			for(SupportedVersion version : SupportedVersion.values()) {
				int diff = Math.abs(build - version.build) + MAX_BUILD_VERS * Math.abs(minor - version.minor) + MAX_MINOR_VERS * MAX_BUILD_VERS * Math.abs(major - version.major);
				if(diff < bestDiff) {
					bestDiff = diff;
					closest = version;
				}
			}
			return closest;
		}
	}

	static Reactor fromJSON(String filePath) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject root = (JSONObject) parser.parse(new FileReader(filePath));
		
		JSONObject version = (JSONObject) root.get("SaveVersion");
		int major = ((Long) version.get("Major")).intValue();
		int minor = ((Long) version.get("Minor")).intValue();
		int build = ((Long) version.get("Build")).intValue();
		if(!SupportedVersion.isSupported(major)) {
			Util.prl("[ERROR] Input json version is on a different major version");
		} else if(!SupportedVersion.isSupported(major, minor)) {
			Util.prl("[WARNING] Input json version is on a different minor version");
		} else if(!SupportedVersion.isSupported(major, minor, build)) {
			Util.prl("[WARNING] Input json version possibly not supported (on a different build)");
		}
		
		Location size = null;
		HashMap<Location, Block> blocks = null;
		
		SupportedVersion temp = SupportedVersion.closest(major, minor, build);
		
		switch(temp) {
		case Ver1_2_18:
			JSONArray blocksArray = (JSONArray) root.get("CompressedReactor");
			
			size = Location.parseLocation((String) root.get("InteriorDimensions")).add(new Location(1, 1, 1));

			blocks = new HashMap<Location, Block>();
			for(Object blockObj : blocksArray) {
				JSONObject blockJSON = (JSONObject) blockObj;
				String name = blockJSON.keySet().toArray()[0].toString();
				JSONArray locations = (JSONArray) blockJSON.get(name);
				Block block = Block.parse(name);
				for(Object locObj : locations) {
					blocks.put(Location.parseLocation((String) locObj), block);
				}
			}
			break;
		case Ver1_2_25:
			JSONObject blocksObject = (JSONObject) root.get("CompressedReactor");
			
			size = Location.parseLocation((JSONObject) root.get("InteriorDimensions")).add(new Location(1, 1, 1));

			blocks = new HashMap<Location, Block>();
			for(Object blockObj : blocksObject.keySet()) {
				String blockName = (String) blockObj;
				JSONArray locations = (JSONArray) blocksObject.get(blockName);
				Block block = Block.parse(blockName);
				for(Object locObj : locations) {
					blocks.put(Location.parseLocation((JSONObject) locObj), block);
				}
			}
			break;
		}

		Reactor r = new Reactor(size);
		r.addBlocks(blocks);
		
		return r;
	}

}
