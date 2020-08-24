package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.Random;

public class QInt {
	public enum QIntType {
		DISCRETE,
		RANGE
	}
	
	ArrayList<Integer> posbInts = new ArrayList<Integer>();
	static Random rnd = new Random();
	
	public QInt(int... posbInts) {
		for(int i = 0; i < posbInts.length; i++) {
			this.posbInts.add(posbInts[i]);
		}
	}
	
	public QInt(QIntType type, int... posbInts) {
		switch(type) {
		case DISCRETE:
			for(int i = 0; i < posbInts.length; i++) {
				this.posbInts.add(posbInts[i]);
			}
		case RANGE:
			if(posbInts.length % 2 == 0) {
				for(int i = 0; i < posbInts.length; i += 2) {
					for (int j = posbInts[i]; j < posbInts[i + 1]; j++) {
						this.posbInts.add(j);
				    }
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
	}
	
	public int collapse() {
		return posbInts.get(rnd.nextInt(posbInts.size()));
	}

	public boolean contains(int val) {
		return posbInts.contains(val);
	}
}