/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.util.ArrayList;

/**
 *
 * @author MONSTER
 */
public class Room {
    public String name;
    public ArrayList<String> usersNames;
    
    public Room(String name, String roomOwnerName)
    {
        usersNames = new ArrayList<String>();
        this.name = name;
        this.usersNames.add(roomOwnerName);
    }
}
