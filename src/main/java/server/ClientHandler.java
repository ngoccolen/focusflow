package server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.Session;

import Util.HibernateUtil;
import model.ChatConversation;
import model.ChatMember;
import model.User;
import model.Message;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, ObjectOutputStream out) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();

                // ✅ Nếu là yêu cầu "join" để tham gia nhóm chat
                if ("join".equals(message.getType())) {
                    int chatId = message.getChatId();

                    ChatServer.addClientToChat(chatId, out);


                    System.out.println("Client joined chatId: " + chatId);
                    continue; // bỏ qua gửi broadcast cho loại join
                }

                // ✅ Nếu là ảnh/file thì xử lý như cũ (nếu cần)
                if ("image".equals(message.getType()) || "file".equals(message.getType())) {
                    File tempDir = new File("temp_images");
                    if (!tempDir.exists()) {
                        tempDir.mkdir();
                    }

                    String savePath = "temp_images/" + message.getContent();
                    File savedFile = new File(savePath);
                    FileOutputStream fileOut = new FileOutputStream(savedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long remaining = message.getFileSize();

                    InputStream rawIn = socket.getInputStream();
                    while ((bytesRead = rawIn.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
                        fileOut.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                        if (remaining <= 0) break;
                    }

                    fileOut.close();
                    message.setFilePath(savePath);

                    // ✅ Gửi lại file tới các client khác trong nhóm
                    ChatServer.broadcastFileToClients(message, savedFile);

                    continue; // Không broadcast lại nữa vì đã broadcast nội dung file rồi
                }
                if ("create_group".equals(message.getType())) {
                    String groupName = message.getContent();
                    String memberIdStr = message.getFilePath(); // ví dụ: "[1, 2, 3]"

                    // 1. Parse danh sách ID từ chuỗi JSON dạng [1,2,3]
                    List<Integer> memberIds = parseMemberIds(memberIdStr);

                    // 2. Mở Hibernate session để lưu nhóm
                    Session session = HibernateUtil.getSessionFactory().openSession();
                    session.beginTransaction();

                    // 3. Tạo ChatConversation mới
                    ChatConversation chat = new ChatConversation();
                    chat.setGroup(true);
                    chat.setGroupName(groupName); // ✅ Tên nhóm lưu vào DB
                    chat.setCreatedAt(LocalDateTime.now());
                    session.save(chat); // chatId được tự sinh

                    // 4. Thêm các thành viên vào nhóm
                    for (Integer id : memberIds) {
                        ChatMember member = new ChatMember();
                        member.setChat(chat);
                        User user = session.get(User.class, id);
                        if (user != null) {
                            member.setUser(user);
                            session.save(member);
                        }
                    }

                    session.getTransaction().commit();
                    session.close();

                    // 5. Đăng ký client đang kết nối (hiện tại) vào nhóm
                    ChatServer.addClientToChat(chat.getChatId(), out); // out của người tạo nhóm

                    // 6. Log
                    System.out.println("📦 Nhóm mới: " + groupName + ", chatId = " + chat.getChatId() + ", members = " + memberIds);

                    // 7. (Gợi ý nâng cao) Trả về thông báo cho client biết nhóm đã được tạo (tuỳ client muốn hiển thị không)
                    Message confirmation = new Message("server", "Group created", "group_created", LocalDateTime.now());
                    confirmation.setChatId(chat.getChatId());
                    confirmation.setContent(groupName);
                    out.writeObject(confirmation);
                    out.flush();

                    return;
                }


                // ✅ Gửi cho đúng nhóm người dùng
                ChatServer.broadcastToChat(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private List<Integer> parseMemberIds(String str) {
        List<Integer> list = new ArrayList<>();
        str = str.replaceAll("\\[|\\]|\\s", ""); // xóa [, ], space
        if (str.isEmpty()) return list;

        for (String s : str.split(",")) {
            try {
                list.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

}
