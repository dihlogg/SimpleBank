package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Login extends JFrame {

    private JPanel contentPane;
    private JTextField txtUserName; // Đổi tên thành txtUsername
    private JPasswordField txtPassword;
    private String sepa = "#####";

    public Login(String Ip) {
        ClientSocket cs = new ClientSocket(Ip);
        setTitle("Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 487, 400);
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(new Color(135, 206, 250));
        setContentPane(contentPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Thêm khoảng cách giữa các thành phần

        // Tiêu đề
        JLabel lbLogin = new JLabel("Sign In");
        lbLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
        lbLogin.setForeground(Color.red);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(lbLogin, gbc);

        // Nhãn Username
        JLabel lbUsername = new JLabel("User:");
        lbUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(lbUsername, gbc);

        // Trường Username
        txtUserName = new JTextField(20); // Sử dụng lại txtMail như trường nhập cho tên đăng nhập
        gbc.gridx = 1;
        contentPane.add(txtUserName, gbc);

        // Nhãn Mật khẩu
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(lblPass, gbc);

        // Trường Mật khẩu
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        contentPane.add(txtPassword, gbc);

        // Nút Hiện mật khẩu
        final JCheckBox chckbxShowPassword = new JCheckBox("Show");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(chckbxShowPassword, gbc);

        // Nút Đăng nhập
        JButton btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        contentPane.add(btnLogin, gbc);

        // Nút Đăng ký
        JButton btnSignUp = new JButton("Sign Up");
        btnSignUp.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 1;
        contentPane.add(btnSignUp, gbc);

        // Sự kiện cho nút Đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String accountName = txtUserName.getText().trim(); // Giữ nguyên logic
                String password = String.valueOf(txtPassword.getPassword());
                if (!accountName.isEmpty()) {
                    cs.sendRequest("LOGIN " + accountName + sepa + password);
                    setVisible(false);
                }
            }
        });

        // Giao diện phần welcome
        JLabel lblWelcome = new JLabel("");
        lblWelcome.setIcon(new ImageIcon("D:\\Monhoc\\laptrinhhuongdoituong\\iconoop\\iconwelcome.png"));
        lblWelcome.setBounds(222, 50, 50, 50);
        contentPane.add(lblWelcome);

        // Sự kiện cho nút Đăng ký
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignUp su = new SignUp(Ip);
                su.setVisible(true);
                setVisible(false);
            }
        });

        // Sự kiện cho checkbox Hiện mật khẩu
        chckbxShowPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chckbxShowPassword.isSelected()) {
                    txtPassword.setEchoChar((char) 0);
                } else {
                    txtPassword.setEchoChar('*');
                }
            }
        });
    }

    public void notify(String notify) {
        JOptionPane.showMessageDialog(this, notify, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
