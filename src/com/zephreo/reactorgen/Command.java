package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;

import com.zephreo.reactorgen.material.Cooler.CoolerType;

public class Command {
	
	static final Options OPTIONS = new Options();
	static final ArrayList<CustomOption> ARGS = new ArrayList<CustomOption>();
	static { 
        new CustomOption("i", "input", OptionType.INPUT_FILE, String.class, null, true, "input file path");
        new CustomOption("o", "output", OptionType.OUTPUT_FILE, String.class, "./optimised.json", true, "output file");
        
        new CustomOption("h", "targetHeat", OptionType.TARGET_HEAT, 0, null, Integer.class, 50, true, "target base heat of fuel");
        new CustomOption("t", "threads", OptionType.THREAD_COUNT, 1, null, Integer.class, 10, true, "threads used for random generation");
        new CustomOption("riterations", OptionType.RANDOM_ITERATIONS, 1, null, Integer.class, 1000, true, "iterations per thread used for random generation");
        new CustomOption("oiterations", OptionType.OPTIMISATION_ITERATIONS, 1, null, Integer.class, 5000, true, "iterations used for optimisation");
        new CustomOption("depth", OptionType.OPTIMISATION_DEPTH, 1, null, Integer.class, 1, true, "how many future steps to consider when optimising");
        
        new CustomOption("accuracy", OptionType.ACCURACY, 0, 1, Float.class, 1, true, "the percentage of locations to consider optimising");
        
        new CustomOption("air", OptionType.MULTIPLIER_AIR, Float.class, -1, true, "adds score based on air multiplied by this value");
        new CustomOption("power", OptionType.MULTIPLIER_POWER, Float.class, 10, true, "adds score based on power multiplied by this value");
        new CustomOption("heat", OptionType.MULTIPLIER_HEAT, Float.class, 0, true, "adds score based on heat multiplied by this value");
        new CustomOption("efficiency", OptionType.MULTIPLIER_EFFICIENCY, Float.class, 10, true, "adds score based on efficiency multiplied by this value");
        new CustomOption("symmetry", OptionType.MULTIPLIER_SYMMETRY, Float.class, 1, true, "adds score based on symmetry multiplied by this value");
        
        new CustomOption("maxAir", OptionType.MAX_AIR, 0, null, Float.class, 10, true, "decrements score if air is over this amount");
        new CustomOption("minPower", OptionType.MIN_POWER, 0, null, Float.class, 10, true, "decrements score if power is under this amount");
        new CustomOption("minHeat", OptionType.MIN_HEAT, 0, null, Float.class, 0, true, "decrements score if heat is under this amount");
        new CustomOption("minEfficiency", OptionType.MIN_EFFICIENCY, 0, null, Float.class, 1, true, "decrements score if efficiency is under this amount");
        new CustomOption("minSymmetry", OptionType.MIN_SYMMETRY, 0, 1, Float.class, 0.4, true, "decrements score if symmetry is under this amount");
        
        new CustomOption("d", "disable", OptionType.DISABLED_COOLERS, String.class, null, true, "What coolers the generator should never use");
	}
	
	enum OptionType {
		INPUT_FILE,
		OUTPUT_FILE,
		TARGET_HEAT,
		THREAD_COUNT,
		RANDOM_ITERATIONS,
		OPTIMISATION_ITERATIONS,
		OPTIMISATION_DEPTH,
		MULTIPLIER_AIR,
		MULTIPLIER_POWER,
		MULTIPLIER_HEAT,
		MULTIPLIER_EFFICIENCY,
		MULTIPLIER_SYMMETRY,
		MAX_AIR,
		MIN_POWER,
		MIN_HEAT,
		MIN_EFFICIENCY,
		MIN_SYMMETRY, 
		ACCURACY, 
		DISABLED_COOLERS;
	}

	static void parseCommand(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(OPTIONS, args);
            List<String> remArgs = cmd.getArgList();
            for(CustomOption option : ARGS) {
            	option.apply();
            }
            if(ReactorGenerator.INPUT_FILE != null) {
            	ReactorGenerator.SIZE.x = Integer.parseInt(remArgs.get(0));
                ReactorGenerator.SIZE.y = Integer.parseInt(remArgs.get(1));
                ReactorGenerator.SIZE.z = Integer.parseInt(remArgs.get(2));
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("reactor-generator", OPTIONS);

            System.exit(1);
        }
	}
	
	static class CustomOption {
		String name;
		Integer min;
		Integer max;
		Object defaultValue;
		String value;
		OptionType op;
		
		void apply() throws Exception {
			String returnVal = OPTIONS.getOption(name).getValue();
			if(returnVal == null && defaultValue != null) {
				returnVal = defaultValue.toString();
			}
			if(min != null) {
				double val = Float.parseFloat(returnVal);
				if(val < min) {
					throw new Exception(name + " is too small, must be greater than " + min);
				}
			}
			if(max != null) {
				double val = Float.parseFloat(returnVal);
				if(val > max) {
					throw new Exception(name + " is too large, must be less than " + max);
				}
			}
			switch(op) {
			case INPUT_FILE:
				ReactorGenerator.INPUT_FILE = returnVal;
				break;
			case MAX_AIR:
				ReactorGenerator.MAX_AIR = Integer.parseInt(returnVal);
				break;
			case MIN_EFFICIENCY:
				ReactorGenerator.MIN_EFFICIENCY = Float.parseFloat(returnVal);
				break;
			case MIN_HEAT:
				ReactorGenerator.MIN_HEAT = Float.parseFloat(returnVal);
				break;
			case MIN_POWER:
				ReactorGenerator.MIN_POWER = Float.parseFloat(returnVal);
				break;
			case MIN_SYMMETRY:
				ReactorGenerator.MIN_SYMMETRY = Float.parseFloat(returnVal);
				break;
			case MULTIPLIER_AIR:
				ReactorGenerator.MULTIPLIER_AIR = Float.parseFloat(returnVal);
				break;
			case MULTIPLIER_EFFICIENCY:
				ReactorGenerator.MULTIPLIER_EFFICIENCY = Float.parseFloat(returnVal);
				break;
			case MULTIPLIER_HEAT:
				ReactorGenerator.MULTIPLIER_HEAT = Float.parseFloat(returnVal);
				break;
			case MULTIPLIER_POWER:
				ReactorGenerator.MULTIPLIER_POWER = Float.parseFloat(returnVal);
				break;
			case MULTIPLIER_SYMMETRY:
				ReactorGenerator.MULTIPLIER_SYMMETRY = Float.parseFloat(returnVal);
				break;
			case OPTIMISATION_DEPTH:
				ReactorGenerator.OPTIMISATION_DEPTH = Integer.parseInt(returnVal);
				break;
			case OPTIMISATION_ITERATIONS:
				ReactorGenerator.OPTIMISATION_ITERATIONS = Integer.parseInt(returnVal);
				break;
			case OUTPUT_FILE:
				ReactorGenerator.OUTPUT_FILE = returnVal;
				break;
			case RANDOM_ITERATIONS:
				ReactorGenerator.RANDOM_ITERATIONS = Integer.parseInt(returnVal);
				break;
			case TARGET_HEAT:
				ReactorGenerator.TARGET_HEAT = Integer.parseInt(returnVal);
				break;
			case THREAD_COUNT:
				ReactorGenerator.THREAD_COUNT = Integer.parseInt(returnVal);
				break;
			case ACCURACY:
				ReactorGenerator.ACCURACY = Float.parseFloat(returnVal);
				break;
			case DISABLED_COOLERS:
				String[] coolers = OPTIONS.getOption(name).getValues();
				if(coolers != null) {
					for(String strCooler : coolers) {
						Reactor.DISABLED_COOLERS.add(CoolerType.fromString(strCooler));
					}
				}
				break;
			}
		}
		
		CustomOption(String name, OptionType op, Integer min, Integer max, Class<?> type, Object defaultValue, boolean hasArg, String description) {
			setup(null, name, op, min, max, type, defaultValue, hasArg, description, false, 1);
		}
		
		CustomOption(String shortName, String name, OptionType op, Integer min, Integer max, Class<?> type, Object defaultValue, boolean hasArg, String description) {
			setup(shortName, name, op, min, max, type, defaultValue, hasArg, description, false, 1);
		}
		
		CustomOption(String name, OptionType op, Class<?> type, Object defaultValue, boolean hasArg, String description) {
			setup(null, name, op, null, null, type, defaultValue, hasArg, description, false, 1);
		}
		
		CustomOption(String shortName, String name, OptionType op, Class<?> type, Object defaultValue, boolean hasArg, String description) {
			setup(shortName, name, op, null, null, type, defaultValue, hasArg, description, false, 1);
		}
		
		CustomOption(String shortName, String name, OptionType op, Class<?> type, Object defaultValue, boolean hasArg, String description, int numArgs) {
			setup(shortName, name, op, null, null, type, defaultValue, hasArg, description, false, numArgs);
		}
		
		CustomOption(String shortName, String name, OptionType op, Class<?> type, Object defaultValue, boolean hasArg, String description, boolean required) {
			setup(shortName, name, op, null, null, type, defaultValue, hasArg, description, required, 1);
		}
		
		private void setup(String shortName, String name, OptionType op, Integer min, Integer max, Class<?> type, Object defaultValue, boolean hasArg, String description, boolean required, int numArgs) {
			this.name = name;
			this.min = min;
			this.max = max;
			this.defaultValue = defaultValue;
			this.op = op;
			if(min != null && max == null) {
				description += " [" + min + " - ]";
			}
			if(min == null && max != null) {
				description += " [ - " + max + "]";
			}
			if(min != null && max != null) {
				description += " [" + min + " - " + max + "]";
			}
			if(defaultValue != null) {
				description += " {default: " + defaultValue + "}";
			}
			Option option;
			if(shortName == null) {
				option = new Option(name, hasArg, description);
			} else {
				option = new Option(shortName, name, hasArg, description);
			}
			option.setRequired(required);
			option.setType(type);
			option.setArgs(numArgs);
	        OPTIONS.addOption(option);
	        ARGS.add(this);
		}
	}

}
