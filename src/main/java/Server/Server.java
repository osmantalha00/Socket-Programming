/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MONSTER
 */
public class Server {
    public ServerSocket socket;
    public int port;
    public ServerListener serverListener;
    
    public static ArrayList<SClient> clients;
    public static ArrayList<Room> rooms;
    
    public Server(int port)
    {
        try {
            this.port = port;
            this.socket = new ServerSocket(this.port);
            this.serverListener = new ServerListener(this);
            this.clients = new ArrayList<SClient>();
            this.rooms = new ArrayList<Room>();
            this.serverListener.start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ServerListener extends Thread{
    
    private Server server;
    
    public ServerListener(Server server)
    {
        this.server = server;
    }

    @Override
    public void run() {
        while (!this.server.socket.isClosed()) {            
            try {
                Socket socket = this.server.socket.accept();
                SClient client = new SClient(socket);
                this.server.clients.add(client);
            } catch (IOException ex) {
                Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
