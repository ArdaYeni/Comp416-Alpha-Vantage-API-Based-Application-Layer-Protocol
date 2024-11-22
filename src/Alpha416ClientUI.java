import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Alpha416ClientUI extends JFrame {
    private JTextField commandField;
    private JTextArea responseArea;
    private JButton sendButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Alpha416ClientUI() {
        setTitle("Alpha416 Client");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeComponents();
        initializeConnection();
    }

    private void initializeComponents() {

        commandField = new JTextField();
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        sendButton = new JButton("Send a Command");

        setLayout(new BorderLayout());
        add(new JScrollPane(responseArea), BorderLayout.CENTER);

        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.add(new JLabel("Enter Command:"), BorderLayout.WEST);
        commandPanel.add(commandField, BorderLayout.CENTER);
        commandPanel.add(sendButton, BorderLayout.EAST);
        add(commandPanel, BorderLayout.SOUTH);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand();
            }
        });
    }

    private void initializeConnection() {
        try {
            socket = new Socket("localhost", 4160);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            showError("Error connecting to server: " + e.getMessage());
        }
    }

    private void sendCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            showError("Please enter a command.");
            return;
        }


        out.println(command);
        commandField.setText("");

        try {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.equals("END_OF_MESSAGE")) {
                response.append(line).append("\n");
            }
            responseArea.setText(response.toString());
        } catch (IOException e) {
            showError("Error reading response from server: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Alpha416ClientUI clientUI = new Alpha416ClientUI();
            clientUI.setVisible(true);
        });
    }
}
