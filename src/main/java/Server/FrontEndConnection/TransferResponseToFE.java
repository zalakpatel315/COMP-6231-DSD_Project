package Server.FrontEndConnection;
public class TransferResponseToFE extends Thread {
	String response;
	public TransferResponseToFE(String response) {
		this.response = response;
	}
	
	public void run() {
		System.out.println("============="+this.response);
		DCMSServerFE.receivedResponses.add(this.response);
	}	
	/*
	 * Transfers the received response from primary server
	 * to the front end
	 */
	public String getResponse() {
		return response;
	}
}
