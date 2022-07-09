import java.net.*;
/**
 * Server.
 * Connect the socket to server port 6789
 * The server waits for connection requests
 * For each connection request, the server creates a thread responsible for the connection 
 */
import java.io.*;
public class SantaServer {
	 public static void main (String args[]) {
		 try {
			 //Connect the socket to server port 6789
			 int serverPort = 6789;
			 ServerSocket listenSocket = new ServerSocket(serverPort);
			//The server waits for connection requests
			 while (true) {
				 Socket clientSocket = listenSocket.accept();
				 Connection c = new Connection(clientSocket);
				 c.start();
			 }
			 // For each connection request, the server creates a thread responsible for the connection 
		 } catch(IOException e) {
			 System.out.println("Listen socket:"+e.getMessage());
		 }
	 }
}