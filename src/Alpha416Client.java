import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Alpha416Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 4160);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(30000); //30 second for each client
            String userInput;

            while (true) {
                System.out.print("Please enter an command: ");
                userInput = scanner.nextLine();

                out.println(userInput);

                // in this part it will read the code until "END_OF_MESSAGE"
                StringBuilder response = new StringBuilder();
                String line;

                try {
                    while ((line = in.readLine()) != null && !line.equals("END_OF_MESSAGE")) {
                        response.append(line).append("\n");
                    }

                    if (line == null) {
                        System.out.println("Server has closed the connection.");
                        break;
                    } else {
                        System.out.println("Server response:\n" + response.toString());
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Error: Connection timed out.");
                    break;
                }

                if (userInput.equalsIgnoreCase("Alpha416 QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
