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
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        // Title (Login)
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(new Color(69, 68, 68));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(50, 20, 300, 50);
        panel.add(titleLabel);

        // User Label
        JLabel userLabel = new JLabel("User Name");
        userLabel.setBounds(50, 80, 100, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(150, 80, 200, 25);
        panel.add(userText);

        // Password Label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50, 120, 100, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(150, 120, 200, 25);
        panel.add(passwordText);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(50, 160, 100, 30);
        loginButton.setBackground(new Color(125, 229, 251));
        loginButton.setForeground(new Color(40, 40, 40));
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new Socket("localhost", 1234);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    // Gửi username và password đến server
                    out.println(userText.getText());
                    out.println(passwordText.getText());
                    String response = in.readLine();
                    JOptionPane.showMessageDialog(null, response);

                    if (response.equals("Login successful")) {
                        // Đóng cửa sổ login và mở cửa sổ transfer
                        SwingUtilities.getWindowAncestor(loginButton).dispose(); // Đóng cửa sổ hiện tại (login)
                        showTransferUI(); // Mở cửa sổ giao diện transfer
                    } else {
                        socket.close(); // Đóng kết nối socket nếu đăng nhập thất bại
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Register Button
        JButton registerButton = new JButton("Register Now");
        registerButton.setBounds(160, 160, 150, 30);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        registerButton.setForeground(new Color(30, 122, 236));
        registerButton.setContentAreaFilled(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRegisterUI();
            }
        });
    }

    private static void showTransferUI() {
        JFrame transferFrame = new JFrame("Transfer Money");
        transferFrame.setSize(400, 300);

        JPanel panel = new JPanel();
        transferFrame.add(panel);
        placeTransferComponents(panel);

        transferFrame.setVisible(true);
    }

    private static void placeTransferComponents(JPanel panel) {
        panel.setLayout(null);

        // Title for Transfer UI
        JLabel transferTitle = new JLabel("Money Transfer");
        transferTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        transferTitle.setForeground(new Color(69, 68, 68));
        transferTitle.setHorizontalAlignment(SwingConstants.CENTER);
        transferTitle.setBounds(50, 20, 300, 50);
        panel.add(transferTitle);

        // To User Label and TextField
        JLabel toUserLabel = new JLabel("To User");
        toUserLabel.setBounds(50, 100, 80, 25);
        toUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(toUserLabel);

        JTextField toUserText = new JTextField(20);
        toUserText.setBounds(150, 100, 200, 25);
        toUserText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(toUserText);

        // Amount Label and TextField
        JLabel amountLabel = new JLabel("Amount");
        amountLabel.setBounds(50, 140, 80, 25);
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(amountLabel);

        JTextField amountText = new JTextField(20);
        amountText.setBounds(150, 140, 200, 25);
        amountText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(amountText);

        // Transfer Button
        JButton transferButton = new JButton("Transfer");
        transferButton.setBounds(150, 190, 100, 35);
        transferButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        transferButton.setBackground(new Color(125, 229, 251));
        transferButton.setForeground(new Color(40, 40, 40));
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

    private static void showRegisterUI() {
        JFrame registerFrame = new JFrame("Register");
        registerFrame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        registerFrame.add(panel);

        JLabel userLabel = new JLabel("User Name");
        userLabel.setBounds(50, 50, 100, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(150, 50, 200, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50, 90, 100, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(150, 90, 200, 25);
        panel.add(passwordText);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(150, 130, 100, 25);
        panel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new Socket("localhost", 1234);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    out.println("REGISTER");
                    out.println(userText.getText());
                    out.println(passwordText.getText());

                    String response = in.readLine();
                    JOptionPane.showMessageDialog(null, response);

                    if (response.equals("Registration successful")) {
                        registerFrame.dispose();
                    }

                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        registerFrame.setVisible(true);
    }
}