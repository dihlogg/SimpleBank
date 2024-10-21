package Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class MailClientGUI extends JFrame {
    private String accountName;
    private String Ip;
    private JTextField recipientField;
    private JTextArea emailContentArea;
    private JTextField txtTitle;
    private JTable emailTable; // JTable cho danh sách email
    private DefaultTableModel tableModel; // Model cho JTable
    private final String sepa = "#####"; // Dấu phân cách giữa các trường dữ liệu

    public MailClientGUI(String accountName, String Ip) {
        this.accountName = accountName;
        this.Ip = Ip;

        // Cấu hình giao diện
        setTitle("Mail Client UDP - " + accountName);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Đặt cửa sổ ở giữa màn hình
        setLayout(new BorderLayout());

        // Panel nhập liệu
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.WEST); // Đặt nửa trái

        // JTable cho danh sách email
        String[] columnNames = { "Sender", "Title", "Content" };
        tableModel = new DefaultTableModel(columnNames, 0); // Tạo bảng với cột
        emailTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(emailTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Received Emails"));
        add(tableScrollPane, BorderLayout.CENTER); // Đặt nửa phải

        // Lắng nghe sự kiện đóng cửa sổ
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendRequest("LOGOUT " + accountName);
                System.exit(0);
            }
        });

        // Bắt đầu kiểm tra email và cập nhật danh sách
        startEmailCheckThread();
        updateEmailList(); // Tải danh sách email ban đầu
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createTitledBorder("Compose Email"));
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(Color.lightGray);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // Khoảng cách giữa các thành phần

        // Thêm các trường nhập liệu vào panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        inputPanel.add(new JLabel("Recipient:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        recipientField = new JTextField(20);
        inputPanel.add(recipientField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        inputPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        txtTitle = new JTextField(20);
        inputPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        inputPanel.add(new JLabel("Content:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        emailContentArea = new JTextArea(4, 20);
        JScrollPane scrollPane = new JScrollPane(emailContentArea);
        inputPanel.add(scrollPane, gbc);

        // Nút gửi email
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; // Đặt nút gửi email trên cả hai cột
        JButton sendEmailButton = new JButton("Send Email");
        sendEmailButton.setForeground(new Color(0, 0, 139));
        sendEmailButton.setBackground(new Color(135, 206, 250));
        sendEmailButton.setFont(new Font("Arial", Font.BOLD, 16));
        inputPanel.add(sendEmailButton, gbc);

        // Lắng nghe sự kiện nhấn nút gửi
        sendEmailButton.addActionListener(e -> sendEmail());

        return inputPanel;
    }

    private void sendEmail() {
        String recipient = recipientField.getText().trim();
        String titleMail = txtTitle.getText().trim();
        String emailContent = emailContentArea.getText().trim();

        if (!recipient.isEmpty() && !titleMail.isEmpty() && !emailContent.isEmpty()) {
            String response = sendRequest("SEND_EMAIL " + accountName + sepa + recipient + sepa + titleMail + sepa + emailContent);
            if (response.startsWith("Email sent successfully")) {
                JOptionPane.showMessageDialog(null, "Đã gửi mail thành công!");
                clearInputFields(); // Xóa các trường nhập liệu
                updateEmailList(); // Cập nhật danh sách email sau khi gửi
            } else {
                JOptionPane.showMessageDialog(null, "Gửi mail thất bại: " + response, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter recipient and content");
        }
    }

    private void clearInputFields() {
        recipientField.setText("");
        txtTitle.setText("");
        emailContentArea.setText("");
    }

    private String sendRequest(String request) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(Ip);
            byte[] sendBuffer = request.getBytes();

            // Gửi yêu cầu
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, 7777);
            socket.send(sendPacket);

            // Nhận phản hồi
            byte[] receiveBuffer = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            socket.close();

            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR Sending request.";
        }
    }

    private void updateEmailList() {
        try {
            // Gửi yêu cầu để lấy danh sách email
            String request = "GET_EMAILS " + accountName;
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(Ip);

            // Gửi yêu cầu
            byte[] sendBuffer = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, 7777);
            socket.send(sendPacket);

            // Nhận phản hồi
            byte[] receiveBuffer = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Chuyển đổi dữ liệu nhận được thành chuỗi
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            // Cập nhật dữ liệu cho JTable
            String[] emails = response.split("\n");
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            for (String email : emails) {
                String[] emailData = email.split(sepa); // Giả sử các email được phân cách bằng dấu sepa
                System.out.println("Email Data có: "+emailData);
                    tableModel.addRow(emailData); // Thêm hàng vào bảng với Sender, Title, Content
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startEmailCheckThread() {
        new Thread(() -> {
            while (true) {
                try {
                    updateEmailList(); // Cập nhật danh sách email sau mỗi khoảng thời gian
                    Thread.sleep(5000); // Cập nhật mỗi 5 giây
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        String accountName = "YourAccountName"; // Lấy tên tài khoản từ người dùng
        String ip = "127.0.0.1"; // Địa chỉ IP của máy chủ
        SwingUtilities.invokeLater(() -> new MailClientGUI(accountName, ip).setVisible(true));
    }
}
