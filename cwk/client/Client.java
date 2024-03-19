//
// Simple client that tries to connect to a PortReporterServer running on localhost,
// and reports all ports to screen, then closes.
//

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client
{
    public void connect(String command, String filename) {
        
        try {

            Socket socket = new Socket( "localhost", 9111);

            // System.out.println( "CLIENT: Connected to server with local port " + socket.getLocalPort() + " connected to remote port " + socket.getPort() + "." );

            if("list".equalsIgnoreCase(command)) {
                handleListRequest(socket);
            } else if ("put".equalsIgnoreCase(command)) {
                if(filename == null) {
                    System.out.println("Usage: java Client put <filename>");
                } else {
                    handlePutRequest(filename, socket);
                }
            } else {
                System.out.println("Usage java Client <command> [<filename>]");
            }
            socket.close();
        } catch( IOException e ) {
            System.out.println( e );
        }
    }

    private void handleListRequest(Socket socket) {

        try {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("list");

            String line;

            while((line = in.readLine())!= null) {
                System.out.println(line);
            }
        } catch ( IOException e ) {
            System.out.println( e );
        }
    }

    private void handlePutRequest(String filename, Socket socket) {
        
        try {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader fileReader = new BufferedReader(new FileReader(filename));

            out.println("put " + filename);

            List<String> lines = new ArrayList<>();

            String line;
            while (( line = fileReader.readLine()) != null) {
                lines.add(line);
            }
            
            out.println(lines.size());

            for (String output : lines) {
                out.println(output);
            }

            // String line;
            // while ((line = fileReader.readLine()) != null) {
            //     out.println(line);
            // }

            // out.println("EOF");

            String response = in.readLine();

            if (response != null) {
                System.out.println("Server response: " + response);
            }

            out.close();
            in.close();
            fileReader.close();

        } catch (IOException e ) {
            System.out.println(e);
        }
    }

    // Called with command line arguments, which are not used in this simple example.
    public static void main( String[] args )
    {
        if ( args.length <= 0) {
            System.out.println("Usage java Client <command> [<filename>]");
            return;
        }

        String command = args[0];
        String filename = (args.length > 1 ) ? args[1] : null;

        Client client = new Client();
        client.connect(command, filename);
    }
}