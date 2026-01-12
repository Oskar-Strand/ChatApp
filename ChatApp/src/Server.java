import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

public class Server implements Runnable {
    @Override
    public void run() {
        try {
        ServerSocket server = new ServerSocket(9999);
        Socket client = server.accept();
    } catch (IOException e) {
            // TODO: handle exception
    }
}

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client) {
            // Initialize with client socket
        }
        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter your name: ");
                name = in.readLine();
            } catch (IOException e) {
                //TODO: handle exception
        }
    }
}
}
