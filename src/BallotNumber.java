
public class BallotNumber {

	private int number;
	private int uniqueId;
	
	public BallotNumber(int uniqueId) {
		  
		this.uniqueId = uniqueId;
		this.number = 0;
	}
	
	public void reset() {
		this.uniqueId = 0;
		this.number = 0;
	}
	
	public BallotNumber(int uniqueId, int number) {
		  
		this.uniqueId = uniqueId;
		this.number = number;
	}
	
	public int getUniqueId() {
		return uniqueId;
	}
	
	public void setUniqueId(int id) {
		this.uniqueId = id;
	}
	
	public void setNumber(int value) {
		number = value;
	}
	
	public int getNumber() {
		return number;
	}
	
	public int CompareTo(BallotNumber temp) {

		if(this.number > temp.getNumber())
			return 1;
		else if(this.number < temp.getNumber())
			return -1;
	
		return 0;		
	}
	
}
