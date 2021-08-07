package Server.FrontEndConnection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import Server.FrontEndConnection.DCMSServerFE;
import Server.Implementation.MainServerImpl;
import Util.Constants;
import Util.LogManager;
import Util.ServerLocations;
import Util.ServerOperations;

public class TransferReqToCurrentServer extends Thread {
	String currentOperationData;
	MainServerImpl server;
	String response;
	Logger loggerInstance;

	public TransferReqToCurrentServer(byte[] operationData, Logger loggerInstance) {
		this.currentOperationData = new String(operationData);
		this.server = null;
		response = null;
		this.loggerInstance = loggerInstance;
	}
	
	/**
	 * Performs the operation based on the type of requestID by 
	 * routing the request to the current server. 
	 * 
	 * After processing the data,
	 * The respective requestID and the response is sent to the Front End
	 */

	public void run() {
		String[] dataArr;
		String[] dataToBeSent = this.currentOperationData.trim().split(Constants.RECEIVED_DATA_SEPERATOR);
		ServerOperations oprn = ServerOperations.valueOf(dataToBeSent[0]);
		String requestId = dataToBeSent[dataToBeSent.length - 1];
		System.out.println("Currently serving request with id :: " + requestId);
	
		switch (oprn) {
		case CREATE_T_RECORD:
			System.out.println(dataToBeSent[1]);
			this.server = chooseServer(dataToBeSent[1]);
			dataArr = Arrays.copyOfRange(dataToBeSent, 3, dataToBeSent.length);
			String teacherData = String.join(Constants.RECEIVED_DATA_SEPERATOR, dataArr);
			response = this.server.createTRecord(dataToBeSent[2], teacherData);
			sendReply(requestId, response);
			break;
		case CREATE_S_RECORD:
			this.server = chooseServer(dataToBeSent[1]);
			dataArr = Arrays.copyOfRange(dataToBeSent, 3, dataToBeSent.length);
			String studentData = String.join(Constants.RECEIVED_DATA_SEPERATOR, dataArr);
			response = this.server.createSRecord(dataToBeSent[2], studentData);
			sendReply(requestId, response);
			break;
		case GET_REC_COUNT:
			this.server = chooseServer(dataToBeSent[1]);
			response = this.server
					.getRecordCount(dataToBeSent[2] + Constants.RECEIVED_DATA_SEPERATOR + dataToBeSent[3]);
			System.out.println("Received response here ::::::::::::::::::::::: " + response);
			sendReply(requestId, response);
			break;
		case EDIT_RECORD:
			this.server = chooseServer(dataToBeSent[1]);
			String newdata = dataToBeSent[5] + Constants.RECEIVED_DATA_SEPERATOR + dataToBeSent[6];
			response = this.server.editRecord(dataToBeSent[2], dataToBeSent[3], dataToBeSent[4], newdata);
			sendReply(requestId, response);
			break;
		case TRANSFER_RECORD:
			this.server = chooseServer(dataToBeSent[1]);
			String newdata1 = dataToBeSent[4] + Constants.RECEIVED_DATA_SEPERATOR + dataToBeSent[5];
			response = this.server.transferRecord(dataToBeSent[2], dataToBeSent[3], newdata1);
			sendReply(requestId, response);
			break;
		}
	}

	public String getResponse() {
		return response;
	}
	
	/**
	 * Retrieves the current server location based on the instance thread and returns it to the callee
	 * @param loc
	 * @return
	 */

	private synchronized MainServerImpl chooseServer(String loc) {
		return DCMSServerFE.centralRepository.get(Constants.PRIMARY_SERVER_ID).get(loc);
	}
	
	/**
	 * The function receives the request from the respective thread
	 * and forwards the message in the form of a datagram packet 
	 * to the front End
	 *  
	 * @param requestId
	 * @param response
	 */

	private void sendReply(String requestId, String response) {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket();
			response = response + Constants.RESPONSE_DATA_SEPERATOR + requestId;
			byte[] dataBytes = response.getBytes();
			DatagramPacket dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName(Constants.FRONT_END_IP), Constants.FRONT_END_UDP_PORT);
			ds.send(dp);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
