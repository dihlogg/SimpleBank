package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClientSocket {

    private String Ip;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private Login login;
    private SignUp signUp;

    public ClientSocket(String ip) {
        this.serverPort = 7777; // Cổng máy chủ
        this.Ip = ip; // Địa chỉ IP của máy chủ
        try {
            socket = new DatagramSocket(); // Khởi tạo socket
            serverAddress = InetAddress.getByName(Ip); // Lấy địa chỉ máy chủ
        } catch (Exception e) {
            e.printStackTrace(); // Xử lý ngoại lệ
        }
    }

    public void sendRequest(String request) {
        try {
            byte[] requestData = request.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);

            socket.send(packet); // Gửi yêu cầu

            // Nhận phản hồi từ máy chủ
            byte[] buffer = new byte[65535];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // Chuyển đổi phản hồi thành chuỗi
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);
            System.out.println("Response: " + response);

            // Xử lý phản hồi
            handleResponse(response);

        } catch (IOException e) {
            e.printStackTrace(); // Xử lý ngoại lệ
        }
    }

    private void handleResponse(String response) {
        if (response.startsWith("Login successful")) { // Sửa điều kiện kiểm tra phản hồi
            String[] responseParts = response.split("; Account name: ");
            if (responseParts.length > 1) {
                String emailList = responseParts[0].contains("Danh sách email: ")
                        ? responseParts[0].split("Danh sách email: ")[1].trim()
                        : "";
                String accountName = responseParts[1].trim();

                System.out.println("List Email: " + emailList);
                System.out.println("Account name: " + accountName);

                // Mở giao diện MailClientGUI với accountName
                openMailClientGUI(accountName, emailList, Ip);
            } else {
                System.out.println("Invalid response format for successful login.");
            }
        } else if (response.startsWith("Tạo tài khoản thành công")) {
            signUp = new SignUp(Ip);
            signUp.notify("Tạo tài khoản thành công");
        } else {
            login = new Login(Ip);
            login.notify("Đăng nhập thất bại");
        }
    }



    private void openMailClientGUI(String accountName, String emailList, String IpServer) {
        MailClientGUI mailClientGUI = new MailClientGUI(accountName, IpServer);
        mailClientGUI.setVisible(true); // Hiển thị GUI của MailClient
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close(); // Đóng socket nếu nó chưa bị đóng
        }
    }
}
