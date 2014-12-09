import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Learner extends Process {

	HashMap<Double,Integer> acceptedValues;
	HashMap <Double, Integer> optAcceptedValues;
	ArrayList<Integer> senders;
	boolean optimal = false;
	public Learner(int processUid) {
		super(processUid);
		acceptedValues = new HashMap<Double, Integer>();
		optAcceptedValues = new HashMap <Double, Integer> ();
		senders = new ArrayList<Integer>();
		value=0;
		optimal = false;
	}
	
	public void reset() {
		value = 0;
		optAcceptedValues.clear();
		optimal=false;
		senders.clear();
		acceptedValues.clear();
	}
	public void receiveAcceptRequest(int uniqueId,BallotNumber proposal, double value) {
		if(value>0)
			senders.add(uniqueId);
		if(acceptedValues.containsKey(value)){
			
			acceptedValues.put(value, acceptedValues.get(value)+1);
		} else {
			
			acceptedValues.put(value,1);
		}
		
		if(acceptedValues.get(value) >= quorumSize) {
			ArrayList<Double> tempValue= new ArrayList<Double>();
			ArrayList<Integer> ids=new ArrayList<Integer>();
			
			if(optimal == false) {
				this.value = value;
				tempValue.add(value);
				ids.add(uniqueId);
				acceptedValues.clear();
			} else {
					
				if(value < 0 ) {
					tempValue.add(value);
					ids.add(uniqueId);
				} else {
					Object[] keys = acceptedValues.keySet().toArray();
					for(int i=0; i<acceptedValues.size();++i) {
						if((Double)keys[i]>0 ){
							tempValue.add((Double)keys[i]);
						}
					}
					ids.addAll(senders);
			
				}
			}
			NetworkSender.sendDecide(ids,tempValue);
	}
}
	public void receiveOptAccepted(int senderId, double value) {
		senders.add(senderId);
		if(optAcceptedValues.containsKey(value)) {
			optAcceptedValues.put(value, optAcceptedValues.get(value) + 1);
		} else {
			optAcceptedValues.put(value, 1);
		}
		if(optAcceptedValues.get(value) >= quorumSize) {
			if(optimal == false) {
				this.value = value;
				NetworkSender.sendOptDecide(senderId, value);
				optAcceptedValues.clear();
			}
 		}
	}
}
