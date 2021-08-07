package Server.Implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

import Server.FrontEndConnection.DCMSServerFE;

public class HeartBeatReceiver extends Thread {

	DatagramSocket ds = null;
	String name;
	boolean isAlive;
	Object mapAccessor;

	/*
	 * Heart beat receiver to keep checking the other servers' status
	 */
	public HeartBeatReceiver(boolean isAlive, String name, int port, Logger logger) {
		try {
			this.isAlive = isAlive;
			this.name = name;
			System.out.println(name + "listening in :: " + port);
			ds = new DatagramSocket(port);
			mapAccessor = new Object();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		byte[] data = new byte[100];
		while (getStatus()) {
			try {
				DatagramPacket dp = new DatagramPacket(data, data.length);
				ds.receive(dp);
				synchronized (mapAccessor) {
					// System.out.println("with time
					// "+System.nanoTime()/1000000+"In "+this.name+" Received
					// data "+new String(dp.getData()));
					DCMSServerFE.server_last_updated_time.put(name, System.nanoTime() / 1000000);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Sets the status of the receiver to false
	 * to kill the present server and its communication with
	 * other servers
	 */
	public void setStatus(boolean value) {
		isAlive = value;
	}

	private boolean getStatus() {
		return isAlive;
	}
}
