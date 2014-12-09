

public class Acceptor extends Process {

	private BallotNumber promise;
	private BallotNumber acceptedID;
	
	public Acceptor(int processUid) {
		super(processUid);
		//initially, promise and accepted ID should be unique id + 0
		//promise = new BallotNumber(0);
		//acceptedID = new BallotNumber(0); 
		promise = new BallotNumber(0, 0);
		acceptedID = new BallotNumber(0, 0);
		value = 0;
	}
	
	public void reset() {
		promise.reset();
		acceptedID.reset();
		value = 0;
	}
	
	public void receivePrepare(BallotNumber proposal) {
		
		if (promise.CompareTo(proposal) < 0) {
			promise.setNumber(proposal.getNumber());
			promise.setUniqueId(proposal.getUniqueId());
		}
		NetworkSender.sendPromise(proposal, promise, value);
	}

	public void receiveAccept(BallotNumber proposal, double value) {
		
		//System.out.println("the value is" + String.valueOf( proposal.CompareTo(promise)));
		if (proposal.CompareTo(promise)>=0) {
			promise.setNumber(proposal.getNumber());
			promise.setUniqueId(proposal.getUniqueId());
			
			acceptedID.setNumber(proposal.getNumber());
			acceptedID.setUniqueId(proposal.getUniqueId());
			
			this.value = value;
			
			NetworkSender.sendAccepted(acceptedID, this.value);
		}
	}

}
