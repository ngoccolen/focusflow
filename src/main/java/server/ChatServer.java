package server;

import java.net.*;
import java.io.*;
import java.util.*;
import model.Message;
import server.ClientHandler;

public class ChatServer {
 //   private static List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
    private static Map<Integer, List<ObjectOutputStream>> chatIdToClients = new HashMap<>();
    
    private static int chatIdCounter = 1000; // hoặc bất kỳ số bắt đầu nào

    public static synchronized int generateNewChatId() {
        return chatIdCounter++;
    }

    public static void addClientToChat(int chatId, ObjectOutputStream out) {
        chatIdToClients.computeIfAbsent(chatId, k -> new ArrayList<>()).add(out);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Chat server is running...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
  //          clientOutputStreams.add(out);

            new ClientHandler(clientSocket, out).start();
        }
    }

    public static void broadcastToChat(Message message) {
        int chatId = message.getChatId();
        List<ObjectOutputStream> clients = chatIdToClients.get(chatId);
        if (clients == null) return;

        for (ObjectOutputStream out : clients) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 // Server phụ xử lý ảnh/file gửi lên
    static {
        new Thread(() -> {
            try (ServerSocket fileServerSocket = new ServerSocket(12346)) {
                System.out.println("File server is running on port 12346...");

                while (true) {
                    Socket fileSocket = fileServerSocket.accept();
                    new Thread(() -> handleFileMessage(fileSocket)).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

 private static void handleFileMessage(Socket fileSocket) {
    try {
        InputStream rawInput = fileSocket.getInputStream();
        ObjectInputStream in = new ObjectInputStream(rawInput);
        Message msg = (Message) in.readObject();

        System.out.println("Đang nhận file: " + msg.getFilePath() + ", dung lượng " + msg.getFileSize() + " bytes");

        // Chuẩn bị thư mục
        File dir = msg.getType().equals("image") ? new File("temp_images") : new File("temp_files");
        if (!dir.exists()) dir.mkdirs();

        // Ghi file từ rawInput
        File savedFile = new File(dir, msg.getFilePath());
        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            byte[] buffer = new byte[4096];
            long remaining = msg.getFileSize();
            while (remaining > 0) {
                int bytesRead = rawInput.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        System.out.println("✅ File saved: " + savedFile.getAbsolutePath());

        // Gửi lại cho các client
        //broadcast(msg);
        broadcastFileToClients(msg, savedFile);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
 public static void broadcastFileToClients(Message msg, File savedFile) {
	    int chatId = msg.getChatId();
	    List<ObjectOutputStream> clients = chatIdToClients.get(chatId);
	    if (clients == null) return;

	    for (ObjectOutputStream out : clients) {
	        try {
	            Socket sendFileSocket = new Socket("localhost", 12347);
	            ObjectOutputStream objOut = new ObjectOutputStream(sendFileSocket.getOutputStream());
	            objOut.writeObject(msg);
	            objOut.flush();

	            OutputStream rawOut = sendFileSocket.getOutputStream();
	            FileInputStream fileIn = new FileInputStream(savedFile);
	            byte[] buffer = new byte[4096];
	            int bytesRead;
	            while ((bytesRead = fileIn.read(buffer)) != -1) {
	                rawOut.write(buffer, 0, bytesRead);
	            }

	            fileIn.close();
	            rawOut.flush();
	            sendFileSocket.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}


}