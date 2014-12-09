import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class NetworkReceiver extends Thread {
	static ServerSocket socket;
	private String receivedMsg;
	private Socket connectionSocket;
	private BufferedReader receiver;
	
	  public void run() {   
		  try {
			socket = new ServerSocket(4000);
				while(OpenBank.recvThreadControl) {
					receivedMsg ="";
					connectionSocket = socket.accept();
					receiver = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					receivedMsg= receiver.readLine();
					System.out.println("Received Msg is: " + receivedMsg);
					
					String task = readTask(receivedMsg);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser.parse(receivedMsg);
					int  uniqueId, leader, requestId, requestPos;
					double value;
					
					int senderId, proposeUid, promiseUid, slots;
					BallotNumber proposeBal, promiseBal, proposal, prevAcceptedByAcceptor;
					ArrayList<Double> temp;
					switch(task) {
						case "optAccept":			senderId = ((Long) jsonObject.get("senderId")).intValue();
													value = ((Long) jsonObject.get("value")).doubleValue();
													NetworkSender.sendOptAccepted(senderId, value);
													break;
													
						case "optAccepted":			senderId = ((Long) jsonObject.get("senderId")).intValue();
													value = ((Long) jsonObject.get("value")).doubleValue();
													OpenBank.learner.receiveOptAccepted(senderId, value);
													break;
													
						case "optDecide":			senderId = ((Long) jsonObject.get("senderId")).intValue();
													JSONArray values = (JSONArray) jsonObject.get("value");
													temp = new ArrayList<Double>();
													value =0;
													for(int i=0; i<values.size();++i) {
														temp.add(((Double)values.get(i)).doubleValue());
														value +=((Double)values.get(i)).doubleValue();
													}
													if (senderId != OpenBank.id) {
														OpenBank.updateLog(temp, value);
													} else {
														OpenBank.optDecided = true;
													}
													break;
													
						case "recoverRequest" :		requestPos = ((Long) jsonObject.get("pos")).intValue();
													requestId = ((Long) jsonObject.get("senderUid")).intValue();
													sendRecoverResponse(requestPos, requestId);
													break;
							
						case "recoverResponse" :    getLogEntries(receivedMsg);
													break;
											
						case "prepare" :    			
											slots = ((Long) jsonObject.get("slotNumber")).intValue();
											leader = ((Long) jsonObject.get("leader")).intValue();
											if(slots > OpenBank.log.size()) {
												NetworkSender.recoverRequest(OpenBank.log.size(), leader);
												for(int i= OpenBank.log.size();i<slots;i++) {
													temp = new ArrayList<Double>();
													temp.add((double)0);
													OpenBank.log.add(temp);
												}
												
											}
											proposal = new BallotNumber(leader,((Long) jsonObject.get("proposalBallotNumber")).intValue());
											OpenBank.acceptor.receivePrepare(proposal);
											break;
											
						case "promise" :	proposeUid = ((Long) jsonObject.get("proposalUid")).intValue();
											promiseUid = ((Long) jsonObject.get("promisedUid")).intValue();
											senderId = ((Long) jsonObject.get("sender")).intValue();
											proposeBal = new BallotNumber(proposeUid, ((Long) jsonObject.get("proposalBallotNumber")).intValue());
											promiseBal = new BallotNumber(promiseUid, ((Long) jsonObject.get("promiseBallotNumber")).intValue());
											value = ((Double) jsonObject.get("value")).doubleValue();
											slots = ((Long) jsonObject.get("slotNumber")).intValue();
											if(slots > OpenBank.log.size()) {
												NetworkSender.recoverRequest(OpenBank.log.size(), senderId);
												for(int i= OpenBank.log.size();i<slots;i++) {
													temp = new ArrayList<Double>();
													temp.add((double)0);
													OpenBank.log.add(temp);
												}
											}
											OpenBank.proposer.recievePromise(senderId, proposeBal, promiseBal, value);
											break;
											
						case "accept" : 	value = ((Double) jsonObject.get("value")).doubleValue();
					                   	 	proposal = new BallotNumber(
					                   	 			((Long) jsonObject.get("acceptUid")).intValue(),
					                   	 			((Long) jsonObject.get("acceptBallotNumber")).intValue());
					                   	 	OpenBank.acceptor.receiveAccept(proposal, value);
					                   	 	break;
					                   	 	
						case "accepted"  :	proposal = new BallotNumber(
												((Long) jsonObject.get("acceptedUid")).intValue(),
												((Long) jsonObject.get("acceptedBallotNumber")).intValue());
											value = ((Double) jsonObject.get("value")).doubleValue();
											uniqueId = ((Long) jsonObject.get("acceptedUid")).intValue();
											//let learner learn the accepted value, whether can be decided
											//OpenBank.acceptor.receiveAcceptRequest(uniqueId,proposal,value);
											//it doesn't need uniqueId?
											OpenBank.learner.receiveAcceptRequest(uniqueId, proposal, value);
											break;
											
						case "decide" :     /*
											*servers competes for value, which is the next log entrance
											*besides, we still need the amount we have to write in that log entrance
											*change the updateLog(value) to updateLog(amount)
											*maybe we need a new data structure	
											*/
											values = (JSONArray) jsonObject.get("value");
									
											JSONArray senders = (JSONArray) jsonObject.get("senderId");
											ArrayList<Integer> senderList = new ArrayList<Integer>();
											ArrayList<Double> tempVal = new ArrayList<Double>();
											value = 0; 
											for(int i=0;i<senders.size();++i) {
												senderList.add(((Long)senders.get(i)).intValue());
												tempVal.add(((Double)values.get(i)).doubleValue());
												value +=((Double) values.get(i)).doubleValue();
												
											}
											if (OpenBank.updateLog(tempVal,value) == true) {
												if (senderList.contains(OpenBank.id)) {		
													OpenBank.jobQueue.poll();
												}
											}
											OpenBank.proposer.reset();
											OpenBank.learner.reset();
											OpenBank.acceptor.reset();
											break;
					}
					
				
				}
		  } catch (IOException | ParseException e) {
			
			e.printStackTrace();
		}
	      
	      
	  }

	  private String readTask(String msg) throws ParseException {
		  
		  JSONParser jsonParser = new JSONParser();
		  Object jsonMessage = jsonParser.parse(msg);
          JSONObject jsonObject = (JSONObject) jsonMessage; 
          return (String) jsonObject.get("task");

	  }
	  
	  public void sendRecoverResponse(int pos, int senderUid) {
			int destUid = senderUid;
			if (pos < OpenBank.log.size()) {
				JSONObject missLogs = new JSONObject();
				JSONArray missEntries = new JSONArray();
				missEntries.addAll(OpenBank.log.subList(pos, OpenBank.log.size()));
				missLogs.put("task", "recoverResponse");
				missLogs.put("pos", new Integer(pos));
				missLogs.put("entries", missEntries);
				NetworkSender.recoverResponse(missLogs, destUid);
			}
		}
	  
	  private void getLogEntries(String msg) throws ParseException {
		  
		  	 JSONObject obj=(JSONObject) JSONValue.parse((msg));
			 JSONArray content = (JSONArray)obj.get("entries");
			 int position = ((Long)obj.get("pos")).intValue();
			 for(int i=position; i<content.size();++i) {
				 ArrayList<Double> temp = new ArrayList<Double>();
				 JSONArray inContent =  (JSONArray)content.get(i);
				 for(int j=0; j<inContent.size();++j) {
					 temp.add(((Double)inContent.get(j)).doubleValue());
				 }
				 if(position>OpenBank.log.size()){
					 OpenBank.log.add(temp);
				 }  else {
					 OpenBank.log.add(position, temp);
				 }
			 }
	  }
	  
	   
	
}
