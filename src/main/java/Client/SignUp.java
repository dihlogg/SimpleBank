package Client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUp extends JFrame {

    private JPanel contentPane;
    private JTextField txtUserName;
    private JPasswordField txtPassword;
    private static final String sepa = "#####";
    private LogPanel logPanel; 
    public SignUp(String ip) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 487, 400);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(135, 206, 250)); // Màu nền nhạt
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Tiêu đề
        JLabel lblTitle = new JLabel("Sign up account");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTitle.setForeground(new Color(25, 25, 112)); // Màu chữ
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 30, 487, 30);
        contentPane.add(lblTitle);

     // Textfield của email
        txtUserName = new JTextField();
        txtUserName.setBounds(162, 110, 256, 40); // Tăng chiều cao lên 40
        contentPane.add(txtUserName);
        txtUserName.setColumns(10);
        txtUserName.setBorder(BorderFactory.createTitledBorder("Gmail"));

        // Password field
        txtPassword = new JPasswordField();
        txtPassword.setBounds(162, 157, 256, 40); // Tăng chiều cao lên 40
        contentPane.add(txtPassword);
        txtPassword.setBorder(BorderFactory.createTitledBorder("Pass"));


        // Nút Đăng ký
        JButton btnSignUp = new JButton("Sign up");
        btnSignUp.setFont(new Font("Arial", Font.BOLD, 14));
        btnSignUp.setBackground(new Color(25, 25, 112)); // Màu nền nút
        btnSignUp.setForeground(Color.BLACK);
        btnSignUp.setBounds(80, 220, 100, 30);
        contentPane.add(btnSignUp);

        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String accountName = txtUserName.getText().trim();
                String password = String.valueOf(txtPassword.getPassword()).trim();
                if (!accountName.isEmpty()) {
                    new ClientSocket(ip).sendRequest("CREATE_ACCOUNT " + accountName + sepa + password);
                } else {
                	logPanel.logMessage("Vui lòng nhập tên tài khoản hợp lệ.");
                }
            }
        });

        // Nút Đăng nhập
        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(25, 25, 112)); // Màu nền nút
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setBounds(260, 220, 100, 30);
        contentPane.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Login login = new Login(ip);
                login.setVisible(true);
                setVisible(false);
            }
        });
    }

    public void notify(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
