import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OpenBank {

	protected static ArrayList<ArrayList<Double>> log; // logging array
	protected static LinkedList<Double> jobQueue;
	static boolean decide;
	static HashMap<String, Integer> serverMapping; // server list
	static int counter;
	static String localIP;
	static Proposer proposer;
	static Acceptor acceptor;
	static Learner learner;
	static int id = 1;
	
	static csv logFile;
	static String fileName = "//homw//ec2-user//cs271Project//log" + id + ".csv";

	static boolean recvThreadControl;
	static boolean isOptimized;
	static boolean optDecided;
	static {
		serverMapping = new HashMap<String, Integer>();
		serverMapping.put("54.173.66.74", 1);
		serverMapping.put("54.67.107.51", 2);
		serverMapping.put("54.77.131.120", 3);
		serverMapping.put("54.169.15.246", 4);
		serverMapping.put("54.94.234.64", 5);
		log = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> temp= new ArrayList<Double>();
		temp.add((double) 0);
		log.add(temp);// initial balance
	//	counter = 0; // operation number
	}
	static int bal = id - serverMapping.size();
	
	public static void main(String args[]) throws Exception {
		
		JSONObject missLogs = new JSONObject();
		JSONArray missEntries = new JSONArray();
		ArrayList<Double> temp= new ArrayList<Double>();
		temp.add((double) 100);
		
		ArrayList<Double> temp2= new ArrayList<Double>();
		temp2.add((double) 200);
		temp2.add((double) 300);
		
		ArrayList<Double> temp3= new ArrayList<Double>();
		temp3.add((double) 400);
		temp3.add((double) 500);
		temp3.add((double) 600);
		
		log.add(temp);
		log.add(temp2);
		log.add(temp3);
		
		missEntries.addAll(OpenBank.log.subList(0, OpenBank.log.size()));
		missLogs.put("tast", "recoverResponse");
		missLogs.put("entries", missEntries);
		
		 JSONObject obj=(JSONObject) JSONValue.parse((missLogs.toJSONString()));
		 JSONArray content = (JSONArray)obj.get("entries");
		 for(int i=0; i<content.size();++i) {
			 ArrayList<Double> temp4 = new ArrayList<Double>();
			 JSONArray inContent =  (JSONArray)content.get(i);
			 for(int j=0; j<inContent.size();++j) {
				 temp4.add(((Double)inContent.get(j)).doubleValue());
			 }
			 log.add(temp4);
		 }
			 
		logFile = new csv(fileName);
		jobQueue = new LinkedList<Double>();
		recvThreadControl = true;
		optDecided = false;
		try {
			localIP = InetAddress.getLocalHost().getHostName();
			System.out.println("Local Ipaddress is " + String.valueOf(localIP));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		proposer = new Proposer(id);
		acceptor = new Acceptor(id);
		learner = new Learner(id);

		new Thread(new NetworkReceiver()).start();
		System.out.println("Account is logged in!");
		System.out.println("Choose Paxos mode:");
		System.out.println("1. ISPaxos");
		System.out.println("2. Modified Paxos");

		Scanner in = new Scanner(System.in);
		String inPaxos = "";
		inPaxos = in.nextLine();

		switch (inPaxos) {
		case "1":
			isOptimized = false;
			break;
		case "2":
			isOptimized = true;
			break;
		}

		System.out.println("1. Deposit/withdraw(+/-)");
		System.out.println("2. Check balance");
		System.out.println("3. Print log file");
		System.out.println("4. Fail");
		System.out.println("5. Unfail");

		while (true) {
			decide = false;

			String input = "";
			input = in.nextLine();
			System.out.println("Which operation do you want to perform");

			switch (input) {
			case "1":
				System.out.println("Enter the amount you want to deposit/ withdraw:");
				double val = Double.parseDouble(in.nextLine());
				// optimized && deposit operation
				if (isOptimized && val > 0) {
					
					//updateLog(val);
					NetworkSender.sendOptAccept(id, val);
					long timeStamp = System.currentTimeMillis();
					while (optDecided == false) {
						if (System.currentTimeMillis() - timeStamp > 2) {
							/*
							increase ballot number
							send again
							update the curr time stamp
							counter: if fails 10 times, finally break
							*/
							break;
						}
					}
					// other server has accepted the value
					if (optDecided == true) {
						optDecided = false;
					} else {
						// jump out from while loop because of time out
						log.remove(log.size() - 1);
						sendToLeaderQueue(val);
					}
				} else {
					sendToLeaderQueue(val);
					proposer.value = jobQueue.peek();
					//every time I will increase my ballot number 
					NetworkSender.sendPrepare(new BallotNumber(id, bal + serverMapping.size()));
				}
				break;

			case "2":
				Double balance = checkBalance();
				System.out.println("Your Balance is: " + String.valueOf(balance));
				break;

			case "3":
				logFile.printLog();
				break;
				
			case "4":
				recvThreadControl = false;
				log.clear();
				break;

			case "5":
				recvThreadControl = true;
				log = logFile.readLog();
				// ping to any server to recover log
				if (id != 5) {
					NetworkSender.recoverRequest(log.size(), id + 1);
				} else {
					NetworkSender.recoverRequest(log.size(), id - 1);
				}
				break;

			default:
				System.out.println("Invalid entry. Press enter to continue");
				System.console().readLine();
			}

		}

	}

	public static Double checkBalance() {
		Double currBalance = log.get(0).get(0);
		for (int i = 1; i < log.size(); i++) {
			for(int j=1;j <log.get(i).size();j++) {
				
				if (log.get(i).get(j) == 0) {
					break;
				} else {
					currBalance += log.get(i).get(j);
				}

			}
		}
		return currBalance;	
	}
	public static boolean updateLog(ArrayList<Double> amount,Double transactionAmount) {
		Double currBalance = checkBalance();
		
		if (currBalance + transactionAmount < 0) {
			System.out.println("Withdraw failure: the balance is less than"
					+ String.valueOf(Math.abs(transactionAmount)));
			return false;
		} else {
			log.add(amount);
			logFile.generateLog(log);
			//not pop job queue here
			//only pop job from the sender server's job queue
			//jobQueue.poll();
			return true;
		}
	}

	public static void sendToLeaderQueue(double value) {
		jobQueue.add(value);
	}
}
