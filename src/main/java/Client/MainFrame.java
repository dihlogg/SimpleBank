package Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextField textFieldIp;
    private ClientSocket cs;

    public MainFrame() {
        setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // Thiết lập màu nền cho JFrame
      

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Thiết lập màu nền cho mainPanel
        mainPanel.setBackground(Color.BLUE); // Màu tím

        // Tạo và thêm giao diện Start
        JPanel startPanel = createStartPanel();
        mainPanel.add(startPanel, "Start");

        // Tạo và thêm giao diện Login
        JPanel loginPanel = new LoginPanel();
        mainPanel.add(loginPanel, "Login");
        try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        add(mainPanel);
    }


    private JPanel createStartPanel() {
        JPanel panel = new JPanel(null);
        JLabel lbIP = new JLabel("IP:");
        lbIP.setBounds(80, 80, 150, 25);
        panel.add(lbIP);
        setBackground(new Color(114, 220, 210));
        textFieldIp = new JTextField();
        textFieldIp.setBounds(200, 80, 200, 25);
        panel.add(textFieldIp);

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBounds(180, 140, 100, 30);
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = textFieldIp.getText();
                if (isServerAvailable(ip)) {
                    cs = new ClientSocket(ip); // Khởi tạo ClientSocket với IP nhập vào
                    cardLayout.show(mainPanel, "Login");
                } else {
                    JOptionPane.showMessageDialog(panel, "Không thể kết nối đến server. Vui lòng kiểm tra địa chỉ IP.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(btnConnect);

        return panel;
    }

    private class LoginPanel extends JPanel {
        private JTextField txtUsername;
        private JPasswordField txtPassword;
        private String sepa = "#####";

        public LoginPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            setBackground(new Color(135, 206, 250)); // Màu tím
            JLabel lbLogin = new JLabel("Login");
            lbLogin.setFont(new Font("Tahoma", Font.BOLD, 24));
            lbLogin.setForeground(new Color(0, 255, 255));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(lbLogin, gbc);

            JLabel lbUsername = new JLabel("User:");
            lbUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            add(lbUsername, gbc);

            txtUsername = new JTextField(20);
            gbc.gridx = 1;
            add(txtUsername, gbc);

            JLabel lblPass = new JLabel("Pass:");
            lblPass.setFont(new Font("Tahoma", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(lblPass, gbc);

            txtPassword = new JPasswordField(20);
            gbc.gridx = 1;
            add(txtPassword, gbc);

            JCheckBox chckbxShowPassword = new JCheckBox("Show");
            gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.WEST;
            add(chckbxShowPassword, gbc);

            chckbxShowPassword.addActionListener(e -> {
                txtPassword.setEchoChar(chckbxShowPassword.isSelected() ? (char) 0 : '*');
            });
            
         

            JButton btnLogin = new JButton("Login");
            gbc.gridy = 4;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            add(btnLogin, gbc);
         // Nút Đăng ký
            JButton btnSignUp = new JButton("Sign up");
            btnSignUp.setFont(new Font("Tahoma", Font.BOLD, 14));
            gbc.gridx = 1;
            add(btnSignUp, gbc);

            btnLogin.addActionListener(e -> {
                String accountName = txtUsername.getText().trim();
                String password = String.valueOf(txtPassword.getPassword());
                if (!accountName.isEmpty()) {
                    cs.sendRequest("LOGIN " + accountName + sepa + password);
                }
            });
            btnSignUp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SignUp su = new SignUp(textFieldIp.getText());
                    su.setVisible(true);
                    setVisible(false);
                }
            });
        }
    }

    private boolean isServerAvailable(String ip) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String testMessage = "PING";
            byte[] sendBuffer = testMessage.getBytes();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 7777);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.setSoTimeout(2000);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            return response.equals("PONG");
        } catch (IOException ex) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
