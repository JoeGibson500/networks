
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.time.LocalTime;

public class ClientHandler extends Thread {
    
	private Socket clientSocket = null;

    public ClientHandler(Socket socket) {
		super("ClientHandler");
		this.clientSocket = socket;
    }

    public void run() {

		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String input= in.readLine();

			String[] parts = input.split(" ", 2);
			String request = parts[0].trim();

			if (request.equals("list")) {
				handleListRequest(out);
			} else if (request.equals("put")) {
				if (parts.length != 2) {
					out.println("Invalid request: put request requires a filename argument.");
				} else {
					String filename = parts[1].trim();
					handlePutRequest(filename, in, out);
				}				
			} else {
				out.println("Invalid input, request = " + request);
			}

			updateLogFile(request);


		} catch ( IOException e ) {
			e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

	// Handle files in the serverFiles directory 
	// Send the list of files back to the client
	// Log the request to log.txt with format date|time|client IP address|request
	private void handleListRequest(PrintWriter out) { 

		try {
			File serverFiles = new File("serverFiles");
			File[] files = serverFiles.listFiles();

			if (files != null && files.length > 0) {
				for (File file : files) {
					out.println(file.getName());
				}
			} else {
				out.println("No files exist on server.");
			}
		} catch ( Exception e ) {
			out.println("Error listing files: " + e.getMessage());
		}
	}


	// Check if file exists in serverFiles directory 
	// If it exists:
	// 		Send an error message back to the client indicating the file already exists
	// Else :
	// 		Receive file data from client
	// 		Write log entry with format date|time|client IP address|request
	//		Close the log file
	private void handlePutRequest(String filename, BufferedReader in, PrintWriter out) { 
		
		File newFile = new File("serverfiles/" + filename);

		if (newFile.exists()) {
			out.println("Error: Cannot upload file" + filename + " already exists on server.");
		} 

		try (BufferedWriter FileWriter = new BufferedWriter(new FileWriter(newFile, true));)
		{
			// BufferedWriter FileWriter = new BufferedWriter(new FileWriter(newFile, true));
			
			String lineCountString = in.readLine();
			int lineCount = Integer.parseInt(lineCountString);

			System.out.println("Linecount = " + lineCount);
			for (int i=0; i < lineCount; i++) {
				
				String line = in.readLine();

				System.out.println(line);

				FileWriter.write(line);
				FileWriter.newLine();
			}


			// while((line = in.readLine()) != null ) {
			// 	FileWriter.write(line);
			// 	FileWriter.newLine();
			// }

			out.println(filename + " successfully uploaded to server.");

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}	
	
	// Write log entry with format date|time|client IP address|request
	private void updateLogFile(String request) {

		InetAddress inet = clientSocket.getInetAddress();
		Date date = new Date();
		LocalTime time = LocalTime.now();

		try (BufferedWriter FileWriter = new BufferedWriter(new FileWriter("log.txt", true));) {

			FileWriter.write("" + date.toString() + "|" + time.toString() + "|" + inet.getHostAddress() + "|" + request);
			FileWriter.newLine();
			// System.out.println("" + date.toString() + "|" + time.toString() + "|" + inet.getHostAddress() + "|" + request);

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
