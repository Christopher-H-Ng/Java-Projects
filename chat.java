import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chat {

	private ArrayList<String> iplist = new ArrayList<>();	//array list will be used later
	private ArrayList<Integer> portlist = new ArrayList<>();
	private ArrayList<Integer> idlist = new ArrayList<>();
	private ArrayList<Socket> socklist = new ArrayList<>();
	Socket sock;
	Socket clientSocket; 
	boolean loop = true;
	int counterid = 0;
	public static void main(String[] args) throws IOException  {

		String portarg = args[0]; 			// takes in the unix command line arguement and sets it to port String
		final int port = Integer.parseInt(portarg);
		Chat p2pchat = new Chat();
		
		p2pchat.Client(port);
		p2pchat.Server(port);
	}

	private void Client(int port) {
		new Thread(){				//client thread which enables us to use commands to do what we want.
			@Override
			public void run() {
				Scanner kb = new Scanner(System.in);
				boolean loop = true;

				while (loop) {
					System.out.println("Enter action. Type 'help' for syntax manual...");
					System.out.print("User>>");
					String userinput = kb.nextLine();
					String[] astring = userinput.split(" "); // Splits user input into an array which is similar to a tokenizer function. 
					// will be able to be used later to verify enough arguments have been entered

					if (astring[0].toLowerCase().equals("help")){			// moved below to clear space for current work
						help();
					}	
					else if (astring[0].toLowerCase().equals("myip")){		// moved below to clear space for current work
						myip();
					}	
					else if (astring[0].toLowerCase().equals("myport")){	// moved below to clear space for current work
						myport(port);
					}	
					else if (astring[0].toLowerCase().equals("connect")){
						if (astring.length >= 3){												// message comments
							boolean connectTest = true;
							String newastring = "/"+astring[1];
							for (int i = 0; i < iplist.size(); i++){
								if (newastring.equals(iplist.get(i)) && Integer.parseInt(astring[2]) == portlist.get(i)) {									System.out.print("User already exists. \nUser>>");
									connectTest = false;
								}
							}
							if(connectTest){
								try{
									sock = new Socket(astring[1], Integer.parseInt(astring[2]));	// code is working well. to use open up Server.java client, get the port numbner then 
									RecieveThread recieveThread = new RecieveThread(sock);
									Thread thread = new Thread(recieveThread);
									thread.start();
									socklist.add(sock);
									System.out.println();
									System.out.println("The connection to peer " + astring[1] + " to port " + astring[2] +  " successfully established with ID# " + counterid);		// not final product, just there to test string verification
									System.out.println();
								} catch (Exception e) {
								System.out.println(e.getMessage());
								}
							}	
						}
						else {
							System.out.println();
							System.out.println("Missing destination and/or port number.");
							System.out.println();
						}
					}	
					else if (astring[0].toLowerCase().equals("send")){						// not working 100% but is working. must be used only after connection is made by
						if (astring.length >= 3){											// connect command. after connect command works correct use send by
																							// "send doesn't matter here this is the test message that is sent to the server"
							int intID = Integer.parseInt(astring[1]);
							if(!idlist.contains(intID))
								System.out.println("Invalid ID. ID does not exist.");
							else{
								String message = "";
								for (int i = 2; i < astring.length; i++)
									message += astring[i] + " ";
								Socket thisSock = socklist.get(intID);
								System.out.println();
								System.out.println("Sending to ID: " + astring[1] + " and the message is " + message);		// not final product, just there to test string verification
								System.out.println();
								SendThread sendThread = new SendThread(thisSock, message);		// here is where it is able to send the socket and message arguments to the send thread function
								Thread tread = new Thread(sendThread);
								tread.start();
								System.out.println("Message sent to "+ thisSock.getInetAddress() + " on port " + thisSock.getPort());
							}
						}else {
							System.out.println();
							System.out.println("Missing Send connectionid and/or message");
							System.out.println();
						}
					}	
					else if (astring[0].toLowerCase().equals("list")){						// not working 100% correctly, only list the connections in which you 
						System.out.println();												// not server connections

						if (iplist.isEmpty() && portlist.isEmpty() && idlist.isEmpty()) {
							System.out.println("List is currently empty. There are no connections.");
						}
						else {
							System.out.println("Current Connections:");	
							System.out.println();
							System.out.println("ID:  IP address		Port No.");
							for (int i = 0; i < idlist.size(); i++){
								System.out.println(" " + idlist.get(i) + ":  " + iplist.get(i).substring(1) + "	" + portlist.get(i));
							}							
						}
						System.out.println();

					}
					else if (astring[0].toLowerCase().equals("terminate")){					// works but only terminates the connect you made as a client, dont know
						if (astring.length >= 2){											// how to terminate as a server yet
							System.out.println();
							
							System.out.println();
							
							int TerminateID = Integer.parseInt(astring[1]);
							
							if (idlist.contains(TerminateID)) {
								int terminateIDIndex = idlist.indexOf(TerminateID);
								Socket thissock = socklist.get(terminateIDIndex);
								try {
									thissock.close();
									iplist.remove(terminateIDIndex);
									portlist.remove(terminateIDIndex);
									idlist.remove(terminateIDIndex);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							else {
								System.out.println("Error: Invalid id number");
							}			

						}else {
							System.out.println();
							System.out.println("Missing terminate connectionid");
							System.out.println();
						}
					}	
					else if (astring[0].toLowerCase().equals("exit")){
						loop = false;
						System.out.println();
						System.out.println("Thank you for using this program.");
						System.out.println();
						try {
							int index = socklist.indexOf(sock);
//							socklist.remove(index);
//							idlist.remove(index);
//							portlist.remove(index);
//							iplist.remove(index);
							for(Socket s: socklist){
								s.close();
							}
							sock.close();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							System.out.println("Exiting program. Disconnecting from users.");
						}
							System.exit(1);
					}
					else {
						System.out.println();
						System.out.println("Invalid input. Please try again.");
						System.out.println();
					}
				}
			}
		}.start();
	}

	private void Server(int port) throws IOException {		
		System.out.println("Server waiting for connection on port " + port);
		ServerSocket ss = new ServerSocket(port);		
		while (loop) {		
			clientSocket = ss.accept();
			new Thread(new SocketThread(clientSocket)).start();
			System.out.println("The connection to peer " + clientSocket.getInetAddress() + " on port "
					+ clientSocket.getLocalPort() + " is successfully established");
		}

	}

	public class SocketThread implements Runnable {
		
		private Socket socket;

		public SocketThread(Socket socket) {
			this.socket = socket;
			iplist.add(socket.getInetAddress().toString());
			portlist.add(socket.getPort());
			idlist.add(counterid);
			counterid++;
			System.out.println("counterid = " + counterid);
			socklist.add(socket);
		}
		@Override
		public void run() {
			RecieveFromClientThread recieve = new RecieveFromClientThread(clientSocket);
			Thread thread = new Thread(recieve);
			thread.start();
		}
	}

	private static void help(){
		System.out.println("-->help - Display information about the available user interface options or command manual.");
		System.out.println();
		System.out.println("-->myip - Display the IP address of this process.");
		System.out.println();
		System.out.println("-->myport - Display the port on which this process is listening for incoming connections.");
		System.out.println();
		System.out.println("-->connect <destination> <port no> - This command establishes a new TCP connection to the specified "
				+ "<destination> at the specified < port no>.");
		System.out.println();
		System.out.println("-->terminate <connection id> - This command will terminate the connection listed under the specified "
				+ "number when LIST is used to display all connections");
		System.out.println();
		System.out.println("-->send <connection id.> <message> - This will send the message to the host on the connection that "
				+ "is designated by the number 3 when command “list” is used.");
		System.out.println();
		System.out.println("-->exit - Close all connections and terminate this process. The other peers should also update their "
				+ "connection list by removing the peer that exits");		
		System.out.println();		
	}

	private static void myip(){
		try {
			System.out.println();
			System.out.println("The IP address is: " + InetAddress.getLocalHost().getHostAddress());
			System.out.println();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // displays ip address
	}

	private static void myport(int port){
		System.out.println();
		System.out.println("The program runs on port number: " + port);	// displays port number that was entered through the command line arguement
		System.out.println();
	}


	class RecieveFromClientThread implements Runnable 
	{
		Socket clientSocket = null;
		BufferedReader brBufferedReader = null;

		public RecieveFromClientThread(Socket clientSocket) 
		{
			this.clientSocket = clientSocket;
		}// end constructor

		public void run() 
		{
			try 
			{
				brBufferedReader = new BufferedReader(new InputStreamReader(
						this.clientSocket.getInputStream()));
				String messageString;
				while (true) 
				{
					while ((messageString = brBufferedReader.readLine()) != null)
					{ // assign message from client to messageString
						if (messageString.equals("EXIT")) 
						{
							System.out.println("Peer " + clientSocket.getInetAddress() + " terminates the connection");
						}
						else
						{
							System.out.println();
							System.out.println("Message received from " + clientSocket.getInetAddress());
							System.out.println("Sender's Port: " + clientSocket.getLocalPort());
							System.out.println("Message: " + messageString);// print
							System.out.println();
							System.out.println("Enter action. Type 'help' for syntax manual...");
							System.out.print("User>>");
							System.out.println();
						}
						
						// the
						// message
						// from
						// client
					}
					this.clientSocket.close();
				}
			} catch (Exception ex) 
			{
				System.out.println(ex.getMessage());
			}
		}
	}// end class RecieveFromClientThread

	class SendThread implements Runnable 
	{
		Socket sock = null;
		PrintWriter print = null;
		BufferedReader brinput = null;
		String msgtoServerString = null;

		public SendThread(Socket sock, String message) 
		{
			this.sock = sock;
			this.msgtoServerString = message;
		}// end constructor

		public void run() 
		{
			try 
			{
				if (sock.isConnected()) 
				{
					this.print = new PrintWriter(sock.getOutputStream(), true);
					this.print.println(msgtoServerString);
					this.print.flush();
				}
			} catch (Exception e) 
			{
				System.out.println(e.getMessage());
			}
		}// end run method
	}// end class

	class RecieveThread  implements Runnable 
	{
		Socket sock = null;
		BufferedReader recieve = null;

		public RecieveThread(Socket sock) 
		{
			this.sock = sock;
			iplist.add(sock.getInetAddress().toString());
			portlist.add(sock.getPort());
			idlist.add(counterid);
			counterid++;
			//System.out.println("ADDING PORT " + sock.getPort() + " AND IP " + sock.getInetAddress().toString());
		}// end constructor

		public void run() 
		{
			try 
			{
				recieve = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));// get inputstream
				String msgRecieved = null;
				while ((msgRecieved = recieve.readLine()) != null) 
				{
					System.out.println();
					System.out.println("Message received from " + sock.getInetAddress());
					System.out.println("Sender's Port: " + sock.getPort());
					System.out.println("Message: " + msgRecieved);// print
					System.out.println();
					System.out.println("Enter action. Type 'help' for syntax manual...");
					System.out.print("User>>");
					System.out.println();
				}
			} catch (Exception e) 
			{
				System.out.println(e.getMessage());
			}
		}// end run
	}// end class recievethread
}

