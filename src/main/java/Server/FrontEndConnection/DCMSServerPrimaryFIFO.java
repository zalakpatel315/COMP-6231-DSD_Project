package Server.FrontEndConnection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import Util.Constants;
import Util.ServerLocations;

public class DCMSServerPrimaryFIFO extends Thread {

	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	DatagramPacket sendPacket;
	int udpPortNum;
	ServerLocations location;
	Logger loggerInstance;
	String recordCount;
	ArrayList<TransferReqToCurrentServer> requests;
	int c;
	Queue<String> FIFORequest = new LinkedList<String>();

	/**
	 * Constructor to set the incoming request with the present ArrayList
	 * 
	 * @param requests
	 *            gets the request
	 * 
	 */

	public DCMSServerPrimaryFIFO(ArrayList<TransferReqToCurrentServer> requests) {
		try {
			this.requests = requests;
			serverSocket = new DatagramSocket(Constants.CURRENT_SERVER_UDP_PORT);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This Thread receives the input from the Front End and adds the data to
	 * the FIFO Queue and transfer the data to the TransferReqToCurrentServer
	 * class
	 */

	@Override
	public void run() {
		byte[] receiveData;
		while (true) {
			try {
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				byte[] receivedData = receivePacket.getData();
				System.out.println("Received pkt :: " + new String(receivedData));
				FIFORequest.add(new String(receivedData));
				TransferReqToCurrentServer transferReq = new TransferReqToCurrentServer(FIFORequest.poll().getBytes(),
						loggerInstance);
				transferReq.start();
				requests.add(transferReq);
				String inputPkt = new String(receivePacket.getData()).trim();
				loggerInstance.log(Level.INFO, "Received " + inputPkt + " from " + location);
			} catch (Exception e) {

			}
		}
	}
}
