package Server.FrontEndConnection;

import java.util.*;
import java.util.logging.Level;

import DCMSCorba.*;

import java.net.*;
import Models.Record;
import Server.Implementation.*;
import Util.Constants;
import Util.LogManager;
import Util.ServerLocations;
import Util.ServerOperations;

/**
 * 
 * DcmsServerImpl class includes all the server operations' implementations,
 * implements all the methods in the IDL interface Performs the necessary
 * operations and returns the result/acknowledgement back to the Client.
 *
 */

public class DCMSServerFE extends DCMSInterfacePOA {
	LogManager ackManager;
	private static LogManager logManager;
	String IPaddress;
	public HashMap<String, List<Record>> recordsMap;
	int studentCount = 0;
	int teacherCount = 0;
	String recordsCount;
	String location;
	Integer requestId;
	HashMap<Integer, String> requestBuffer;
	ArrayList<TransferReqToCurrentServer> requests;
	public static HashMap<Integer, TransferResponseToFE> responses;
	public static ArrayList<String> receivedResponses;
	public static HashMap<String, MainServerImpl> primaryServerMap, replica1ServerMap, replica2ServerMap;
	MultiCastReceiver primaryReceiver, replica1Receiver, replica2Receiver;
	ReplicaResponseReceiver replicaResponseReceiver;
	Object crlock = new Object();
	public static HashMap<Integer, HashMap<String, MainServerImpl>> centralRepository;
	MainServerImpl s1, s2, s3;
	MainServerImpl primaryMtlServer;
	MainServerImpl primaryLvlServer;
	MainServerImpl primaryDdoServer;
	static boolean s1_MTL_sender_isAlive = true;
	static boolean s2_MTL_sender_isAlive = true;
	static boolean s3_MTL_sender_isAlive = true;
	static boolean s1_LVL_sender_isAlive = true;
	static boolean s2_LVL_sender_isAlive = true;
	static boolean s3_LVL_sender_isAlive = true;
	static boolean s1_DDO_sender_isAlive = true;
	static boolean s2_DDO_sender_isAlive = true;
	static boolean s3_DDO_sender_isAlive = true;

	static int TIME_OUT = 1000;
	static int LEADER_ID = 100;

	static int S1_ID = 1;
	static int S2_ID = 2;
	static int S3_ID = 3;

	static Object mapAccessor = new Object();
	static HashMap<String, Integer> currentIds = new HashMap<>();
	public static HashMap<String, Boolean> server_leader_status = new HashMap<>();
	public static HashMap<String, Long> server_last_updated_time = new HashMap<>();

	int s1_MTL_receive_port = 5431;
	int s2_MTL_receive_port = 5432;
	int s3_MTL_receive_port = 5433;
	int s1_LVL_receive_port = 5441;
	int s2_LVL_receive_port = 5442;
	int s3_LVL_receive_port = 5443;
	int s1_DDO_receive_port = 5451;
	int s2_DDO_receive_port = 5452;
	int s3_DDO_receive_port = 5453;

	String MTLserverName1 = "MTL1";
	String MTLserverName2 = "MTL2";
	String MTLserverName3 = "MTL3";
	String LVLserverName1 = "LVL1";
	String LVLserverName2 = "LVL2";
	String LVLserverName3 = "LVL3";
	String DDOserverName1 = "DDO1";
	String DDOserverName2 = "DDO2";
	String DDOserverName3 = "DDO3";
	public static BackupWriter S1_MTL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "MTL1_backup.txt");
	public static BackupWriter S2_MTL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "MTL2_backup.txt");
	public static BackupWriter S3_MTL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "MTL3_backup.txt");
	public static BackupWriter S1_LVL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "LVL1_backup.txt");
	public static BackupWriter S2_LVL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "LVL2_backup.txt");
	public static BackupWriter S3_LVL = new BackupWriter(
			Constants.BACKUP_DIR + "" + "LVL3_backup.txt");
	public static BackupWriter S1_DDO = new BackupWriter(
			Constants.BACKUP_DIR + "" + "DDO1_backup.txt");
	public static BackupWriter S2_DDO = new BackupWriter(
			Constants.BACKUP_DIR + "" + "DDO2_backup.txt");
	public static BackupWriter S3_DDO = new BackupWriter(
			Constants.BACKUP_DIR + "" + "DDO3_backup.txt");

	/**
	 * DcmsServerImpl Constructor to initializes the variables used for the
	 * implementation
	 * 
	 */
	public DCMSServerFE() {
		logManager = new LogManager("ServerFE");

		recordsMap = new HashMap<>();
		requests = new ArrayList<>();
		responses = new HashMap<>();
		requestBuffer = new HashMap<>();
		receivedResponses = new ArrayList<>();
		DCMSServerPrimaryFIFO udpReceiverFromFE = new DCMSServerPrimaryFIFO(requests);
		udpReceiverFromFE.start();
		UDPResponseReceiver udpResponse = new UDPResponseReceiver(responses);
		udpResponse.start();

		centralRepository = new HashMap<>();
		primaryServerMap = new HashMap<>();
		replica1ServerMap = new HashMap<>();
		replica2ServerMap = new HashMap<>();
		requestId = 0;

		server_leader_status.put(MTLserverName1, true);
		server_leader_status.put(LVLserverName1, true);
		server_leader_status.put(DDOserverName1, true);
		server_leader_status.put(MTLserverName2, false);
		server_leader_status.put(LVLserverName2, false);
		server_leader_status.put(DDOserverName2, false);
		server_leader_status.put(MTLserverName3, false);
		server_leader_status.put(LVLserverName3, false);
		server_leader_status.put(DDOserverName3, false);

		currentIds.put(MTLserverName1, LEADER_ID);
		currentIds.put(MTLserverName2, S2_ID);
		currentIds.put(MTLserverName3, S3_ID);
		currentIds.put(LVLserverName1, LEADER_ID);
		currentIds.put(LVLserverName2, S2_ID);
		currentIds.put(LVLserverName3, S3_ID);
		currentIds.put(DDOserverName1, LEADER_ID);
		currentIds.put(DDOserverName2, S2_ID);
		currentIds.put(DDOserverName3, S3_ID);

		server_last_updated_time.put(MTLserverName1, System.nanoTime() / 1000000);
		server_last_updated_time.put(MTLserverName2, System.nanoTime() / 1000000);
		server_last_updated_time.put(MTLserverName3, System.nanoTime() / 1000000);
		server_last_updated_time.put(LVLserverName1, System.nanoTime() / 1000000);
		server_last_updated_time.put(LVLserverName2, System.nanoTime() / 1000000);
		server_last_updated_time.put(LVLserverName3, System.nanoTime() / 1000000);
		server_last_updated_time.put(DDOserverName1, System.nanoTime() / 1000000);
		server_last_updated_time.put(DDOserverName2, System.nanoTime() / 1000000);
		server_last_updated_time.put(DDOserverName3, System.nanoTime() / 1000000);

		init();
	}

	private static LogManager getLogInstance(String serverName, ServerLocations loc) {
		LogManager logger = new LogManager(serverName, loc.toString());
		return logger;
	}

	public void init() {
		try {

			ArrayList<Integer> replicas = new ArrayList<>();
			replicas.add(Constants.REPLICA1_SERVER_ID);
			replicas.add(Constants.REPLICA2_SERVER_ID);
			boolean isPrimary = true;
			primaryReceiver = new MultiCastReceiver(isPrimary, ackManager);
			primaryReceiver.start();

			replicaResponseReceiver = new ReplicaResponseReceiver(new LogManager("ReplicasResponse"));
			replicaResponseReceiver.start();
			DatagramSocket socket1 = new DatagramSocket();

			primaryMtlServer = new MainServerImpl(Constants.PRIMARY_SERVER_ID, isPrimary, ServerLocations.MTL,
					9999, socket1, s1_MTL_sender_isAlive, MTLserverName1, s1_MTL_receive_port, s2_MTL_receive_port,
					s3_MTL_receive_port, replicas, getLogInstance("PRIMARY_SERVER", ServerLocations.MTL));

			primaryLvlServer = new MainServerImpl(Constants.PRIMARY_SERVER_ID, isPrimary, ServerLocations.LVL,
					7777, socket1, s1_LVL_sender_isAlive, LVLserverName1, s1_LVL_receive_port, s2_LVL_receive_port,
					s3_LVL_receive_port, replicas, getLogInstance("PRIMARY_SERVER", ServerLocations.LVL));

			primaryDdoServer = new MainServerImpl(Constants.PRIMARY_SERVER_ID, isPrimary, ServerLocations.DDO,
					6666, socket1, s1_DDO_sender_isAlive, DDOserverName1, s1_DDO_receive_port, s2_DDO_receive_port,
					s3_DDO_receive_port, replicas, getLogInstance("PRIMARY_SERVER", ServerLocations.DDO));

			primaryServerMap.put("MTL", primaryMtlServer);
			primaryServerMap.put("LVL", primaryLvlServer);
			primaryServerMap.put("DDO", primaryDdoServer);

			replica1Receiver = new MultiCastReceiver(false, ackManager);
			replica1Receiver.start();

			DatagramSocket socket2 = new DatagramSocket();
			MainServerImpl replica1MtlServer = new MainServerImpl(Constants.REPLICA1_SERVER_ID, false,
					ServerLocations.MTL, 5555, socket2, s2_MTL_sender_isAlive, MTLserverName2, s2_MTL_receive_port,
					s1_MTL_receive_port, s3_MTL_receive_port, replicas,
					getLogInstance("REPLICA1_SERVER", ServerLocations.MTL));

			MainServerImpl replica1LvlServer = new MainServerImpl(Constants.REPLICA1_SERVER_ID, false,
					ServerLocations.LVL, 4444, socket2, s2_LVL_sender_isAlive, LVLserverName2, s2_LVL_receive_port,
					s1_LVL_receive_port, s3_LVL_receive_port, replicas,
					getLogInstance("REPLICA1_SERVER", ServerLocations.LVL));

			MainServerImpl replica1DdoServer = new MainServerImpl(Constants.REPLICA1_SERVER_ID, false,
					ServerLocations.DDO, 2222, socket2, s2_DDO_sender_isAlive, DDOserverName2, s2_DDO_receive_port,
					s1_DDO_receive_port, s3_DDO_receive_port, replicas,
					getLogInstance("REPLICA1_SERVER", ServerLocations.DDO));

			replica1ServerMap.put("MTL", replica1MtlServer);
			replica1ServerMap.put("LVL", replica1LvlServer);
			replica1ServerMap.put("DDO", replica1DdoServer);

			DatagramSocket socket3 = new DatagramSocket();
			MainServerImpl replica2MtlServer = new MainServerImpl(Constants.REPLICA2_SERVER_ID, false,
					ServerLocations.MTL, 9878, socket3, s3_MTL_sender_isAlive, MTLserverName3, s3_MTL_receive_port,
					s1_MTL_receive_port, s2_MTL_receive_port, replicas,
					getLogInstance("REPLICA2_SERVER", ServerLocations.MTL));

			MainServerImpl replica2LvlServer = new MainServerImpl(Constants.REPLICA2_SERVER_ID, false,
					ServerLocations.LVL, 9701, socket3, s3_LVL_sender_isAlive, LVLserverName3, s3_LVL_receive_port,
					s1_LVL_receive_port, s2_LVL_receive_port, replicas,
					getLogInstance("REPLICA2_SERVER", ServerLocations.LVL));

			MainServerImpl replica2DdoServer = new MainServerImpl(Constants.REPLICA2_SERVER_ID, false,
					ServerLocations.DDO, 5655, socket3, s3_DDO_sender_isAlive, DDOserverName3, s3_DDO_receive_port,
					s1_DDO_receive_port, s2_DDO_receive_port, replicas,
					getLogInstance("REPLICA2_SERVER", ServerLocations.DDO));

			replica2ServerMap.put("MTL", replica2MtlServer);
			replica2ServerMap.put("LVL", replica2LvlServer);
			replica2ServerMap.put("DDO", replica2DdoServer);

			synchronized (centralRepository) {
				centralRepository.put(Constants.PRIMARY_SERVER_ID, primaryServerMap);
				centralRepository.put(Constants.REPLICA1_SERVER_ID, replica1ServerMap);
				centralRepository.put(Constants.REPLICA2_SERVER_ID, replica2ServerMap);
			}

			Thread thread1 = new Thread() {
				public void run() {
					while (getStatus(MTLserverName1)) {
						primaryMtlServer.send();
					}
				}
			};
			Thread thread2 = new Thread() {
				public void run() {
					while (getStatus(MTLserverName2)) {
						replica1MtlServer.send();
					}
				}
			};
			Thread thread3 = new Thread() {
				public void run() {
					while (getStatus(MTLserverName3)) {
						replica2MtlServer.send();
					}
				}
			};
			Thread thread4 = new Thread() {
				public void run() {
					while (getStatus(LVLserverName1)) {
						primaryLvlServer.send();
					}
				}
			};
			Thread thread5 = new Thread() {
				public void run() {
					while (getStatus(LVLserverName2)) {
						replica1LvlServer.send();
					}
				}
			};
			Thread thread6 = new Thread() {
				public void run() {
					while (getStatus(LVLserverName3)) {
						replica2LvlServer.send();
					}
				}
			};
			Thread thread7 = new Thread() {
				public void run() {
					while (getStatus(DDOserverName1)) {
						primaryDdoServer.send();
					}
				}
			};
			Thread thread8 = new Thread() {
				public void run() {
					while (getStatus(DDOserverName2)) {
						replica1DdoServer.send();
					}
				}
			};
			Thread thread9 = new Thread() {
				public void run() {
					while (getStatus(DDOserverName3)) {
						replica2DdoServer.send();
					}
				}
			};
			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
			thread5.start();
			thread6.start();
			thread7.start();
			thread8.start();
			thread9.start();

			Thread statusChecker = new Thread() {
				public void run() {
					while (true) {
						checkServerStatus("MTL1");
						checkServerStatus("MTL2");
						checkServerStatus("MTL3");
						checkServerStatus("LVL1");
						checkServerStatus("LVL2");
						checkServerStatus("LVL3");
						checkServerStatus("DDO1");
						checkServerStatus("DDO2");
						checkServerStatus("DDO3");
					}
				}
			};

			statusChecker.start();
		} catch (Exception e) {

		}
	}

	/**
	 * Once the teacher record is created, createTRRecord function returns the
	 * record ID of the teacher record created to the client
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param teacherField
	 *            values of the teacher attribute concatenated by the comma
	 *            which are received from the client
	 * 
	 */

	@Override
	public String createTRecord(String managerID, String teacher) {
		teacher = ServerOperations.CREATE_T_RECORD + Constants.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID)
				+ Constants.RECEIVED_DATA_SEPERATOR + managerID + Constants.RECEIVED_DATA_SEPERATOR + teacher;
		logManager.logger.log(Level.INFO, " Sending request to Server to create Teacher record: : " + teacher);
		return sendRequestToServer(teacher);
	}

	private String getServerLoc(String managerID) {
		return managerID.substring(0, 3);
	}

	/**
	 * Once the student record is created, the function createSRecord returns
	 * the record ID of the student record created to the client
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param studentFields
	 *            values of the student attribute concatenated by the comma
	 *            which are received the client
	 * 
	 */

	@Override
	public String createSRecord(String managerID, String student) {
		student = ServerOperations.CREATE_S_RECORD + Constants.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID)
				+ Constants.RECEIVED_DATA_SEPERATOR + managerID + Constants.RECEIVED_DATA_SEPERATOR + student;
		logManager.logger.log(Level.INFO, " Sending request to Server to create student record: : " + student);
		return sendRequestToServer(student);
	}

	/**
	 * Invokes record count request on MTL/LVL/DDO server to get record count
	 * from all the servers Creates UDPRequest Provider objects for each request
	 * and creates separate thread for each request. And makes sure each thread
	 * is complete and returns the result
	 * 
	 * @param managerID
	 *            gets the managerID
	 */

	@Override
	public String getRecordCount(String managerID) {
		String req = ServerOperations.GET_REC_COUNT + Constants.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID)
				+ Constants.RECEIVED_DATA_SEPERATOR + managerID;
		logManager.logger.log(Level.INFO, " Sending request to Server for getRecordCount: : " + req);
		return sendRequestToServer(req);
	}

	/**
	 * The edit record function performs the edit operation on the server and
	 * returns the appropriate message
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param recordID
	 *            gets the recordID to be edited
	 * @param fieldname
	 *            gets the fieldname to be edited for the given recordID
	 * @param newvalue
	 *            gets the newvalue to be replaced to the given fieldname from
	 *            the client
	 */

	@Override
	public String editRecord(String managerID, String recordID, String fieldname, String newvalue) {
		String editData = ServerOperations.EDIT_RECORD + Constants.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID)
				+ Constants.RECEIVED_DATA_SEPERATOR + managerID + Constants.RECEIVED_DATA_SEPERATOR + recordID
				+ Constants.RECEIVED_DATA_SEPERATOR + fieldname + Constants.RECEIVED_DATA_SEPERATOR + newvalue;
		logManager.logger.log(Level.INFO, " Sending request to Server for editRecord: : " + editData);
		return sendRequestToServer(editData);
	}

	/**
	 * Performs the transfer record to the remoteCenterServer by sending the
	 * appropriate packet to the DcmsServerUDPRequestProvider thread Creates
	 * UDPRequest Provider objects for each request and creates separate thread
	 * for each request. And makes sure each thread is complete and returns the
	 * result
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param recordID
	 *            gets the recordID to be edited
	 * @param remoteCenterServerName
	 *            gets the location to transfer the recordID from the client
	 */
	public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		String req = ServerOperations.TRANSFER_RECORD + Constants.RECEIVED_DATA_SEPERATOR + getServerLoc(managerID)
				+ Constants.RECEIVED_DATA_SEPERATOR + managerID + Constants.RECEIVED_DATA_SEPERATOR + recordID
				+ Constants.RECEIVED_DATA_SEPERATOR + remoteCenterServerName;
		logManager.logger.log(Level.INFO, " Sending request to Server for transferRecord: : " + req);
		return sendRequestToServer(req);
	}

	/**
	 * Performs the transfer of the request to the primary server by sending the
	 * appropriate packet request to request buffer and then waiting for the
	 * acknowledgement to the current server.
	 * 
	 * @param data
	 *            gets the data for the request from the client.
	 * 
	 */

	public String sendRequestToServer(String data) {
		try {
			requestId += 1;
			DatagramSocket ds = new DatagramSocket();
			data = data + Constants.RECEIVED_DATA_SEPERATOR + Integer.toString(requestId);
			byte[] dataBytes = data.getBytes();
			DatagramPacket dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName(Constants.CURRENT_SERVER_IP), Constants.CURRENT_SERVER_UDP_PORT);
			ds.send(dp);
			System.out.println("Adding request to request buffer with req id..." + requestId);
			logManager.logger.log(Level.INFO, "Adding request to request buffer with req id..." + requestId);
			requestBuffer.put(requestId, data);
			System.out.println("Waiting for acknowledgement from current server...");
			logManager.logger.log(Level.INFO, "Waiting for acknowledgement from current server...");
			Thread.sleep(Constants.RETRY_TIME);
			return getResponse(requestId);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return e.getMessage();
		}
	}

	/**
	 * Gets the requestId from the sendRequestToServer and removes the requestId
	 * from the buffer.
	 * 
	 * @param requestId
	 *            gets the requestId to be removed.
	 **/

	public String getResponse(Integer requestId) {
		try {
			responses.get(requestId).join();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
		requestBuffer.remove(requestId);
		return responses.get(requestId).getResponse();
	}

	/**
	 * Performs the server status by checking the server_last_updated_time with
	 * the current time to find out if the server has failed. If the failed
	 * server is the leader, electNewLeader used to find out the new leader.
	 * 
	 * @param serverName
	 *            gets the serverName to be checked for failure.
	 */

	private static synchronized void checkServerStatus(String serverName) {
		synchronized (mapAccessor) {
			long currentTime = System.nanoTime() / 1000000;
			if (server_last_updated_time.containsKey(serverName)) {
				if (currentTime - server_last_updated_time.get(serverName) > TIME_OUT) {
					if (server_leader_status.containsKey(serverName)) {
						if (server_leader_status.get(serverName)) {
							System.out.println(serverName + " Leader Failed Found!!!");
							logManager.logger.log(Level.INFO, serverName + " Leader Failed Found!!!");
							electNewLeader(serverName, logManager);
						}
					}
				}
			}
		}
	}

	/**
	 * Performs the election process to elect the new leader using bully
	 * election process after receiving the failed serverName from
	 * checkServerStatus, the elected leader will act as the primary server
	 * henceforth.
	 * 
	 * @param oldLeader
	 *            gets the name of the failed leader as oldLeader
	 * @param logManager
	 *            gets the LogManager instance to perform logging.
	 */

	private static String electNewLeader(String oldLeader, LogManager logManager) {
		server_leader_status.remove(oldLeader);
		server_last_updated_time.remove(oldLeader);
		currentIds.remove(oldLeader);
		String loc = oldLeader.substring(0, 3);
		Map.Entry<String, Integer> maxEntry = null;
		for (Map.Entry<String, Integer> entry : currentIds.entrySet()) {
			if (entry.getKey().contains(loc)) {
				if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
					maxEntry = entry;
				}
			}
		}
		server_leader_status.put(maxEntry.getKey(), true);
		currentIds.put(maxEntry.getKey(), LEADER_ID);
		logManager.logger.log(Level.INFO, "++++Elected new leader :: " + maxEntry.getKey() + " in the location" + loc);
		HashMap<String, MainServerImpl> replaceserver = new HashMap<String, MainServerImpl>();
		synchronized (centralRepository) {
			replaceserver = centralRepository.get(Constants.PRIMARY_SERVER_ID);
		}
		if (maxEntry.getKey().contains("2")) {
			ArrayList<Integer> replicas = new ArrayList<>();
			replicas.add(Constants.REPLICA2_SERVER_ID);
			HashMap<String, MainServerImpl> getnewserver = new HashMap<String, MainServerImpl>();
			synchronized (centralRepository) {
				getnewserver = centralRepository.get(Constants.REPLICA1_SERVER_ID);
			}
			MainServerImpl newPrimary = getnewserver.get(loc);
			newPrimary.setPrimary(true);
			newPrimary.setReplicas(replicas);
			newPrimary.setServerID(Constants.PRIMARY_SERVER_ID);

			replaceserver.remove(loc);

			replaceserver.put(loc, newPrimary);
			synchronized (centralRepository) {
				centralRepository.put(Constants.PRIMARY_SERVER_ID, replaceserver);
			}
			getnewserver.remove(loc);
			synchronized (centralRepository) {
				centralRepository.put(Constants.REPLICA1_SERVER_ID, getnewserver);
			}
			HashMap<String, MainServerImpl> replicamap = centralRepository.get(Constants.REPLICA2_SERVER_ID);
			MainServerImpl replica = replicamap.get(loc);
			replica.setReplicas(replicas);
			replicamap.put(loc, replica);
			synchronized (centralRepository) {
				centralRepository.put(Constants.REPLICA2_SERVER_ID, replicamap);
			}

		} else if (maxEntry.getKey().contains("3")) {
			ArrayList<Integer> replicas = new ArrayList<>();
			replicas.add(Constants.REPLICA1_SERVER_ID);
			HashMap<String, MainServerImpl> getnewserver = centralRepository.get(Constants.REPLICA2_SERVER_ID);
			MainServerImpl newPrimary = getnewserver.get(loc);
			newPrimary.setPrimary(true);
			newPrimary.setReplicas(replicas);
			newPrimary.setServerID(Constants.PRIMARY_SERVER_ID);

			replaceserver.put(loc, newPrimary);
			synchronized (centralRepository) {
				centralRepository.put(Constants.PRIMARY_SERVER_ID, replaceserver);
			}
			getnewserver.remove(loc);
			synchronized (centralRepository) {
				centralRepository.put(Constants.REPLICA2_SERVER_ID, getnewserver);
			}
			HashMap<String, MainServerImpl> replicamap = centralRepository.get(Constants.REPLICA1_SERVER_ID);
			MainServerImpl replica = replicamap.get(loc);
			replica.setReplicas(replicas);
			replicamap.put(loc, replica);
			synchronized (centralRepository) {
				centralRepository.put(Constants.REPLICA1_SERVER_ID, replicamap);
			}
		}
//		synchronized (centralRepository) {
//			for (Map.Entry<Integer, HashMap<String, DcmsServerImpl>> entry : centralRepository.entrySet()) {
//				System.out.println("ID :: " + entry.getKey());
//				for (Map.Entry<String, DcmsServerImpl> entry1 : entry.getValue().entrySet()) {
//					System.out.println("LOC :: " + entry1.getKey() + " REF :: " + entry1.getValue());
//				}
//			}
//		}
		System.out.println("Elected new leader :: " + maxEntry.getKey() + " in the location" + loc);
		return "and elected new leader " + maxEntry.getKey() + " in the location" + loc;
	}

	/**
	 * Performs the status check for server with the given server name.
	 * 
	 * @param name
	 *            gets the name of the server to be checked.
	 * 
	 */

	private static boolean getStatus(String name) {
		if (name.equals("MTL1")) {
			return s1_MTL_sender_isAlive;
		} else if (name.equals("MTL2")) {
			return s2_MTL_sender_isAlive;
		} else if (name.equals("MTL3")) {
			return s3_MTL_sender_isAlive;
		} else if (name.equals("LVL1")) {
			return s1_LVL_sender_isAlive;
		} else if (name.equals("LVL2")) {
			return s2_LVL_sender_isAlive;
		} else if (name.equals("LVL3")) {
			return s3_LVL_sender_isAlive;
		} else if (name.equals("DDO1")) {
			return s1_DDO_sender_isAlive;
		} else if (name.equals("DDO2")) {
			return s2_DDO_sender_isAlive;
		} else if (name.equals("DDO3")) {
			return s3_DDO_sender_isAlive;
		}
		return false;
	}

	/**
	 * Performs the server kill, for the given server location
	 * 
	 * @param location
	 *            get the server location to be killed
	 * 
	 */

	@Override
	public String killServer(String location) {
		String msg = "";
		if (location.equals("MTL")) {
			if (s1_MTL_sender_isAlive && s2_MTL_sender_isAlive && s3_MTL_sender_isAlive) {
				s1_MTL_sender_isAlive = false;
				primaryMtlServer.heartBeatReceiver.setStatus(false);
				msg = "MTL1 Server is killed " + electNewLeader("MTL1", logManager);
			} else {
				msg = "Primary is already killed!!";
			}
		} else if (location.equals("LVL")) {
			if (s1_LVL_sender_isAlive && s2_LVL_sender_isAlive && s3_LVL_sender_isAlive) {
				s1_LVL_sender_isAlive = false;
				primaryLvlServer.heartBeatReceiver.setStatus(false);
				msg = "LVL1 Server is killed " + electNewLeader("LVL1", logManager);
			} else {
				msg = "Primary is already killed!!";
			}
		} else if (location.equals("DDO")) {
			if (s1_DDO_sender_isAlive && s2_DDO_sender_isAlive && s3_DDO_sender_isAlive) {
				s1_DDO_sender_isAlive = false;
				primaryDdoServer.heartBeatReceiver.setStatus(false);
				msg = "DDO1 Server is killed " + electNewLeader("DDO1", logManager);
			} else {
				msg = "Primary is already killed!!";
			}
		}
		return msg;
	}
}