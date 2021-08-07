package Server.Implementation;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import DCMSCorba.*;
import Models.Record;
import Models.Student;
import Models.Teacher;
import Server.FrontEndConnection.DCMSServerFE;
import Util.Constants;
import Util.LogManager;
import Util.ServerLocations;

/**
 * 
 * DcmsServerImpl class includes all the server operations' implementations,
 * implements all the methods in the IDL interface Performs the necessary
 * operations and returns the result/acknowledgement back to the Client.
 *
 */

public class MainServerImpl extends DCMSInterfacePOA {
	private LogManager logManager;
	public HashMap<String, List<Record>> recordsMap;
	public HeartBeatReceiver heartBeatReceiver;
	public ArrayList<Integer> replicas;

	UDPPacketReceiver dcmsServerUDPReceiver;
	String IPaddress;
	Object recordsMapAccessorLock = new Object();

	int studentCount = 10000;
	int teacherCount = 10000;
	String recordsCount;
	String location;
	int locUDPPort = 0;
	boolean isPrimary;
	Integer serverID = 0;

	HeartBeatSender heartBeatSender;
	String name;
	int port1, port2;
	boolean isAlive;
	DatagramSocket ds = null;

	public int getlocUDPPort() {
		return this.locUDPPort;
	}

	/*
	 * DcmsServerImpl Constructor to initializes the variables used for the
	 * implementation
	 * 
	 * @param loc The server location for which the server implementation should
	 * be initialized
	 */
	public MainServerImpl(int serverID, boolean isPrimary, ServerLocations loc, int locUDPPort, DatagramSocket ds,
			boolean isAlive, String name, int receivePort, int port1, int port2, ArrayList<Integer> replicas,
			LogManager logger) {
		logManager = logger;
		synchronized (recordsMapAccessorLock) {
			recordsMap = new HashMap<>();
		}
		this.locUDPPort = locUDPPort;
		dcmsServerUDPReceiver = new UDPPacketReceiver(true, locUDPPort, loc, logManager.logger, this);
		dcmsServerUDPReceiver.start();
		location = loc.toString();
		this.isPrimary = isPrimary;
		this.serverID = serverID;
		this.name = name;
		this.port1 = port1;
		this.port2 = port2;
		this.isAlive = isAlive;
		heartBeatReceiver = new HeartBeatReceiver(isAlive, name, receivePort, logManager.logger);
		heartBeatReceiver.start();
		this.ds = ds;
		this.replicas = replicas;
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
	public synchronized String createTRecord(String managerID, String teacher) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				GenerateReplicasRequest req = new GenerateReplicasRequest(replicaId,
						logManager.logger);
				req.createTRecord(managerID, teacher);
			}
		}
		String temp[] = teacher.split(":");
		String teacherID = "TR" + (++teacherCount);
		String firstName = temp[0];
		String lastname = temp[1];
		String address = temp[2];
		String phone = temp[3];
		String specialization = temp[4];
		String location = temp[5];
		String requestID = temp[6];
		Teacher teacherObj = new Teacher(managerID, teacherID, firstName, lastname, address, phone, specialization,
				location);
		String key = lastname.substring(0, 1);
		String message = addRecordToHashMap(key, teacherObj, null);
		if (message.equals("success")) {
			System.out.println("teacher is added " + teacherObj + " with this key " + key + " by Manager " + managerID
					+ " for the request ID: " + requestID);
			logManager.logger.log(Level.INFO, "Teacher record created " + teacherID + " by Manager : " + managerID
					+ " for the request ID: " + requestID);
		} else {
			logManager.logger.log(Level.INFO, "Error in creating T record" + requestID);
			return "Error in creating T record";
		}

		return teacherID;

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
	public synchronized String createSRecord(String managerID, String student) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				GenerateReplicasRequest req = new GenerateReplicasRequest(replicaId,
						logManager.logger);
				req.createSRecord(managerID, student);
			}
		}
		String temp[] = student.split(":");
		String firstName = temp[0];
		String lastName = temp[1];
		String CoursesRegistered = temp[2];
		List<String> courseList = putCoursesinList(CoursesRegistered);
		String status = temp[3];
		String statusDate = temp[4];
		String requestID = temp[5];
		String studentID = "SR" + (++studentCount);
		Student studentObj = new Student(managerID, studentID, firstName, lastName, courseList, status, statusDate);
		String key = lastName.substring(0, 1);
		String message = addRecordToHashMap(key, null, studentObj);
		if (message.equals("success")) {
			System.out.println(" Student is added " + studentObj + " with this key " + key + " by Manager " + managerID
					+ " for the requestID " + requestID);
			logManager.logger.log(Level.INFO, "Student record created " + studentID + " by manager : " + managerID
					+ " for the requestID " + requestID);
		} else {
			return "Error in creating S record";
		}
		return studentID;
	}

	

	/**
	 *
	 * returns the current server record count
	 * 
	 */

	private synchronized int getCurrServerCnt() {
		int count = 0;
		synchronized (recordsMapAccessorLock) {
			for (Map.Entry<String, List<Record>> entry : this.recordsMap.entrySet()) {
				List<Record> list = entry.getValue();
				count += list.size();
			}
		}
		return count;
	}

	/**
	 * Invokes record count request on MTL/LVL/DDO server to get record count
	 * from all the servers Creates UDPRequest Provider objects for each request
	 * and creates separate thread for each request. And makes sure each thread
	 * is complete and returns the result
	 */

	@Override
	public synchronized String getRecordCount(String manager) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				GenerateReplicasRequest req = new GenerateReplicasRequest(replicaId,
						logManager.logger);
				req.getRecordCount(manager);
			}
		}
		String data[] = manager.split(Constants.RECEIVED_DATA_SEPERATOR);
		String managerID = data[0];
		String requestID = data[1];
		String recordCount = null;
		UDPRequestProvider[] req = new UDPRequestProvider[2];
		int counter = 0;
		ArrayList<String> locList = new ArrayList<>();
		locList.add("MTL");
		locList.add("LVL");
		locList.add("DDO");
		for (String loc : locList) {
			// System.out.println("11>>>>>>>>>>>>>>>>>>>>>>>>>>>Now serving
			// location :: " + loc);
			if (loc == this.location) {
				recordCount = loc + " " + getCurrServerCnt();
			} else {
				try {
					// System.out.println("22>>>>>>>>>>>>>>>>>>>>>>>>>>>Now
					// serving location :: " + loc);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Server id :: " + serverID);
					req[counter] = new UDPRequestProvider(
							DCMSServerFE.centralRepository.get(serverID).get(loc), "GET_RECORD_COUNT", null,
							logManager.logger);
				} catch (IOException e) {
					System.out.println("Exception in get rec count :: " + e.getMessage());
					logManager.logger.log(Level.SEVERE, e.getMessage());
				}
				req[counter].start();
				counter++;
			}
		}
		for (UDPRequestProvider request : req) {
			try {
				request.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			recordCount += " , " + request.getRemoteRecordCount().trim();
		}
		System.out.println(
				recordCount + " for the request ID " + requestID + " as requested by the managerID " + managerID);
		logManager.logger.log(Level.INFO,
				recordCount + " for the request ID " + requestID + " as requested by the managerID " + managerID);
		return recordCount;
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
	public synchronized String editRecord(String managerID, String recordID, String fieldname, String newvalue) {
		if (isPrimary) {
			for (Integer replicaId : replicas) {
				GenerateReplicasRequest req = new GenerateReplicasRequest(replicaId,
						logManager.logger);
				req.editRecord(managerID, recordID, fieldname, newvalue);
			}
		}
		String data[] = newvalue.split(Constants.RECEIVED_DATA_SEPERATOR);
		String requestID = data[1];
		String type = recordID.substring(0, 2);
		if (type.equals("TR")) {
			return editTRRecord(managerID, recordID, fieldname, newvalue);
		} else if (type.equals("SR")) {
			return editSRRecord(managerID, recordID, fieldname, newvalue);
		}
		logManager.logger.log(Level.INFO, "Record edit successful for the request ID " + requestID);
		return "Operation not performed!";
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
	public synchronized String transferRecord(String managerID, String recordID, String data) {

		if (isPrimary) {
			for (Integer replicaId : replicas) {
				GenerateReplicasRequest req = new GenerateReplicasRequest(replicaId,
						logManager.logger);
				req.transferRecord(managerID, recordID, data);
			}
		}
		String parsedata[] = data.split(Constants.RECEIVED_DATA_SEPERATOR);
		String remoteCenterServerName = parsedata[0];
		String requestID = parsedata[1];
		String type = recordID.substring(0, 2);
		UDPRequestProvider req = null;
		UDPRequestProvider req1 = null;
		try {
			Record record = getRecordForTransfer(recordID);
			if (record == null) {
				return "RecordID unavailable!";
			} else if (remoteCenterServerName.equals(this.location)) {
				return "Please enter a valid location to transfer. The record is already present in " + location;
			}
			req = new UDPRequestProvider(
					DCMSServerFE.centralRepository.get(serverID).get(remoteCenterServerName.trim()), "TRANSFER_RECORD",
					record, logManager.logger);

			if (isPrimary && this.replicas.size() == Constants.TOTAL_REPLICAS_COUNT - 1) {
				System.out.println("Replicas size is ::::::::::: 1" + remoteCenterServerName);
				req1 = new UDPRequestProvider(DCMSServerFE.centralRepository.get(Constants.REPLICA2_SERVER_ID)
						.get(remoteCenterServerName.trim()), "TRANSFER_RECORD", record, logManager.logger);
				req1.start();
				try {
					req1.join();
					backupAfterTransferRecord(Constants.REPLICA2_SERVER_ID, remoteCenterServerName);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			logManager.logger.log(Level.SEVERE, e.getMessage());
		}
		if (req != null) {
			req.start();
		}
		try {
			if (req != null) {
				req.join();
			}
			if (removeRecordAfterTransfer(recordID) == "success") {
				logManager.logger.log(Level.INFO, "Record created in  " + remoteCenterServerName + "  and removed from "
						+ location + " with requestID " + requestID);
				System.out.println("Record created in " + remoteCenterServerName + "and removed from " + location
						+ " with requestID " + requestID);
				takeTheBackup();
				backupAfterTransferRecord(this.serverID, remoteCenterServerName);
				return "Record created in " + remoteCenterServerName + "and removed from " + location;
			}
		} catch (Exception e) {
			System.out.println("Exception in transfer record :: " + e.getMessage());
		}

		return "Transfer record operation unsuccessful!";
	}

	/*
	 * Remove record after transfer method, removes the record from current
	 * server after the transfer operation is performed.
	 * 
	 * @param recordID record id of the student/teacher to be removed
	 */
	private synchronized String removeRecordAfterTransfer(String recordID) {
		synchronized (recordsMapAccessorLock) {
			for (Entry<String, List<Record>> element : recordsMap.entrySet()) {
				List<Record> mylist = element.getValue();
				for (int i = 0; i < mylist.size(); i++) {
					if (mylist.get(i).getRecordID().equals(recordID)) {
						mylist.remove(i);
					}
				}
				recordsMap.put(element.getKey(), mylist);
			}
			System.out.println("Removed record from " + this.location);
		}
		return "success";
	}

	/*
	 * Get record for transfer method gets the record from the hashmap given the
	 * record ID of the student/teacher
	 */
	private synchronized Record getRecordForTransfer(String recordID) {
		synchronized (recordsMapAccessorLock) {
			for (Entry<String, List<Record>> value : recordsMap.entrySet()) {
				List<Record> mylist = value.getValue();
				Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();
				if (recordID.contains("TR")) {
					if (record.isPresent())
						return (Teacher) record.get();
				} else {
					if (record.isPresent())
						return (Student) record.get();
				}
			}
		}
		return null;
	}

	/**
	 * The editSRRecord function performs the edit operation on the student
	 * record and returns the appropriate message
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

	private synchronized String editSRRecord(String maangerID, String recordID, String fieldname, String data) {
		String newdata[] = data.split(Constants.RECEIVED_DATA_SEPERATOR);
		String newvalue = newdata[0];
		String requestID = newdata[1];
		for (Entry<String, List<Record>> value : recordsMap.entrySet()) {
			List<Record> mylist = value.getValue();
			Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();
			if (record.isPresent()) {
				if (record.isPresent() && fieldname.equals("Status")) {
					((Student) record.get()).setStatus(newvalue);
					logManager.logger.log(Level.INFO, maangerID + " performed the operation with the requestID "
							+ requestID + " and Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with status :: " + newvalue;
				} else if (record.isPresent() && fieldname.equals("StatusDate")) {
					((Student) record.get()).setStatusDate(newvalue);
					logManager.logger.log(Level.INFO, maangerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with status date :: " + newvalue;
				} else if (record.isPresent() && fieldname.equals("CoursesRegistered")) {
					List<String> courseList = putCoursesinList(newvalue);
					((Student) record.get()).setCoursesRegistered(courseList);
					logManager.logger.log(Level.INFO, maangerID + " performed the operation with the requestID "
							+ requestID + "Updated the courses registered\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with courses :: " + courseList;
				} else {
					System.out.println("Record with " + recordID + " not found");
					logManager.logger.log(Level.INFO, "Record with " + recordID + "not found!" + location);
					return "Record with " + recordID + " not found";
				}
			}
		}
		return "Record with " + recordID + "not found!";
	}

	/**
	 * The editTRRecord function performs the edit operation on the Teacher
	 * record and returns the appropriate message
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

	private synchronized String editTRRecord(String managerID, String recordID, String fieldname, String data) {
		String newdata[] = data.split(Constants.RECEIVED_DATA_SEPERATOR);
		String newvalue = newdata[0];
		String requestID = newdata[1];
		for (Entry<String, List<Record>> val : recordsMap.entrySet()) {
			List<Record> mylist = val.getValue();
			Optional<Record> record = mylist.stream().filter(x -> x.getRecordID().equals(recordID)).findFirst();

			if (record.isPresent()) {
				if (record.isPresent() && fieldname.equals("Phone")) {
					((Teacher) record.get()).setPhone(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with Phone :: " + newvalue;
				}

				else if (record.isPresent() && fieldname.equals("Address")) {
					((Teacher) record.get()).setAddress(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with address :: " + newvalue;
				}

				else if (record.isPresent() && fieldname.equals("Location")) {
					((Teacher) record.get()).setLocation(newvalue);
					logManager.logger.log(Level.INFO, managerID + " performed the operation with the requestID "
							+ requestID + "Updated the records\t" + location);
					System.out.println("Record with recordID " + recordID + "update with new " + fieldname + " as "
							+ newvalue + " with requestID " + requestID);
					takeTheBackup();
					return "Updated record with location :: " + newvalue;
				} else {
					System.out.println("Record with " + recordID + " not found");
					logManager.logger.log(Level.INFO, "Record with " + recordID + "not found!" + location);
					return "Record with " + recordID + " not found";
				}
			}
		}
		return "Record with " + recordID + " not found";
	}
	
	
	/*
	 * Methods to access only from FE once the primary sever is killed.
	 */

	public void send() {
		heartBeatSender = new HeartBeatSender(ds, name, port1, port2);
		heartBeatSender.start();
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public ArrayList<Integer> getReplicas() {
		return replicas;
	}

	public void setReplicas(ArrayList<Integer> replicas) {
		this.replicas = replicas;
	}

	@Override
	public String killServer(String location) {
		return null;
	}

	public Integer getServerID() {
		return serverID;
	}

	public void setServerID(Integer serverID) {
		this.serverID = serverID;
	}

	/**
	 * The Function passes the respective HashMap to the DcmsServerBackupWriter
	 * class to store a written backup of the Repository
	 */

	public synchronized void takeTheBackup() {
		synchronized (recordsMapAccessorLock) {
			if (this.location.equalsIgnoreCase("MTL") && serverID == 1 && recordsMap.size() > 0) {
				DCMSServerFE.S1_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 1 && recordsMap.size() > 0) {
				DCMSServerFE.S1_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 1 && recordsMap.size() > 0) {
				DCMSServerFE.S1_DDO.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("MTL") && serverID == 2 && recordsMap.size() > 0) {
				DCMSServerFE.S2_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 2 && recordsMap.size() > 0) {
				DCMSServerFE.S2_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 2 && recordsMap.size() > 0) {
				DCMSServerFE.S2_DDO.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("MTL") && serverID == 3 && recordsMap.size() > 0) {
				DCMSServerFE.S3_MTL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("LVL") && serverID == 3 && recordsMap.size() > 0) {
				DCMSServerFE.S3_LVL.backupMap(this.recordsMap);
			} else if (this.location.equalsIgnoreCase("DDO") && serverID == 3 && recordsMap.size() > 0) {
				DCMSServerFE.S3_DDO.backupMap(this.recordsMap);
			}
		}
	}

	/**
	 * Takes the backup after transfer record operation is performed
	 * 
	 * @param remoteCenterServerName
	 */

	public void backupAfterTransferRecord(Integer serverID, String remoteCenterServerName) {
		synchronized (recordsMapAccessorLock) {
			HashMap<String, MainServerImpl> serverList = DCMSServerFE.centralRepository.get(serverID);
			MainServerImpl remoteServer = serverList.get(remoteCenterServerName);
			if (remoteServer != null) {
				remoteServer.takeTheBackup();
			}
		}
	}
	
	/**
	 * Adds the Teacher and Student to the HashMap the function
	 * addRecordToHashMap returns the success message, if the student / teacher
	 * record is created successfully else returns Error message
	 * 
	 * @param key
	 *            gets the key of the recordID stored in the HashMap
	 * @param teacher
	 *            gets the teacher object if received from createTRecord
	 *            function
	 * @param student
	 *            gets the student object if received from createSRecord
	 *            function which are received the respective functions.
	 * 
	 */

	public synchronized String addRecordToHashMap(String key, Teacher teacher, Student student) {
		String message = "Error";
		if (teacher != null) {
			List<Record> recordList = null;
			synchronized (recordsMapAccessorLock) {
				recordList = recordsMap.get(key);
			}
			if (recordList != null) {
				recordList.add(teacher);
			} else {
				List<Record> records = null;
				synchronized (recordsMapAccessorLock) {
					records = new ArrayList<Record>();
					records.add(teacher);
				}
				recordList = records;
			}
			synchronized (recordsMapAccessorLock) {
				recordsMap.put(key, recordList);
			}
			message = "success";
		}

		if (student != null) {
			List<Record> recordList = null;
			synchronized (recordsMapAccessorLock) {
				recordList = recordsMap.get(key);
			}
			if (recordList != null) {
				recordList.add(student);
			} else {
				List<Record> records = null;
				synchronized (recordsMapAccessorLock) {
					records = new ArrayList<Record>();
					records.add(student);
				}
				recordList = records;
			}
			synchronized (recordsMapAccessorLock) {
				recordsMap.put(key, recordList);
			}
			message = "success";
		}
		takeTheBackup();
		return message;
	}
	
	/**
	 * The putCoursesinList function adds the newCourses to the List
	 * 
	 * @param newvalue
	 *            gets the newcourses value and adds to the list
	 *
	 */

	public synchronized List<String> putCoursesinList(String newvalue) {
		String[] courses = newvalue.split("//");
		ArrayList<String> courseList = new ArrayList<>();
		for (String course : courses)
			courseList.add(course);
		return courseList;
	}
}