package Client;

import java.util.logging.Level;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DCMSCorba.DCMSInterface;
import DCMSCorba.DCMSInterfaceHelper;
import Util.ServerLocations;

/**
 * Implementation code of Client class
 * 
 * ClientImp gets the inputs from the DcmsClient and sends them to the server to
 * perform the corresponding operations
 * 
 * Returns the Success/Failure message to the client for the corresponding operations.
 * 
 **/

public class FrontEndImpl {
	DCMSInterface serverLoc = null;
	static NamingContextExt ncRef = null;

	/**
	 * creates the client instance with
	 * 
	 * @param args
	 *            gets the port number and IP address and creates the ORB object
	 *            with it.
	 * @param location
	 *            gets the location of the client,based on the location the
	 *            appropriate server instance is called to perform the operation
	 *            requested by the manager.
	 * @param ManagerID
	 *            creates the log file with the managerID.
	 */
	FrontEndImpl(String[] args, ServerLocations location, String ManagerID) {
		try {
			/*
			 * Initialize the ORB service with the given input arguments Host
			 * name and port number
			 */
			ORB orb = ORB.init(args, null);
			/*
			 * Resolve the naming context reference to know which path to look
			 * into
			 */
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			/* Cast the naming context corba object reference to java object */
			ncRef = NamingContextExtHelper.narrow(objRef);
			/*
			 * Resolve the name and get the object reference to invoke servant
			 * methods
			 */
			if ((location == ServerLocations.MTL)|| (location == ServerLocations.LVL) 
					|| (location == ServerLocations.DDO)){
				serverLoc = DCMSInterfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));
			}
			
		} catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}

	/**
	 * If the teacher record is created, It Displays the record ID of the
	 * teacher record created on the server with the values given by the
	 * manager.
	 * 
	 * If the teacher record is not created it displays the message.
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param teacherField
	 *            values of the teacher attribute concatenated by the comma and
	 *            are sent to the server
	 * 
	 */
	public String createTRecord(String managerID, String teacherField) {
		String result = "";
		String teacherID = "";
		teacherID = serverLoc.createTRecord(managerID, teacherField);
		if (teacherID != null)
			result = "Teacher record is created and assigned with " + teacherID;
		else
			result = "Teacher record is not created";
		return result;
	}

	/**
	 * If the student record is created, It Displays the record ID of the
	 * student record created on the server with the values given by the
	 * manager.
	 * 
	 * If the teacher record is not created it displays the message.
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param studentFields
	 *            values of the student attribute concatenated by the comma and
	 *            are sent to the server
	 * 
	 */
	public String createSRecord(String managerID, String studentFields) {
		String result = "";
		String studentID = "";
		studentID = serverLoc.createSRecord(managerID, studentFields);
		if (studentID != null)
			result = "student record is created and assigned with " + studentID;
		else
			result = "student record is not created";
		return result;
	}

	/**
	 * Invokes record count on MTL, DDO and LVL servers to get record count on
	 * 
	 * Displays the record counts of all the servers as result
	 * 
	 */
	public String getRecordCounts(String managerID) {
		String count = "";
		count = serverLoc.getRecordCount(managerID);
		return count;
	}

	/**
	 * invokes edit record on the server return the appropriate message
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param recordID
	 *            gets the recordID to be edited
	 * @param location
	 *            gets the location to transfer the recordID
	 * 
	 */
	public String transferRecord(String ManagerID, String recordID, String location) {
		String message = "";
		message = serverLoc.transferRecord(ManagerID, recordID, location);
		System.out.println(message);
		return message;
	}

	/**
	 * invokes edit record on the server return the appropriate message
	 * 
	 * @param managerID
	 *            gets the managerID
	 * @param recordID
	 *            gets the recordID to be edited
	 * @param fieldname
	 *            gets the fieldname to be edited for the given recordID
	 * @param newvalue
	 *            gets the newvalue to be replaced to the given fieldname
	 * 
	 */
	public String editRecord(String managerID, String recordID, String fieldname, String newvalue) {
		String message = "";
		message = serverLoc.editRecord(managerID, recordID, fieldname, newvalue);
		return message;
	}

	public String killServer(String location) {
		String message = "";
		message = serverLoc.killServer(location);
		return message;
	}
}