import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
/**
 * connection between server and clients. The thread reads a message from the in flux and echoes it in the out flux
 */
class Connection extends Thread {
	 DataInputStream in;
	 DataOutputStream out;
	 Socket clientSocket;
	 public Connection(Socket aClientSocket) {
		 try {
			 //create the input output flux
			 clientSocket = aClientSocket;
			 in = new DataInputStream(clientSocket.getInputStream());
			 out = new DataOutputStream(clientSocket.getOutputStream());
			 this.start();
		 } catch(IOException e) {
			 	System.out.println("Connection:"+e.getMessage());
		 	}
	 }
	 public void run() {
		 try { 
			 //The thread reads a message from the in flux and echoes it in the out flux
			 String data = in.readUTF();
			 out.writeUTF(data);
		 	} catch (EOFException e) {
		 		System.out.println("EOF:"+e.getMessage());
		 	} catch(IOException e) {
		 		System.out.println("readline:"+e.getMessage());
		 	} finally {
		 			try {
		 					clientSocket.close();
		 			}catch(IOException e) { /* close failed */ }
		 	}
	}
}