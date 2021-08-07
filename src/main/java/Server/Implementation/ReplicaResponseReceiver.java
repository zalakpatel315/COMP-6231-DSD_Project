package Server.Implementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;

import Server.FrontEndConnection.TransferResponseToFE;
import Util.Constants;
import Util.LogManager;
import Util.ServerLocations;

/*
 * Thread class that receives the acknowledgement or response from the replicas 
 * and passes it on to the primary server and 
 */
public class ReplicaResponseReceiver extends Thread {

	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	int udpPortNum;
	ServerLocations location;
	LogManager loggerInstance;
	String recordCount;
	HashMap<Integer, TransferResponseToFE> responses;
	int c;

	public ReplicaResponseReceiver(LogManager logManager) {
		try {
			loggerInstance = logManager;
			serverSocket = new DatagramSocket(Constants.CURRENT_PRIMARY_PORT_FOR_REPLICAS);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public synchronized void run() {
		byte[] receiveData;
		while (true) {
			try {
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				byte[] receivedData = receivePacket.getData();
				String inputPkt = new String(receivedData).trim();
				if (inputPkt.contains("ACKNOWLEDGEMENT")) {
					System.out.println(new String(receivedData));
					loggerInstance.logger.log(Level.INFO, inputPkt);
				} else {
					System.out.println("Received response packet in PRIMARY:: " + new String(receivedData));
					loggerInstance.logger.log(Level.INFO, "Received response in Primary " + inputPkt);
				}
			} catch (Exception e) {

			}
		}
	}
}
