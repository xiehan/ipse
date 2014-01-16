package madparker.gametools.util;

import processing.core.PVector;

public class Util {

	public static PVector cloneVector(PVector v){
		return new PVector(v.x, v.y, v.z);
	}
	
}
