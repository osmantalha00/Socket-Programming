/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bilgisayar_aglar_proje1;

import Server.Sender;
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
public class Client {
    public Socket socket;
    public ObjectInputStream sInput;
    public ObjectOutputStream sOutput;
    public String userName;
    public String serverIP;
    public int serverPort;
    public ServerListener serverListener;
    
    public void Connect(String serverIP, int port){
        try {
            this.serverIP = serverIP;
            this.serverPort = port;
            this.socket = new Socket(this.serverIP, this.serverPort);
            this.sOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.sInput = new ObjectInputStream(this.socket.getInputStream());
            this.serverListener = new ServerListener(this);
            this.serverListener.start();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(Object message){
        try {
            this.sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ServerListener extends Thread{
    
    Client client;
    
    public ServerListener(Client client)
    {
        this.client = client;
    }
    
    @Override
    public void run() {
        while (!this.client.socket.isClosed()) {            
            try {
                Sender keepMessage = (Sender) (this.client.sInput.readObject());
                
                switch (keepMessage.type) {
                    
                    default:
                        throw new AssertionError();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
