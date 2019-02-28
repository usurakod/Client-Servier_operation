
import java.io.*;
import java.net.*;
import java.util.*;

public final class server {
	static String myVal="";
	public static void main(String args[]) throws Exception {
		if (args.length != 4) {
			System.out.println("Enter 4 arguments including port number");
			System.exit(0);
		}
		myVal = args[1];

		// Establish the listen socket.
		ServerSocket mySocket = new ServerSocket(Integer.parseInt(args[3]));

		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			Socket conSocket = mySocket.accept();
			// Construct an object to process the HTTP request message.
			HttpRequest myRequest = new HttpRequest(conSocket);
			// Creating a thread which will process the request.
			Thread thread = new Thread(myRequest);
			// Start the thread.
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";// returning carriage return (CR) and a line feed (LF)
	Socket socket;
	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	// Implement the run() method of the Runnable interface.
	// Within run(), we explicitly catch and handle exceptions with a try/catch
	// block.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println("Exception while running: "+e);
		}
	}

	private void processRequest() throws Exception {
		// Get a reference to the socket's input and output streams.
		try {
			InputStream instream = socket.getInputStream();
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			String statusLine = null;
			String contentTypeLine = null;
			String entityBody = null;

			// Set up input stream filters.
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(instream));// reads the input data
			boolean isNewVersion = false;

			// Get the request line of the HTTP request message.
			String reqLine = buffRead.readLine();// get /path/file.html version of http
			String[] parts = reqLine.toUpperCase().split("\\s+"); // Splitting 1st line of request header with "/" to know the type of
			// request

			// Display the request line.
			System.out.println();
			System.out.println(reqLine);

			// HERE WE NEED TO DEAL WITH THE REQUEST
			if (parts[2].equals("HTTP/1.1")) {
				isNewVersion = true;
				//socket.setKeepAlive(true);
			} else if (parts[2].equals("HTTP/1.0")) {
				isNewVersion = false;
				//socket.setKeepAlive(false);
			}
			else {
				statusLine = "400 Bad Request/505 version not supported" + CRLF;// HTTP Bad Request
				outStream.writeBytes(statusLine);
				socket.close();
				return;
			}
			//boolean alive = true;
			if (isNewVersion) { // This is done if connection is persistent
				//socket.setSoTimeout(1000*30);
				/*while (alive) {
					alive = false;*/
				//try {
				// Checking if Request type is GET/HEAD
				if ((parts[0].equals("GET")) || (parts[0].equals("HEAD"))) {

					// Extract the filename from the request line.
					StringTokenizer tokens = new StringTokenizer(reqLine);// this is a input method with
					// deliminators
					tokens.nextToken(); // skip over the method, which should be "GET"
					String fileName = tokens.nextToken();

					if (fileName.endsWith("/")) // Append "/" with "index.html"
						fileName += "index.html";

					while (fileName.indexOf("/") == 0)
						fileName = fileName.substring(1);// Remove leading / from filename

					fileName = server.myVal + fileName;// add file path with the given file path
					// Open the requested file.

					if((new File(fileName).exists()) && !(new File(fileName).canRead())) {
						statusLine = "403 Forbidden" + CRLF;// File Forbidden
						outStream.writeBytes(statusLine);
						socket.close();
					}

					FileInputStream fileCheck = null;
					boolean fileAvailable = true;
					try {
						fileCheck = new FileInputStream(fileName);
					} catch (FileNotFoundException e) {
						fileAvailable = false;
					}

					// Construct the response message.
					if (fileAvailable) {
						statusLine = "HTTP/1.1 200 OK" + CRLF; // common success message
						contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
					} // content info

					else {
						statusLine = "HTTP/1.1 404" + CRLF;// common error message
						contentTypeLine = "Content-type: " + "text/html" + CRLF;// content info
						entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>"
								+ "<BODY>Page Not Found </BODY></HTML>";
					}

					// Send the status line.
					outStream.writeBytes(statusLine);

					// Send the content type line.
					outStream.writeBytes(contentTypeLine);

					// Send a blank line to indicate the end of the header lines.
					outStream.writeBytes(CRLF);

					// Send the entity body.
					if (fileAvailable) {
						sendBytes(fileCheck, outStream);
						outStream.writeBytes(statusLine);// Send the status line.
						outStream.writeBytes(contentTypeLine);// Send the content type line.
						fileCheck.close();
					} else {
						outStream.writeBytes(statusLine);// Send the status line.
						outStream.writeBytes(entityBody);// Send the an html error message info body.
						outStream.writeBytes(contentTypeLine);// Send the content type line.
						socket.close();
					}

					System.out.println("*");
					System.out.println(fileName);// print out file request to console
					System.out.println("*");

					// Get and display the header lines.
					String headerLine = null;
					while ((headerLine = buffRead.readLine()).length() != 0) {
						System.out.println(headerLine);
					}
				} else {
					statusLine = "HTTP/1.1 400 Bad Request" + CRLF;// for GET isn't there Error message
					contentTypeLine = "Content-type: " + "text/html" + CRLF;// content info

					// Send the status line.
					outStream.writeBytes(statusLine);

					// Send the content type line.
					outStream.writeBytes(contentTypeLine);

					// Send a blank line to indicate the end of the header lines.
					outStream.writeBytes(CRLF);
				}

				// Close streams and socket.
				outStream.close();
				buffRead.close();
				socket.close();
				/*reqLine = buffRead.readLine();// get /path/file.html version of http
					parts = reqLine.split("\\s+"); // Splitting 1st line of request header with "/" to know the type of
					while(reqLine.length()!=0){
						reqLine=buffRead.readLine();
					}
					alive = true;*/

				/*} catch (SocketTimeoutException e) {
					statusLine = "HTTP/1.1 408 Request Time Out" + CRLF;// GET Error message

					// Send the status line.
					outStream.writeBytes(statusLine);

					System.out.println("session timeout");
					//alive = false;
					outStream.close();
					buffRead.close();
					socket.close();
				}*/					
				//}
			} else { //this is for HTTP/1.0 request
				if ((parts[0].equals("GET")) || (parts[0].equals("HEAD")) || 
						(parts[0].equals("get")) || (parts[0].equals("head"))) { // Checking if Request type is
					// GET/HEAD
					StringTokenizer tokens = new StringTokenizer(reqLine);// this is a input method with
					// deliminators
					tokens.nextToken(); // skip over the method, which should be "GET"
					String fileName = tokens.nextToken();

					if (fileName.endsWith("/")) // Append "/" with "index.html"
						fileName += "index.html";

					while (fileName.indexOf("/") == 0)
						fileName = fileName.substring(1);// Remove leading / from filename

					fileName = server.myVal + fileName;// add file path with the given file path

					if((new File(fileName).exists()) && !(new File(fileName).canRead())) {
						statusLine = "403 Forbidden" + CRLF;// File Forbidden
						outStream.writeBytes(statusLine);
						socket.close();
					}

					// Open the requested file.
					FileInputStream fileCheck = null;
					boolean fileAvailable = true;
					try {
						fileCheck = new FileInputStream(fileName);
					} catch (FileNotFoundException e) {
						fileAvailable = false;
					}

					// Construct the response message.
					if (fileAvailable) {
						statusLine = "HTTP/1.0 200 OK" + CRLF; // common success message
						contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
					} // content info

					else {
						statusLine = "HTTP/1.0 404 " + CRLF;// common error message
						contentTypeLine = "Content-type: " + "text/html" + CRLF;// content info
						entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>"
								+ "<BODY>Page Not Found </BODY></HTML>";
					}

					// Send the status line.
					outStream.writeBytes(statusLine);

					// Send the content type line.
					outStream.writeBytes(contentTypeLine);

					// Send a blank line to indicate the end of the header lines.
					outStream.writeBytes(CRLF);

					// Send the entity body.
					if (fileAvailable) {
						sendBytes(fileCheck, outStream);
						outStream.writeBytes(statusLine);// Send the status line.
						outStream.writeBytes(contentTypeLine);// Send the content type line.
						fileCheck.close();
					} else {
						outStream.writeBytes(statusLine);// Send the status line.
						outStream.writeBytes(entityBody);// Send the an html error message info body.
						outStream.writeBytes(contentTypeLine);// Send the content type line.
						socket.close();
					}

					System.out.println("*");
					System.out.println(fileName);// print out file request to console
					System.out.println("*");
					// Get and display the header lines.
					String headerLine = null;
					while ((headerLine = buffRead.readLine()).length() != 0) {
						System.out.println(headerLine);
					}
				} else {
					statusLine = "HTTP/1.0 400 Bad Request" + CRLF;// GET Error message
					contentTypeLine = "Content-type: " + "text/html" + CRLF;// content info

					// Send the status line.
					outStream.writeBytes(statusLine);

					// Send the content type line.
					outStream.writeBytes(contentTypeLine);

					// Send a blank line to indicate the end of the header lines.
					outStream.writeBytes(CRLF);
				}
				// Close streams and socket.
				outStream.close();
				buffRead.close();
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// return the file types
	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".css")) {
			return "text/css";
		}
		if (fileName.endsWith(".gif")) {
			return "image/gif";
		}
		if (fileName.endsWith(".mp4")) {
			return "video/mp4";
		}
		return "application/octet-stream";
	}

	// set up input output streams
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1)// read() returns minus one, indicating that the end of the file
		{
			os.write(buffer, 0, bytes);
		}
	}
}