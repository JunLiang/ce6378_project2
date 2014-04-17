package edu.utdallas.ce6378.project2;

import java.io.Serializable;
import java.util.Random;

//Vector time implementation

public class VectorTimestamp implements Comparable<VectorTimestamp>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1704856273686947045L;

	
	
	private Integer timeVector[] = new Integer[Constant.topN];
	
	
	
	/*Initialize a time vector*/
	public VectorTimestamp () {
		//Initialize the vector to 0
		for (int i = 0; i < timeVector.length; i++) {
			timeVector[i] = 0;
		}
	}
	
	@Override
	public int compareTo(VectorTimestamp o) {
		// TODO Auto-generated method stub
		//If the two vector's have different size, then no comparison can be made
		//assume they are equal in this case.
		assert(o.getTimeVector().length != this.timeVector.length);
		
		for (int i = 0; i < this.timeVector.length; i++) {
			if (this.timeVector[i] < o.getTimeVector()[i]) {
				return -1;
			} else if (this.timeVector[i] < o.getTimeVector()[i]) {
				return 1;
			}
		}
		return 0;
	}


	public Integer[] getTimeVector() {
		return timeVector;
	}

	public void setTimeVector(Integer[] timeVector) {
		assert (this.timeVector.length == timeVector.length);
		
		for (int i = 0; i < this.timeVector.length; i++) {
			this.timeVector[i] = timeVector[i];
		}
	}
	
	public void tickVectorTimestamp(Integer nodeId) {
		this.timeVector[nodeId] = this.timeVector[nodeId]+1;
		
	}
	
	public void adjustVectorTimestamp(VectorTimestamp vts, Integer nodeId) {
		for (int i = 0; i < vts.getTimeVector().length; i++) {
			if (i != nodeId) {
				if (this.timeVector[i] < vts.getTimeVector()[i]) {
					this.timeVector[i] = vts.getTimeVector()[i];
				}
			} else {
				assert (this.timeVector[i]  >= vts.getTimeVector()[i]);
			}
		}
	}
	
	public String printTimestamp () {
		StringBuilder a = new StringBuilder();
		a.append("[");
		for (int i = 0; i < this.timeVector.length; i++) {
			a.append(this.timeVector[i]).append(" ");
		}
		a.append("]");
		
		return a.toString();
	}
	
	public static void main(String[] args) {
		VectorTimestamp a = new VectorTimestamp();
		VectorTimestamp b = new VectorTimestamp();
		
		Random rand = new Random();
		
		Integer [] av = new Integer[] {rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(1000)};
		Integer [] bv = new Integer[] {rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(1000)};
		
		a.setTimeVector(av);
		b.setTimeVector(bv);
		
		System.out.println("A " + a.printTimestamp() + " compares B " + b.printTimestamp() + " is " + a.compareTo(b));
		
	}

}
