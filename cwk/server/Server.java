import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Server 
{
    ServerSocket server = null;
    ExecutorService service = null;

    public void runServer() {
        
        System.out.println("Server running...");

        try {
            server = new ServerSocket(9111);

        } catch (IOException e) {
            System.err.println("Could not listen on port: 9111.");
            System.exit(-1);
        }

        // Initialise the executor.
        // Creates a new thread pool that re-uses threads
        // service = Executors.newCachedThreadPool();
        service = Executors.newFixedThreadPool(20);


        // For each new client, submit a new handler to the thread pool.
        try {

            createLogFile();

            while( true )
            {
                Socket client = server.accept();
            
                // Submit a new instance of KKClientHandler
                service.submit( new ClientHandler(client) );
            }

        } catch (IOException e ) {

            System.out.println(e);
        }
    }

    private static void createLogFile() {

        // File logFile = new File("server/log.txt");
        File logFile = new File("log.txt");


        if (logFile.exists()) {
            logFile.delete();
        }

        try {
            logFile.createNewFile();
        } catch ( IOException e ) { 
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {

        Server server = new Server();
        server.runServer();

    }

}

