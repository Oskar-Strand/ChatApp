// Import statements for I/O operations, networking, and concurrent threading
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Server class that manages client connections and broadcasts messages
// Implements Runnable to allow the server to run on a separate thread
public class Server implements Runnable {

    // List to store all active client connection handlers
    private ArrayList<ConnectionHandler> connections;
    // ServerSocket that listens for incoming client connections
    private ServerSocket server;
    // Flag to control when the server should shut down
    private boolean done;
    // Thread pool to handle multiple client connections concurrently
    private ExecutorService pool;
    
    // Constructor initializes the server components
    public Server() {
        connections = new ArrayList<>();
        done = false;
    }
    
    // Main server loop that accepts client connections
    @Override
    public void run() {
        try {
            // Create a ServerSocket listening on port 9999
            server = new ServerSocket(9999);
            // Create a thread pool to handle multiple clients concurrently
            pool = Executors.newCachedThreadPool();
            // Accept client connections in a loop until shutdown
            while (!done) {
                // Accept incoming client connection
                Socket client = server.accept();
                // Create a handler for this client
                ConnectionHandler handler = new ConnectionHandler(client);
                // Add handler to the list of connections
                connections.add(handler);
                // Execute handler in the thread pool
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    // Broadcasts a message to all connected clients
    public void broadcast(String message) {
        for (ConnectionHandler handler : connections) {
            if( handler != null) {
                handler.sendMessage(message);
            }
        }
    }

    // Gracefully shuts down the server and all client connections
    public void shutdown() {
        try {
            // Set done flag to exit the server loop
            done = true;
            // Shutdown the thread pool
            pool.shutdown();
            // Close the server socket if it's still open
            if (!server.isClosed()) {
                server.close();
            }
            // Notify all clients and close their connections
            for (ConnectionHandler handler : connections) {
                handler.sendMessage("Server is shutting down...");
                handler.shutdown();
            }
        } catch (IOException e) {
            // Silently catch any IO exceptions during shutdown
        }
    }

    // Inner class that handles individual client connections
    class ConnectionHandler implements Runnable {
        // Socket for communicating with the client
        private Socket client;
        // Reader to receive messages from the client
        private BufferedReader in;
        // Writer to send messages to the client
        private PrintWriter out;
        // Client's username
        private String name;

        // Constructor that accepts a client socket
        public ConnectionHandler(Socket client) {
            this.client = client;
        }
        
        // Run method that handles the client connection lifecycle
        @Override
        public void run() {
            try {
                // Set up output stream with auto-flush enabled
                out = new PrintWriter(client.getOutputStream(), true);
                // Set up input stream to receive client messages
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                // Ask client for their username
                out.println("Enter your name: ");
                // Read the username from the client
                name = in.readLine();
                // Print to server console that client connected
                System.out.println(name + " has connected.");
                // Notify all clients that a new user joined
                broadcast(name + " Joined the chat!");
                // Read messages from client in a loop
                String message;
                while ((message = in.readLine()) != null) {
                    // Handle nickname change command
                    if(message.startsWith("/nick")) {
                        // Split message to extract new nickname
                        String [] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            // Broadcast the name change to all clients
                            broadcast(name + " changed their name to " + messageSplit[1]);
                            // Update the client's name
                            name = messageSplit[1];
                            // Confirm the change to the client
                            out.println("Your name has been changed to " + name);
                            
                        } else {
                            // Notify if no nickname was provided
                            out.println("No nickname provided.");
                        }
                    // Handle quit command
                    } else if (message.startsWith("/quit")) {
                        // Notify all clients that user is leaving
                        broadcast(name + " has left the chat.");
                        // Shutdown this client connection
                        shutdown();
                    // Broadcast regular chat messages
                    } else {
                        broadcast(name + ": " + message);
                    }
            }} catch (IOException e) {
                // Handle connection errors by shutting down
                shutdown();
        }
        }
        
        // Sends a message to this specific client
        public void sendMessage(String message) {
            out.println(message);
        }
        
        // Closes the connection for this client
        public void shutdown() {
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
    }
    
    // Main method to start the server
    public static void main(String[] args) {
        // Create a new server instance
        Server server = new Server();
        // Start the server's main loop
        server.run();
    }
}
