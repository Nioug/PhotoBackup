/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.view;

import java.awt.Color;
import javax.swing.JButton;

/**
 *
 * @author Graulich Etienne
 */
public class MyButton extends JButton {
    private final Color myBlue = new Color(60,120,150);
    private final Color disabled = new Color(180,180,180);
    
    private final Color defaultColor;
    
    public MyButton(String title) {
        super(title);
        this.setBackground(myBlue);
        this.setForeground(Color.WHITE);    
        defaultColor = myBlue;
    }
    
    public MyButton(String title, Color color) {
        super(title);
        this.setBackground(myBlue);
        this.setForeground(Color.WHITE);    
        defaultColor = color;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled){
            super.setBackground(defaultColor);
        }
        else {
            super.setBackground(disabled);
        }
    }
}
