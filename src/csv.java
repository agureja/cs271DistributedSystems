import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class csv {
	
	private String fileName;
	private Double val;
	protected ArrayList<ArrayList<Double>> recoverLog;
	protected ArrayList<ArrayList<Double>> readLog;
	private BufferedReader br;
	private String line;
	public csv(String fileName) throws IOException {
		this.fileName = fileName;
		FileWriter fw = new FileWriter(fileName,false);
		fw.write("Deposit	0.0");
		fw.close();
		
		
	}
	
	protected ArrayList<ArrayList<Double>> readLog() {
		recoverLog = new ArrayList<ArrayList<Double>>();
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				String[] contents = line.split("	");
				ArrayList<Double> tempList = new ArrayList<Double>();
				
				if (contents[0] == "Deposit") {
					String[] deposits = contents[1].split(",");
					for(int i=0;i<deposits.length;++i) {
						tempList.add(Double.valueOf(deposits[i]));
					}
						recoverLog.add(tempList);
				} else {
					val = Double.valueOf(contents[1]);
					val = -val;
					tempList.add(val);
					recoverLog.add(tempList);
				}
			}
			return recoverLog;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void printLog() {
		readLog = new ArrayList<ArrayList<Double>> ();
		try {
			br = new BufferedReader(new FileReader(fileName));
			while((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void generateLog(ArrayList <ArrayList<Double>> log) {
		try {
			FileWriter fw = new FileWriter(fileName,false);
			StringBuilder file = new StringBuilder();
			for (int i = 0; i < log.size(); ++i) {
				
				String temp="";
				val = (double) 0;
				for(int j = 0; j<log.get(i).size();++j) {
					val = val + log.get(i).get(j);
					temp+= String.valueOf(Math.abs(val))+",";
				}
				temp=temp.substring(0,temp.length()-2);
				
				if (val >= 0) {
						
						file.append("Deposit	"+temp);
					
				} else {
						
						file.append("Withdraw	"+temp);
			}
			file.append("\r\n");	
			}
			fw.write(file.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
