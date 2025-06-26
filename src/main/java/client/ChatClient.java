package client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import javafx.application.Platform;
import model.Message;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ChatClient() throws IOException {
        socket = new Socket("localhost", 12345); // Nếu server online khác IP
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    System.out.println("Received: " + msg.getContent());

                    // Gọi callback cập nhật UI
                    javafx.application.Platform.runLater(() -> {
                        if (onMessageReceived != null) {
                            onMessageReceived.onReceived(msg);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try (ServerSocket receiveFileSocket = new ServerSocket(12347)) {
                while (true) {
                    Socket fileSocket = receiveFileSocket.accept();
                    new Thread(() -> receiveFile(fileSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


    }
    public interface MessageListener {
        void onReceived(Message message);
    }
    private MessageListener onMessageReceived;

    public void setOnMessageReceived(MessageListener listener) {
        this.onMessageReceived = listener;
    }

    public void sendMessage(String sender, String content, int chatId, String avatar) throws IOException {
        Message msg = new Message(sender, content, "text", LocalDateTime.now());
        msg.setChatId(chatId);
        msg.setSenderAvatar(avatar); // ✅ thêm avatar
        out.writeObject(msg);
        out.flush();
    }

    public void sendFile(String sender, File file, String type, int chatId, String avatar) throws IOException {
        Socket fileSocket = new Socket("localhost", 12346);

        ObjectOutputStream objOut = new ObjectOutputStream(fileSocket.getOutputStream());

        Message msg = new Message(sender, file.getName(), type, LocalDateTime.now());
        msg.setFilePath(file.getAbsolutePath()); // ✅ Đường dẫn tuyệt đối
        msg.setFileSize(file.length());
        msg.setChatId(chatId);
        msg.setSenderAvatar(avatar);

        objOut.writeObject(msg);
        objOut.flush();

        OutputStream rawOut = fileSocket.getOutputStream();
        FileInputStream fileIn = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileIn.read(buffer)) != -1) {
            rawOut.write(buffer, 0, bytesRead);
        }

        fileIn.close();
        rawOut.flush();
        fileSocket.close();
    }


 private void receiveFile(Socket fileSocket) {
	    try {
	        ObjectInputStream in = new ObjectInputStream(fileSocket.getInputStream());
	        Message msg = (Message) in.readObject();

	        // ✅ Tạo thư mục lưu ảnh riêng biệt trong Downloads
	        String folderName = msg.getType().equals("image") ? "FocusFlow_Images" : "FocusFlow_Files";
	        String downloadPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + folderName;
	        Files.createDirectories(Paths.get(downloadPath));

	        File file = new File(downloadPath, msg.getContent());

	        try (FileOutputStream fos = new FileOutputStream(file);
	             InputStream rawIn = fileSocket.getInputStream()) {

	            byte[] buffer = new byte[4096];
	            long remaining = msg.getFileSize();
	            int bytesRead;

	            while (remaining > 0 && (bytesRead = rawIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
	                fos.write(buffer, 0, bytesRead);
	                remaining -= bytesRead;
	            }
	        }

	        // ✅ Gán đường dẫn thực tế cho message
	        msg.setFilePath(file.getAbsolutePath());
	        System.out.println("✅ File saved to: " + file.getAbsolutePath());

	        Platform.runLater(() -> {
	            if (onMessageReceived != null) {
	                onMessageReceived.onReceived(msg);
	            }
	        });
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.err.println("❌ Error receiving file: " + e.getMessage());
	    }
	}

    public void joinChat(int chatId) throws IOException {
        Message joinMsg = new Message();
        joinMsg.setType("join");
        joinMsg.setChatId(chatId);
        out.writeObject(joinMsg);
        out.flush();
    }
    public void createGroupChat(String groupName, List<Integer> memberIds) throws IOException {
        Message msg = new Message();
        msg.setType("create_group");
        msg.setContent(groupName);
        // Giả sử tạm: nhét danh sách ID thành chuỗi để truyền
        msg.setFilePath(memberIds.toString()); // dùng tạm field filePath để chứa memberIds
        out.writeObject(msg);
        out.flush();
    }
    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }


}
