/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MONSTER
 */
public class SClient {
    public Socket socket;
    public ObjectInputStream sInput;
    public ObjectOutputStream sOutput;
    public ClientListener listener;
    public String userName;
    
    public SClient(Socket socket)
    {
        try {
            this.socket = socket;
            this.sOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.sInput = new ObjectInputStream(this.socket.getInputStream());
            this.listener = new ClientListener(this);
            this.listener.start();
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(Object msg)
    {
        try {
            this.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ClientListener extends Thread{
    SClient client;
    
    public ClientListener(SClient client)
    {
        this.client = client;
    }

    @Override
    public void run() {
        while (this.client.socket.isConnected()) {            
            try {
                Sender keep = (Sender) this.client.sInput.readObject();
                switch (keep.type) {
                    case ClientSetName:
                        this.client.userName = keep.message.toString();
                        Sender respond = new Sender(Sender.SenderMessage.ClientSetName);
                        respond.message = this.client.userName;
                        client.sendMessage(respond);
                        break;
                    default:
                        throw new AssertionError();
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
