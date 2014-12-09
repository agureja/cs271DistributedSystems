import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NetworkSender {

	
	protected Socket socket;
	protected static PrintWriter outputStream;
	private static HashMap<Integer, String> serverMapping;
	static {
		serverMapping = new HashMap<Integer, String>();
		serverMapping.put(1,"54.173.66.74");
		serverMapping.put(2,"54.67.107.51");
		serverMapping.put(3,"54.77.131.120");
		serverMapping.put(4,"54.169.15.246");
		serverMapping.put(5,"54.94.234.64");
     
	}
	
	public static void sendString(final String s, final String dest){
		//for every send job, start a new thread; or it will be blocked
		  new Thread() {
                        public void run() {
                                try {                                       
                                        Socket socket = new Socket(InetAddress.getByName(dest), 4000);
                                        socket.setSoTimeout(200);
                                        outputStream = new PrintWriter(socket.getOutputStream(), true);
                                        outputStream.println(s);
                                        socket.close();
                                } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                }
                        }
                }.start();

	}
	
	
	//Proposer: send prepare operations to all
	public static void sendPrepare(BallotNumber bal) {
		JSONObject obj = new JSONObject();
		obj.put("task", "prepare");
		obj.put("leader", new Integer(bal.getUniqueId()));
		obj.put("slotNumber", new Integer(OpenBank.log.size()));

		//send ballot number, not bal's process id
		//obj.put("proposalBallotNumber", new Integer(bal.getUniqueId()));
		obj.put("proposalBallotNumber", new Integer(bal.getNumber()));
		for(int i = 1;i<=serverMapping.size();i++ ) {
			sendString(obj.toString(), serverMapping.get(i));
		}
	}
	
	//Acceptor: send promise to i (need to get sender's processId, can be processed when recved prepare msg
	public static void sendPromise(BallotNumber proposedBallot, BallotNumber promiseBallot, double val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "promise");
		obj.put("slotNumber", new Integer(OpenBank.log.size()));
		obj.put("sender",new Integer(OpenBank.id));
		obj.put("proposalUid", new Integer(proposedBallot.getUniqueId()));
		obj.put("proposalBallotNumber", new Integer(proposedBallot.getNumber()));
		obj.put("promisedUid", new Integer(promiseBallot.getUniqueId()));
		obj.put("promiseBallotNumber",  new Integer(promiseBallot.getNumber()));
		obj.put("value", new Double(val));
	//	System.out.println(obj.toString());
	//	System.out.println(serverMapping.get(proposedBallot.getUniqueId()));
		sendString(obj.toString(), serverMapping.get(proposedBallot.getUniqueId()));
	}
	
	//Proposer: send accept to all
	public static void sendAccept(BallotNumber acceptBallot, double val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "accept");
		obj.put("acceptUid", new Integer(acceptBallot.getUniqueId()));
		obj.put("acceptBallotNumber", new Integer(acceptBallot.getNumber()));
		obj.put("value", new Double(val));
//		System.out.println(obj.toString());
		for(int i = 1;i<=serverMapping.size();i++) {
	//		 System.out.println(serverMapping.toString());	
			 sendString(obj.toString(), serverMapping.get(i));
		}	
	}
	
	//Acceptor: send accepted to all
	public static void sendAccepted(BallotNumber acceptedBallot, double val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "accepted");
		obj.put("acceptedUid",  new Integer(acceptedBallot.getUniqueId()));
		obj.put("acceptedBallotNumber",  new Integer(acceptedBallot.getNumber()));
		obj.put("value", new Double(val));
//		System.out.println(obj.toString());
	//	for(int i = 1;i<=serverMapping.size();i++) {
	//		 System.out.println(serverMapping.toString());	
		sendString(obj.toString(), serverMapping.get(acceptedBallot.getUniqueId()));
	//	}	
	}
	
	//when decide on a value, send decide to all
	public static void sendDecide(ArrayList<Integer> senderId, ArrayList<Double> val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "decide");
		JSONArray senderIds = new JSONArray();
		senderIds.addAll(senderId);
		obj.put("senderId", senderIds);
		JSONArray values= new JSONArray();
		for (int i=0; i < val.size(); i++) {
	        values.add(val.get(i));
		}
		obj.put("value", values);
		for(int i = 1;i<=serverMapping.size();i++ ) {
	        //System.out.println(serverMapping.toString());
			sendString(obj.toString(), serverMapping.get(i));
		}	
	}
	
	//send recover request to i
	public static void recoverRequest(int startPos, int destUid) {
		JSONObject obj = new JSONObject();
		obj.put("task", "recoverRequest");
		obj.put("pos", new Integer(startPos));
		obj.put("recieverId", new Integer(OpenBank.id));
		obj.put("senderId", new Integer(destUid));
		sendString(obj.toString(), serverMapping.get(destUid));
	}
	
	//send recover response to j (who sends recover request, destUid)
	public static void recoverResponse(JSONObject obj, int destUid) {
		sendString(obj.toString(), serverMapping.get(destUid));
	}
	
	/*
	 * Optimized part
	 */
	public static void sendOptAccept(int senderId, double val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "optAccept");
		obj.put("senderId", senderId);
		obj.put("value", val);
		for (int i = 0; i < serverMapping.size(); ++i) {
			sendString(obj.toString(), serverMapping.get(i));
		}
	}
	
	public static void sendOptAccepted(int senderId, double val) {
		JSONObject obj = new JSONObject();
		obj.put("task", "optAccepted");
		obj.put("senderId", senderId);
		obj.put("value", val);
		for (int i = 0; i < serverMapping.size(); ++i) {
			sendString(obj.toString(), serverMapping.get(i));
		}
	}
	
	public static void sendOptDecide(int senderId, double value) {
		JSONObject obj = new JSONObject();
		obj.put("task", "optDecide");
		obj.put("senderId", senderId);
		obj.put("value", value);
		for (int i = 0; i < serverMapping.size(); ++i) {
			sendString(obj.toString(), serverMapping.get(i));
		}
	}
}
