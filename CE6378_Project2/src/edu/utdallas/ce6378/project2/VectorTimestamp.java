package edu.utdallas.ce6378.project2;

//Vector time implementation

public class VectorTimestamp implements Comparable<VectorTimestamp>{
	
	private Integer timeVector[] = new Integer[3];
	
	
	
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
		
		if (this.timeVector[0].equals(o.getTimeVector()[0]) 
			&& this.timeVector[1].equals(o.getTimeVector()[1]) &&
			this.timeVector[2].equals(o.getTimeVector()[2])) {
			return 0;
		}
		
		if (this.timeVector[0] < o.getTimeVector()[0]) {
			return -1;
		} else if (this.timeVector[0].equals(o.getTimeVector()[0]) && this.timeVector[1] < o.getTimeVector()[1] ) {
			return -1;
		} else if (this.timeVector[0].equals(o.getTimeVector()[0]) 
			&& this.timeVector[1].equals(o.getTimeVector()[1]) &&
			this.timeVector[2] < o.getTimeVector()[2]) {
			return -1;
		}
		
		return 1;
	}


	public Integer[] getTimeVector() {
		return timeVector;
	}

	public void setTimeVector(Integer[] timeVector) {
		this.timeVector = timeVector;
	}
	
	public void tickVectorTimestamp(VectorTimestamp o) {
		
	}

}
