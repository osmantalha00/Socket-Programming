/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author MONSTER
 */
public class Sender implements java.io.Serializable{
    public static enum SenderMessage{
        ClientSetName
    }
    
    public SenderMessage type;
    public Object message;
    
    public Sender(SenderMessage type)
    {
        this.type = type;
    }
}
