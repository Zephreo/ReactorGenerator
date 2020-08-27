# ReactorGenerator
 Nuclearcraft preoverhaul fission reactor generator
 
 Generates a fission reactor based on the input paramaters. 
 
 The main options to use are `--targetHeat` (which can be found in the description of the fuel you plan to use) and xyz dimensions. 
 
 The generator outputs a json file and a text-based representation of the reactor. The json file can be used with hellrage's reactor planner (hellrage/NC-Reactor-Planner)
 
 The generator can also optimise an existing reactor if you give it an input file with the same format as the output. (`-i "./input.json"`)

## Usage
```
java ReactorGen <x> <y> <z> [options]
java ReactorGen 3 3 3 -o "./Builds/optimised.json" -d Cryotheum,Enderium
```

```
 -accuracy <arg>         the percentage of locations to consider optimising [0 - 1] {default: 1}
 -air <arg>              adds score based on air multiplied by this value {default: -1}
 -d,--disable <arg>      What coolers the generator should never use
 -depth <arg>            how many future steps to consider when optimising [1 - ] {default: 1}
 -efficiency <arg>       adds score based on efficiency multiplied by this value {default: 10}
 -h,--targetHeat <arg>   target base heat of fuel [0 - ] {default: 50}
 -heat <arg>             adds score based on heat multiplied by this value {default: 0}
 -i,--input <arg>        input file path
 -maxAir <arg>           decrements score if air is over this amount [0 - ] {default: 10}
 -minEfficiency <arg>    decrements score if efficiency is under this amount [0 - ] {default: 1}
 -minHeat <arg>          decrements score if heat is under this amount [0 - ] {default: 0}
 -minPower <arg>         decrements score if power is under this amount [0 - ] {default: 10}
 -minSymmetry <arg>      decrements score if symmetry is under this amount [0 - 1] {default: 0.4}
 -o,--output <arg>       output file {default: ./optimised.json}
 -oiterations <arg>      iterations used for optimisation [1 - ] {default: 5000}
 -power <arg>            adds score based on power multiplied by this value {default: 10}
 -riterations <arg>      iterations per thread used for random generation [1 - ] {default: 1000}
 -symmetry <arg>         adds score based on symmetry multiplied by this value {default: 1}
 -t,--threads <arg>      threads used for random generation [1 - ] {default: 10}
 ```
