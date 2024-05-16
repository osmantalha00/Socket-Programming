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
    public ArrayList<String> userNamesList = new ArrayList();
    
    public Room(String name, String creatorName){
        this.name = name;
        userNamesList.add(creatorName);
    }
    
    
}
