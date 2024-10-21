package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class BankClient {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bank Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("User");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new Socket("localhost", 1234);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    // Send username and password to the server
                    out.println(userText.getText());
                    out.println(passwordText.getText());  // Send the password
                    String response = in.readLine();
                    JOptionPane.showMessageDialog(null, response);

                    if (response.equals("Login successful")) {
                        showTransferUI();
                    } else {
                        socket.close(); // Close socket if login fails
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static void showTransferUI() {
        JFrame transferFrame = new JFrame("Transfer");
        transferFrame.setSize(300, 200);

        JPanel panel = new JPanel();
        transferFrame.add(panel);
        placeTransferComponents(panel);

        transferFrame.setVisible(true);
    }

    private static void placeTransferComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel toUserLabel = new JLabel("To User");
        toUserLabel.setBounds(10, 20, 80, 25);
        panel.add(toUserLabel);

        JTextField toUserText = new JTextField(20);
        toUserText.setBounds(100, 20, 165, 25);
        panel.add(toUserText);

        JLabel amountLabel = new JLabel("Amount");
        amountLabel.setBounds(10, 50, 80, 25);
        panel.add(amountLabel);

        JTextField amountText = new JTextField(20);
        amountText.setBounds(100, 50, 165, 25);
        panel.add(amountText);

        JButton transferButton = new JButton("Transfer");
        transferButton.setBounds(10, 80, 100, 25);
        panel.add(transferButton);

        transferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String toUser = toUserText.getText();
                    String amount = amountText.getText();
                    out.println("TRANSFER " + toUser + " " + amount);

                    String response = in.readLine();
                    JOptionPane.showMessageDialog(null, response);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

