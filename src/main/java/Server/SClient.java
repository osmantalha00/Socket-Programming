package Server;

import Message.Message;
import Message.Message.Message_Type;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import Server.Room;
import Ekranlar.Login;
import javax.swing.JOptionPane;

public class SClient implements java.io.Serializable {

    int clientID;
    Socket socket;
    public String name = "NoName";
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;
    // dinleme threadi
    Listen listenThread;

    public SClient(int clientID, Socket socket) {
        try {
            this.socket = socket;
            this.clientID = clientID;
            this.listenThread = new Listen(this);
            //this.pairThread = new PairingThread(this);
            this.sOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.sInput = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //client mesaj gönderme
    public void Send(Message message) {
        try {
            this.sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Disconnect() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class Listen extends Thread implements java.io.Serializable {

        SClient sclient;

        public Listen(SClient sclient) {
            this.sclient = sclient;
        }

        @Override
        public void run() {
            while (sclient.socket.isConnected()) {
                try {
                    Message msg = (Message) sclient.sInput.readObject();
                    switch (msg.type) {
                        case NAME:
                            //// NAME mesajı alındığında, gelen ismi konsola yaz ve SClient nesnesinin ismini güncelle
                            System.out.println("gelen name mesajı: " + msg.content.toString());
                            sclient.name = msg.content.toString();
                            break;
                        case PROJE_NAME:
                            //  // PROJE_NAME mesajı alındığında, gelen oda adını konsola yaz, yeni bir Room oluştur ve Server.rooms listesine ekle
                            System.out.println("gelen room name mesajı: " + msg.content.toString());
                            Room newRoom = new Room(msg.content.toString(), sclient.name);
                            Server.rooms.add(newRoom);
                            Message roomMSG = new Message(Message.Message_Type.PROJE_NAME);
                            roomMSG.content = newRoom.name;
                            Server.SendClient(this.sclient, roomMSG);
                            break;
                        case LIST:
                            // // LIST mesajı alındığında, mevcut tüm kullanıcı adlarını al ve bu bilgiyi gönderen istemciye geri gönder
                            ArrayList<String> usernames = new ArrayList<String>();
                            for (SClient item : Server.sclients) {
                                usernames.add(item.name);
                            }
                            Message mesaj = new Message(Message_Type.LIST);
                            mesaj.content = usernames;
                            Server.SendClient(this.sclient, mesaj);
                            break;
                        case PROJE_LIST:
                            //  // PROJE_LIST mesajı alındığında, mevcut tüm proje adlarını alarak bu bilgiyi gönderen istemciye geri gönder
                            ArrayList<String> roomNames = new ArrayList<String>();
                            for (Room item : Server.rooms) {
                                roomNames.add(item.name);
                            }
                            Message roomListMsg = new Message(Message_Type.PROJE_LIST);
                            roomListMsg.content = roomNames;
                            Server.SendClient(this.sclient, roomListMsg);
                            break;
                        case JOIN_ROOM:
                            // JOIN_ROOM mesajı alındığında, istemcinin katılmak istediği oda adını kontrol et
                            // Eğer oda bulunursa, istemciyi odaya ekleyerek katılımı sağla
                            
                            String tempRoomName = "NO";
                            for (Room item : Server.rooms) {
                                if (item.name.equals(msg.content)) {
                                    tempRoomName = item.name;
                                    item.userNamesList.add(this.sclient.name);
                                    break;
                                }
                            }
                            Message roomNameMSG = new Message(Message.Message_Type.JOIN_ROOM);
                            roomNameMSG.content = tempRoomName;
                            Server.SendClient(this.sclient, roomNameMSG);
                            break;
                        case ReLoad:
                            // // ReLoad mesajı alındığında, istemcinin yeniden yüklenmesi gereken odanın kullanıcı adlarını al ve gönder
                            
                            ArrayList<String> clientNames = new ArrayList();
                            for (Room room : Server.rooms) {
                                if (room.name.equals(msg.content.toString())) {
                                    for (String client : room.userNamesList) {
                                        clientNames.add(client);
                                    }
                                }
                            }
                            Message clientsMSG = new Message(Message_Type.ReLoad);
                            clientsMSG.content = clientNames;
                            Server.SendClient(this.sclient, clientsMSG);
                            break;
                        case START_CHAT:
                            
                            Message decideMSG = new Message(Message.Message_Type.START_CHAT);
                            decideMSG.content = (String) "DECIDE";
                            decideMSG.whoWantsToTalk = msg.senderName;
                            for (SClient sclient : Server.sclients) {
                                if (sclient.name.equals(msg.content)) {
                                    Server.SendClient(sclient, decideMSG);
                                    break;
                                }
                            }
                            break;
                        case SELECTION:
                            // msg.content == selection ---> YES = 0, NO = 1
                            if (msg.content.equals(0)) {
                                Message finish = new Message(Message.Message_Type.SELECTION_FINISH);
                                finish.content = "OPEN";
                                finish.senderName = msg.senderName;
                                finish.whoWantsToTalk = msg.whoWantsToTalk;
                                for (SClient item : Server.sclients) {
                                    if (item.name.equals(msg.whoWantsToTalk)) {
                                        Server.SendClient(item, finish);
                                        break;
                                    }
                                }  
                            }              
                            break;     
                        case P2P_TEXT:
                            Message textMSG = new Message(Message.Message_Type.P2P_TEXT);
                            textMSG.content = this.sclient.name.toUpperCase() + ": " + msg.content;
                            Server.SendClient2(textMSG);
                            break;
                        case P2P_FILE:
                            Server.SendClient2(msg);
                            break;
                        case P2P_FILE_NOTIFY:
                            Server.SendClient2(msg);
                            break;
                        case TEXT:
                            Message chatMSG = new Message(Message.Message_Type.TEXT);
                            chatMSG.content = this.sclient.name.toUpperCase() + ": " + msg.content;
                            Server.SendClient2(chatMSG);
                            break;
                        case RECEIVED_ROOM_FILE:
                            Server.SendClient2(msg);
                            break;
                        case ROOM_FILE_RECEIVED_NOTIFICATION:
                            Server.SendClient2(msg);
                            break;
                    }
                } catch (IOException ex) {
                    this.sclient.Disconnect();
                    System.out.println("Listen Thread Exceptionnn: " + ex);
                    return;
                } catch (ClassNotFoundException ex) {
                    System.out.println("Class Not Foundddd: " + ex);;
                } catch (IllegalThreadStateException te) {
                    System.out.println("Illegal Threaddd: " + te);
                }
            }
        }

    }
}
