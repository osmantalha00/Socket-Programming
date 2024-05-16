package Client;

import Message.Message;
import Server.SClient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import Ekranlar.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Client implements java.io.Serializable {

    public static Socket socket;
    public static ObjectInputStream sInput;
    public static ObjectOutputStream sOutput;
    public static boolean isPaired = false;
    public static ListenThread listen;
    public static String clientName; // yeni ekledim son 

    public static void StartClient(String ip, int port) {
        try {
            // server ve socket baglantilari
            Client.socket = new Socket(ip, port);
            Client.listen = new ListenThread();
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listen.start();

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Stop() {
        try {
            if (Client.socket != null) {
                Client.listen.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();
                Client.sInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Display(String message) {
        System.out.println(message);
    }

    public static void SendServer(Message msg) {
        try {
            Client.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class ListenThread extends Thread implements java.io.Serializable {

    @Override
    public void run() {
        while (Client.socket.isConnected()) {
            try {
                Message msg = (Message) Client.sInput.readObject();
                switch (msg.type) {
                    case NAME:
                        break;
                    case PROJE_NAME: // // Gelen proje adını büyük harflerle görüntüleyerek chat penceresini göster

                        Login.menu.chat.roomlbl.setText(msg.content.toString().toUpperCase());
                        Login.menu.chat.setVisible(true);
                        break;
                    case LIST: //  Liste mesajı alındığında, mevcut liste elemanlarını temizle ve 
                               // ve yeni elemanları ekle
                        Login.menu.dlm.removeAllElements();
                        ArrayList<String> receivedNames = new ArrayList();
                        receivedNames = (ArrayList<String>) msg.content;
                        for (String item : receivedNames) {
                            Login.menu.dlm.addElement(item);
                        }
                        break;
                    case PROJE_LIST:
                        // // Proje listesi mesajı alındığında,
                        // mevcut liste elemanlarını temizle ve yeni elemanları ekle
                        Login.menu.dlm2.removeAllElements();
                        ArrayList<String> receivedRooms = new ArrayList();
                        receivedRooms = (ArrayList<String>) msg.content;
                        for (String item : receivedRooms) {
                            Login.menu.dlm2.addElement(item);
                        }
                        break;
                    case JOIN_ROOM:
                        // // Odaya katılma mesajı alındığında, eğer katılım başarısız ise hata mesajı göster
                         // Başarılı ise oda adını büyük harflerle görüntüleyerek chat penceresini göste
                        // msg.content == temp (NO or room name)
                        if (msg.content.equals("NO")) {
                            JOptionPane.showMessageDialog(Login.menu, "Room name Failure!!");
                            return;
                        }
                        Login.menu.chat.roomlbl.setText(msg.content.toString().toUpperCase());
                        Login.menu.chat.setVisible(true);
                        break;
                    case ReLoad:
                        // // Yeniden yükleme mesajı alındığında, mevcut liste elemanlarını temizle ve yeni elemanları ekle
                        Login.menu.chat.dlm.removeAllElements();
                        ArrayList<String> roomsClients = new ArrayList();
                        roomsClients = (ArrayList<String>) msg.content;
                        for (String item : roomsClients) {
                            Login.menu.chat.dlm.addElement(item);
                        }
                        break;
                    case START_CHAT:
                        // msg.content == "DECIDE"
                        // msg.whoWantsToTalk == username who click start chat button
                        // selection: receiver users decide. Is he/she wants to talk or not ?
                        int selection = 0;
                        if (msg.content.equals("DECIDE")) {
                            selection = JOptionPane.showConfirmDialog(Login.menu, msg.whoWantsToTalk + " wants to talk you? Do you accept?", "Chat Request", JOptionPane.YES_OPTION);
                        }

                        if (selection == JOptionPane.YES_OPTION) {
                            Login.menu.singleChat.setTitle(msg.whoWantsToTalk.toUpperCase());
                            Login.menu.singleChat.setVisible(true);

                            Message decide = new Message(Message.Message_Type.SELECTION);
                            decide.content = selection; // if option is yes selection equals 0, if it is not then selection eq 1
                            decide.senderName = Login.menu.jLabel1.getText().substring(3, Login.menu.jLabel1.getText().length() - 1);
                            decide.whoWantsToTalk = msg.whoWantsToTalk;
                            Client.SendServer(decide);
                        }

                        break;
                    case SELECTION_FINISH:
                        // // SELECTION_FINISH mesajı alındığında, eğer içerik "OPEN" ise istemci sohbeti kabul etmiş demektir
                        // if msg.content open, so it means client accept to chat
                        if (msg.content.equals("OPEN")) {
                            Login.menu.singleChat.setTitle(msg.senderName.toUpperCase());
                            Login.menu.singleChat.setVisible(true);
                        }
                        break;
                    case P2P_TEXT:
                        Login.menu.singleChat.p2p_textArea.append(msg.content.toString() + "\n");
                        break;
                    case P2P_FILE:
//                   // P2P_FILE mesajı alındığında, gelen dosyayı kullanıcının masaüstüne kaydet
                        String homePath = System.getProperty("user.home");
                        File receivedFile = new File(homePath + "/Desktop/" + msg.content);
                        OutputStream os = new FileOutputStream(receivedFile);
                        byte content[] = (byte[]) msg.fileContent;
                        os.write(content);
                        break;
                    case P2P_FILE_NOTIFY:
                        Login.menu.singleChat.p2p_textArea.append(msg.content.toString() + "\n");
                        break;
                    case TEXT:
                        Login.menu.chat.Chat_textArea.append(msg.content.toString() + "\n");
                        break;
                    case RECEIVED_ROOM_FILE:
                        // // Odadan alınan dosyayı kullanıcının masaüstüne kaydet
                        String homePath1 = System.getProperty("user.home");
                        File receivedFile1 = new File(homePath1 + "/Desktop/" + msg.content);
                        OutputStream os1 = new FileOutputStream(receivedFile1);
                        byte content1[] = (byte[]) msg.fileContent;
                        os1.write(content1);
                        break;
                    case ROOM_FILE_RECEIVED_NOTIFICATION:
                        Login.menu.chat.Chat_textArea.append(msg.content.toString() + "\n");
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
