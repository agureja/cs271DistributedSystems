import java.util.UUID;


public class Process {
	private static int processUid;
	protected double value;
	protected int quorumSize;
	
	public Process(int pId) {
		processUid = pId;
		quorumSize = OpenBank.serverMapping.size() / 2 + 1;
	}
	
	public int getUniqueId() {
		
		return processUid;
	}
}
