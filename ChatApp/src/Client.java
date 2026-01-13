// Import statements for I/O operations and networking
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Client class that connects to the chat server and handles messaging
// Implements Runnable to allow the client to run on a separate thread
public class Client implements Runnable {

    // Socket connection to the server
    private Socket client;
    // Reader to receive messages from the server
    private BufferedReader in;
    // Writer to send messages to the server
    private PrintWriter out;
    // Flag to control when the client should shut down
    private boolean done;
    
    // Main client loop that connects to the server and receives messages
    @Override
    public void run() {
        try {
            // Connect to the server on localhost port 9999
            client = new Socket("127.0.0.1", 9999);
            // Set up output stream to send messages with auto-flush enabled
            out = new PrintWriter(client.getOutputStream(), true);
            // Set up input stream to receive messages from the server
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // Create an input handler to read messages from the user
            InputHandler inHandler = new InputHandler();
            // Start the input handler in a separate thread
            Thread t = new Thread(inHandler);
            t.start();

            // Receive and display messages from the server
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    // Gracefully shuts down the client connection
    public void shutdown() {
        // Set done flag to exit the input handler loop
        done = true;
        try {
            // Close input stream
            in.close();
            // Close output stream
            out.close();
            // Close client socket if it's still open
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // Silently catch any IO exceptions during shutdown
        }
    }
    
    // Inner class that handles user input and sends it to the server
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                // Create a reader to get input from the console
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                
                // Read user input in a loop until client shuts down
                while (!done) {
                    // Get the next line of input from the user
                    String message = inReader.readLine();
                    // Check if the user wants to quit
                    if(message.equals("/quit")) {
                        // Send the quit command to the server
                        out.println(message);
                        // Close the input reader
                        inReader.close();
                        // Shutdown the client
                        shutdown();
                    } else {
                        // Send the message to the server
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                // Handle connection errors by shutting down
                shutdown();
            }
        }
    }
    
    // Main method to start the client
    public static void main(String[] args) {
        // Create a new client instance
        Client client = new Client();
        // Start the client's main loop
        client.run();
    }
}
