package Server.MainServer;

import Server.FrontEndConnection.DCMSServerFE;
import Util.Constants;
import Util.ServerLocations;

import org.omg.CosNaming.*;
import java.io.File;
import java.io.IOException;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import DCMSCorba.*;

/**
 * Creates the CORBA server instance for the current application and establishes
 * the initial set of communication between the client module and the server
 * module for performing various operations
 */
public class MainServer {
	static DCMSInterface dcmsInterfaceFE;
	static {
		try {
			Runtime.getRuntime().exec("orbd -ORBInitialPort 1050 -ORBInitialHost localhost");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("CORBA Service Started!");
	}

	/**
	 * Initialize and start the server instances Creates the orbd objects and
	 * performs the naming service Bind the Corba objects to establish connection to
	 * the client module
	 * 
	 * @param args[] - port number and IP address Corba server starts listening the
	 *               given port number and IP address
	 */
	public static void main(String args[]) {
		try {

			init();
			/* Initialize the ORB service with the respective arguments */
			ORB orb = ORB.init(args, null);

			/*
			 * Initialize and Activate the root POA Manager POA - Portable Object Adapter
			 */
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			/*
			 * As per the process implementations, create the Java object instances For the
			 * client, create servants to do the work
			 */
			DCMSServerFE serverFE = new DCMSServerFE();

			org.omg.CORBA.Object feRef = rootpoa.servant_to_reference(serverFE);

			dcmsInterfaceFE = DCMSInterfaceHelper.narrow(feRef);

			/* CORBA Naming Service */

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			/* Create Naming directory entries, which the client would use to resolve */

			NameComponent fePath[] = ncRef.to_name("FrontEnd");

			/* Rebind will bind the object reference to the given path */

			ncRef.rebind(fePath, dcmsInterfaceFE);
			System.out.println("DCMS Servers ready and waiting ...");
			orb.run();
		}

		catch (Exception e) {
			System.err.println("Exception in Server Main:: " + e);
			e.printStackTrace(System.out);
		}

	}

	/**
	 * In order to store the events and actions taking place this function creates
	 * and initializes the log directories in the server side One log directory per
	 * location to separate the events
	 */
	private static void init() {
		new File(Constants.LOG_DIR + "ServerFE").mkdir();
		new File(Constants.LOG_DIR + "ReplicasResponse").mkdir();

		new File(Constants.LOG_DIR + "PRIMARY_SERVER").mkdir();
		new File(Constants.LOG_DIR + "PRIMARY_SERVER" + "\\" + ServerLocations.MTL.toString()).mkdir();
		new File(Constants.LOG_DIR + "PRIMARY_SERVER" + "\\" + ServerLocations.LVL.toString()).mkdir();
		new File(Constants.LOG_DIR + "PRIMARY_SERVER" + "\\" + ServerLocations.DDO.toString()).mkdir();

		new File(Constants.LOG_DIR + "REPLICA1_SERVER").mkdir();
		new File(Constants.LOG_DIR + "REPLICA1_SERVER" + "\\" + ServerLocations.MTL.toString()).mkdir();
		new File(Constants.LOG_DIR + "REPLICA1_SERVER" + "\\" + ServerLocations.LVL.toString()).mkdir();
		new File(Constants.LOG_DIR + "REPLICA1_SERVER" + "\\" + ServerLocations.DDO.toString()).mkdir();

		new File(Constants.LOG_DIR + "REPLICA2_SERVER").mkdir();
		new File(Constants.LOG_DIR + "REPLICA2_SERVER" + "\\" + ServerLocations.MTL.toString()).mkdir();
		new File(Constants.LOG_DIR + "REPLICA2_SERVER" + "\\" + ServerLocations.LVL.toString()).mkdir();
		new File(Constants.LOG_DIR + "REPLICA2_SERVER" + "\\" + ServerLocations.DDO.toString()).mkdir();

	}
}