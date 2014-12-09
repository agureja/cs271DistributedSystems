import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class Proposer extends Process {

	BallotNumber propId;
	BallotNumber prevIDAcceptedByProposer;
	
	Set<Integer> promiseList;
	
	public Proposer(int processUid) {
		super(processUid);
		propId = new BallotNumber(processUid); 
		
		prevIDAcceptedByProposer = new BallotNumber(0);
		value = 0;
		//list of user accepting the promise.
		promiseList = new HashSet<Integer>(); 
	}
	
	public void reset() {
		propId.reset();
		prevIDAcceptedByProposer.reset();
		value = 0;
	}
	
	public boolean sendPrepare(double value) {
		
		promiseList.clear();
		propId.setNumber(propId.getNumber()+1); // counter*n+1
		NetworkSender.sendPrepare(propId);
		return true;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	/*
	 * still need modification this part
	 */
	public boolean recievePromise(Integer uuid, BallotNumber propId, BallotNumber prevAcceptedByAcceptor, double value) {
		//I feel like we don't need the uuid
		//but leave it here at first
		
		if(prevIDAcceptedByProposer.getUniqueId() == 0) {
			
			prevIDAcceptedByProposer = new BallotNumber(prevAcceptedByAcceptor.getUniqueId(),prevAcceptedByAcceptor.getNumber());
		}
		else if(prevIDAcceptedByProposer.CompareTo(prevAcceptedByAcceptor)<0) {
			
			prevIDAcceptedByProposer.setUniqueId(prevAcceptedByAcceptor.getUniqueId());
			prevIDAcceptedByProposer.setNumber(prevAcceptedByAcceptor.getNumber());
			this.value = value;
		}

		promiseList.add(uuid);
		if(promiseList.size() >= quorumSize) {
			NetworkSender.sendAccept(propId, this.value);
			promiseList.clear();
		}
		return true;
	}
	
}
