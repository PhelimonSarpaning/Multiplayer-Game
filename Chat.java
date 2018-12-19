import java.net.*;
import java.io.*;
import java.util.Date;
import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.HashMap;


class Chat
{   static int player_id = 0;
	static int new_x=0;
	static int new_y=0;
    static int new_tx=0;
    static HashMap<Integer, String> message = new HashMap<Integer,String>();
    static int player_one = 0;
    static int player_two = 0;
		
	static String getServerTime()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}

	static void sendLine(PrintWriter out, String line)
	{
		out.print(line); // Send over the socket
		out.print("\r\n");
		System.out.println(line); // Print it to the console too, just to make debugging easier
	}

	static void onGet(OutputStream os, String url) throws Exception
	{
		PrintWriter out = new PrintWriter(os, true);
		String filename = url.substring(1); // cut off the initial "/"
		File f = new File(filename);
		Path path = Paths.get(filename);
		String dateString = getServerTime();
		System.out.println("----------The server replied: ----------");
		if(f.exists() && !f.isDirectory())
		{
			// Read the file from disk
			byte[] fileContents = Files.readAllBytes(path);

			// Send the headers
			sendLine(out, "HTTP/1.1 200 OK");
			sendLine(out, "Content-Type: " + Files.probeContentType(path));
			sendLine(out, "Content-Length: " + Integer.toString(fileContents.length));
			sendLine(out, "Date: " + dateString);
			sendLine(out, "Last-Modified: " + dateString);
			sendLine(out, "Connection: close");
			sendLine(out, "");
			out.flush();

			// Send the payload
			os.write(fileContents);
			String blobHead = fileContents.length < 60 ? new String(fileContents) : new String(fileContents, 0, 60) + "...";
			System.out.println(blobHead);
		}
		else
		{
			// Make an error message
			String payload = "404 - File not found: " + filename;

			// Send HTTP headers
			sendLine(out, "HTTP/1.1 200 OK");
			sendLine(out, "Content-Type: text/html");
			sendLine(out, "Content-Length: " + Integer.toString(payload.length()));
			sendLine(out, "Date: " + dateString);
			sendLine(out, "Last-Modified: " + dateString);
			sendLine(out, "Connection: close");
			sendLine(out, "");

			// Send the payload
			sendLine(out, payload);
		}
	}

	static void onPost(OutputStream os, String url, char[] incomingPayload)
	{
		// Parse the incoming payload
 		System.out.println("----------------------------------------");
		String payload = String.valueOf(incomingPayload);
		System.out.println("Received the following payload: " + payload);
		Json incoming = Json.parse(payload);
		System.out.println("the user id is " + incoming.get("my_id"));
		System.out.println("The client's favorite number is: " + incoming.get("fav_num"));
		System.out.println("The x of mario is " + incoming.getLong("dx"));
		int mess_value = Integer.valueOf(incoming.get("mess_value").toString());

		
		//System.out.println("The x of turtle is " + incoming.getLong("tx"));


        //assigns user id
	     int incoming_id = Integer.valueOf(incoming.get("my_id").toString());
		System.out.println(incoming_id);

		//assigns value from mario position
         int mdest_x = Integer.valueOf(incoming.get("dx").toString());
        int mdest_y = Integer.valueOf(incoming.get("dy").toString());
        int t_destx = Integer.valueOf(incoming.get("fav_num").toString());
        
    
         new_x += mdest_x;
         new_y +=mdest_y;
         new_tx +=t_destx;
   
         System.out.println("added turtle movements is  " + new_tx);
          //System.out.println("added turtle  movements is  " + t_dest);
         if(t_destx < new_tx){
         	t_destx = new_tx;
         	new_tx = 0;
         }
		
         if(mdest_x < new_x ){
         	mdest_x = new_x;
            mdest_y = new_y;
            new_x =0;
            new_y = 0;
         }
         
		// Make a response
		Json outgoing = Json.newObject();
		outgoing.add("msg", "Thanks for the spiffy message");
		outgoing.add("fav_num",t_destx);
		System.out.println("the number i am sending is " + t_destx);

	    outgoing.add("dx", mdest_x);
		outgoing.add("dy", mdest_y);
		//outgoing.add("tx", 1);
		
		//assign user id
        if(incoming_id == -1){
			incoming_id = player_id;
			    player_id+=1;

		}
		else{
			//incoming_id = --player_id;
		}
		//message
        if(mess_value == 1){
			System.out.println("Message is  " + incoming.getString("message"));
			 String new_mess =(String)incoming.getString("message");
			 outgoing.add("message" , new_mess);
         System.out.println("the id sending this " + incoming_id + " message is " + new_mess);
          message.put(incoming_id, new_mess);
          for(int i=0; i<message.size(); i++){
         	System.out.println("the messages are " + message.get(i));
         	String going_mess = message.get(i);
            
         }
		}
        /* if(new_mess!=null && !new_mess.isEmpty()){
         	if(incoming_id == 0){
         		player_one++;
         		System.out.println("i have an added message as  " + player_one);
         	}
         	else if(incoming_id == 1){
         		player_two++;
         		System.out.println("i have an added message as  " + player_two);
         	}
         	else{

         	}*/
        
         
         //System.out.println("I am putting this in here  " + new_mess);
     	//}
         
		
		outgoing.add("my_id", incoming_id);
        System.out.println(" The new player id is  " + incoming_id);


		
		String response = outgoing.toString();

		// Send HTTP headers
		System.out.println("----------The server replied: ----------");
		String dateString = getServerTime();
		PrintWriter out = new PrintWriter(os, true);
		sendLine(out, "HTTP/1.1 200 OK");
		sendLine(out, "Content-Type: application/json");
		sendLine(out, "Content-Length: " + Integer.toString(response.length()));
		sendLine(out, "Date: " + dateString);
		sendLine(out, "Last-Modified: " + dateString);
		sendLine(out, "Connection: close");
		sendLine(out, "");
		
		// Send the response
		sendLine(out, response);
		out.flush();
	}

	public static void main(String[] args) throws Exception
	{
		// Make a socket to listen for clients
		int port = 1235;
		//int backlog =50;
		String ServerURL = "http://localhost:" + Integer.toString(port) + "/Mario.htm";
		//String ServerURL1 = "http://localhost:" + Integer.toString(port) + "/page.html";
		ServerSocket serverSocket = new ServerSocket(port);

		// Start the web browser
		if(Desktop.isDesktopSupported()){
			
			Desktop.getDesktop().browse(new URI(ServerURL));
			Desktop.getDesktop().browse(new URI(ServerURL));
		}
		else{

			System.out.println("Please direct your browser to " + ServerURL);
		}

		// Handle requests from clients
		while(true)
		{
			Socket clientSocket = serverSocket.accept(); // This call blocks until a client connects
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			OutputStream os = clientSocket.getOutputStream();

			// Read the HTTP headers
			String headerLine;
			int requestType = 0;
			int contentLength = 0;
			String url = "";
			System.out.println("----------A client said: ----------");
			while ((headerLine = in.readLine()) != null)
			{
				System.out.println(headerLine);
				if(headerLine.length() > 3 && headerLine.substring(0, 4).equals("GET "))
				{
					requestType = 1;
					url = headerLine.substring(4, headerLine.indexOf(" ", 4));
				}
				else if(headerLine.length() > 4 && headerLine.substring(0, 5).equals("POST "))
				{
					requestType = 2;
					url = headerLine.substring(5, headerLine.indexOf(" ", 5));
				}
				else if(headerLine.length() > 15 && headerLine.substring(0, 16).equals("Content-Length: "))
					contentLength = Integer.parseInt(headerLine.substring(16));
				if(headerLine.length() < 2) // Headers are terminated by a "\r\n" line
					break;
			}

			// Send a response
			if(requestType == 1)
			{
				onGet(os, url);
			}
			else if(requestType == 2)
			{
				// Read the incoming payload
				char[] incomingPayload = new char[contentLength];
				in.read(incomingPayload, 0, contentLength);
				String blobHead = incomingPayload.length < 60 ? new String(incomingPayload) : new String(incomingPayload, 0, 60) + "...";
				System.out.println(blobHead);
				onPost(os, url, incomingPayload);
			}
			else
				System.out.println("Received bad headers. Ignoring.");

			// Hang up
			os.flush();
			clientSocket.close();
		}
	}
}
