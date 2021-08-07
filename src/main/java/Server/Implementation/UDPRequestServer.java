package Server.Implementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Models.Record;
import Models.Student;
import Models.Teacher;
import Util.ServerLocations;

public class UDPRequestServer extends Thread {
	DatagramSocket serverSocket;
	ServerLocations location;
	private DatagramPacket receivePacket;
	private MainServerImpl server;
	private Logger loggerInstance;
	private Object mapLock;

	/**
	 * DcmsServerUDPRequestServer forwards the request received to the
	 * respective server port
	 * 
	 * @param pkt
	 *            datagram packet that holds the packet information
	 * @param serverImp
	 *            server instance of the requested location
	 * 
	 */

	public UDPRequestServer(DatagramPacket pkt, MainServerImpl serverImp, Logger logger) {
		receivePacket = pkt;
		server = serverImp;
		mapLock = new Object();
		this.loggerInstance = logger;
		try {
			serverSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */

	public void run() {
		byte[] responseData;
		try {
			String inputPkt = new String(receivePacket.getData()).trim();
			String[] pktSplit = new String[2];
			if (inputPkt.contains("#")) {
				pktSplit = inputPkt.split("#");
				inputPkt = pktSplit[0];
			}
			switch (inputPkt) {
			case "TRANSFER_RECORD":
				System.out.println("Transferring :: " + pktSplit[1]);
				loggerInstance.log(Level.INFO, "Transferring :: " + pktSplit[1]);
				responseData = transferRecord(pktSplit[1]).getBytes();
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
				break;
			case "GET_RECORD_COUNT":
				responseData = Integer.toString(getRecCount()).getBytes();
				System.out.println("data in udp req server :: " + Integer.toString(getRecCount()));
				loggerInstance.log(Level.INFO, "data in udp req server :: " + Integer.toString(getRecCount()));
				serverSocket.send(new DatagramPacket(responseData, responseData.length, receivePacket.getAddress(),
						receivePacket.getPort()));
				break;
			default:
				System.out.println("Invalid UDP request type");
				loggerInstance.log(Level.INFO, "Invalid UDP request type");
			}

			//loggerInstance.log(Level.INFO, "Received " + inputPkt + " from " + location);
		} catch (Exception e) {
			// System.out.println(
			// "Exception in UDP Request server Thread :: " + e.getMessage());
		}
	}

	/**
	 * This method the functionality of adding the data to the respective
	 * server's hashmap (Transfer Functionality)
	 */
	private synchronized String transferRecord(String recordToBeAdded) {
		String temp[] = recordToBeAdded.split(",");
		String managerID = temp[0];
		String recordID = temp[1];
		if (recordID.contains("TR")) {
			String firstName = temp[2];
			String lastName = temp[3];
			String address = temp[4];
			String phone = temp[5];
			String specialization = temp[6];
			String location = temp[7];
			String key = lastName.substring(0, 1);
			Teacher teacherObj = new Teacher(managerID, recordID, firstName, lastName, address, phone, specialization,
					location);
			String message;
			List<Record> data;
			synchronized (mapLock) {
				message = server.addRecordToHashMap(key, teacherObj, null);
				data = server.recordsMap.get(key);
			}
			return message + " " + data;
		} else {
			String firstName = temp[2];
			String lastName = temp[3];
			String CoursesRegistered = temp[4];
			List<String> courseList = server.putCoursesinList(CoursesRegistered);
			String status = temp[3];
			String statusDate = temp[5];
			Student studentObj = new Student(managerID, recordID, firstName, lastName, courseList, status, statusDate);
			String key = lastName.substring(0, 1);

			String message;
			List<Record> data;
			synchronized (mapLock) {
				message = server.addRecordToHashMap(key, null, studentObj);
				data = server.recordsMap.get(key);
			}
			return message + " " + data;
		}
	}

	/**
	 * getRecCount is the function that returns the number of entries made in
	 * the respective hashmap
	 *
	 */

	private synchronized int getRecCount() {
		int count = 0;
		synchronized (mapLock) {
			for (Map.Entry<String, List<Record>> entry : server.recordsMap.entrySet()) {
				List<Record> list = entry.getValue();
				count += list.size();
			}
		}
		return count;
	}
}
