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

		try ( // Try with resources
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
			
			// Parse input
			String input= in.readLine();

			String[] parts = input.split(" ", 2);
			String request = parts[0].trim();

			// Validate and determine request type
			switch (request) {
				case "list" : handleListRequest(out);
							  break;
				case "put" : if (parts.length == 2) {
							 	handlePutRequest(parts[1].trim(), in, out);
							 } else {
							 	out.println("Invalid request: put request requires a filename argument.");
							 }
				default:
					out.println("Invalid input, request = " + request);
					break;
			}

			updateLogFile(request);

		} catch (IOException e) {
			System.out.println(e);
        } finally { // Always close
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

	private void handleListRequest(PrintWriter out) { 

		try {
			
			File serverFiles = new File("serverFiles");
			File[] files = serverFiles.listFiles();

			// Verify serverFiles not empty and if not, send the name of all files to client
			if (files != null && files.length > 0) {
				
				out.println("Listing " + files.length + " file(s):");
				
				for (File file : files) {
					out.println(file.getName());
				}

			} else {
				out.println("No files exist on server.");
			}

		} catch (Exception e) {
			out.println("Error listing files: " + e.getMessage());
		}
	}

	private void handlePutRequest(String filename, BufferedReader in, PrintWriter out) { 
		
		File newFile = new File("serverfiles/" + filename);

		if (newFile.exists()) {
			out.println("Error: Cannot upload file " + filename + " already exists on server.");
		} 

		// Try with resources 
		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(newFile, true));) {
			
			// Read the number of lines in file
			String lineCountString = in.readLine();
			int lineCount = Integer.parseInt(lineCountString);

			// Read the line and write it to the file followed by a newline
			// Repeat "lineCount" number of times
			for (int i=0; i<lineCount; i++) {
				
				String line = in.readLine();

				fileWriter.write(line);
				
				if (i < lineCount - 1) {
					fileWriter.newLine();
				}
			}

			out.println(filename + " successfully uploaded to server.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	// Write log entry with format date|time|client IP address|request
	private void updateLogFile(String request) {

		InetAddress inet = clientSocket.getInetAddress();
		Date date = new Date();
		LocalTime time = LocalTime.now();

		// Try with resources
		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter("log.txt", true));) {

			fileWriter.write(date + "|" + time + "|" + inet.getHostAddress() + "|" + request);
			fileWriter.newLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
