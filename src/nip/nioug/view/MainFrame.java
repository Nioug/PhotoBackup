/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.view;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import nip.nioug.model.FilesHandler;
import nip.nioug.photobackup.BackupInfos;
import nip.nioug.photobackup.listener.*;

/**
 *
 * @author Graulich Etienne
 */
public class MainFrame extends JFrame implements BackupInfos, ProgressListener
{
    private final String[] TYPES = {".JPG", ".JPEG", ".TIFF"};
    private final Color myBlue = new Color(60,120,150);
    private final String invite = "Please select the source and destination folder.\n"
            + "You can also chose a prefix for all your pictures and their extension.\n"
            + "Existing files won't be replaced in the destination directories.\n"
            + "(Prefix and extension are not case sensitive.)\n";

    private JPanel panel;
    
    private ArrayList<JComponent> selection = new ArrayList<>();
    private JTextField tfSource = new JTextField(50);    
    private JTextField tfDestination = new JTextField(50);    
    private JTextField tfPrefix = new JTextField(6);  
    private JComboBox cbType = new JComboBox(TYPES);
    private JLabel lExample = new JLabel("ex: ####.JPG");    
    private MyButton bStart = new MyButton("Copy");    
    
    private JProgressBar pbProgress = new JProgressBar(0,100);    
    private JTextArea taDetail = new JTextArea(invite);    
    private JScrollPane scrollDetail = new JScrollPane(taDetail);
    private JButton bDisplay = new JButton("Show detail");    
    
    
    private FilesHandler filesHandler;

    
    public MainFrame()
    {
        this.setTitle(BackupInfos.TITLE);
        this.setSize(500,500);      // (500,500) when processing
        this.setResizable(false);
        Container c = this.getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(Color.WHITE);
        this.setDisplay(c);
    }
 
    
    private void setDisplay(Container c) {
        Border bordered = BorderFactory.createLineBorder(myBlue);
        panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        
        tfSource.setBorder(BorderFactory.createTitledBorder(
                bordered, "Source", 0, 0, null, Color.GRAY));
        tfSource.setBounds(10, 5, 470, 40);
        panel.add(tfSource);
        selection.add(tfSource);
     
        tfDestination.setBorder(BorderFactory.createTitledBorder(
                bordered, "Destination", 0, 0, null, Color.GRAY));
        tfDestination.setBounds(10, 50, 470, 40);
        panel.add(tfDestination);
        selection.add(tfDestination);

        tfPrefix.setBorder(BorderFactory.createTitledBorder(
                bordered, "Prefix", 0, 0, null, Color.GRAY));
        tfPrefix.setBounds(10, 95, 60, 40);   
        tfPrefix.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { updateExample(); }
            @Override
            public void keyPressed(KeyEvent e) { updateExample(); }
            @Override
            public void keyReleased(KeyEvent e) { updateExample(); }
        
        });
        panel.add(tfPrefix);
        selection.add(tfPrefix);
        
        cbType.setBorder(BorderFactory.createTitledBorder(
                bordered, "Type", 0, 0, null, Color.GRAY));
        cbType.setForeground(myBlue);
        cbType.setBackground(Color.WHITE);
        cbType.setBounds(75, 95, 75, 40);
        cbType.addItemListener(l -> this.editExtension(l));
        panel.add(cbType);       
        selection.add(cbType);
        
        lExample.setBounds(160, 105, 300, 25);
        lExample.setForeground(Color.GRAY);
        panel.add(lExample);
        selection.add(lExample);
        
        bStart.setBackground(myBlue);
        bStart.setForeground(Color.WHITE);
        bStart.setBounds(400, 95, 75, 40);
        bStart.addActionListener(a -> performCopy());
        panel.add(bStart);
        selection.add(bStart);
        
        pbProgress.setBounds(10, 145, 470, 25);
        pbProgress.setStringPainted(true);
        pbProgress.setString("- / -");
        panel.add(pbProgress);
        
        
        taDetail.setEditable(false);
        taDetail.setBounds(10, 180, 470, 280);
        scrollDetail.setBounds(10, 180, 470, 280);
        scrollDetail.setBackground(Color.WHITE);
        scrollDetail.setBorder(bordered);
        panel.add(scrollDetail);        
        
        c.add(panel);        
    }
    
    
    private void performCopy() {
        if (this.checkValues()) {
            this.filesHandler = new FilesHandler(
                    Paths.get(tfSource.getText()),
                    Paths.get(tfDestination.getText()),
                    BackupInfos.getRegex(
                            tfPrefix.getText(),
                            cbType.getSelectedItem().toString()
                    )
            );
            this.filesHandler.addProgressListener(this);
            this.filesHandler.start();
        }
    }
    
    private boolean checkValues() {
        if (tfSource.getText().isEmpty())
            tfSource.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED), 
                "Source (can not be empty)", 0, 0, null, Color.GRAY));
        else
            tfSource.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(myBlue), 
                "Source", 0, 0, null, Color.GRAY));
        
        if (tfDestination.getText().isEmpty())
            tfDestination.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED), 
                "Destination (can not be empty)", 0, 0, null, Color.GRAY));
        else
            tfDestination.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(myBlue), 
                "Destination", 0, 0, null, Color.GRAY));
        
        return !tfSource.getText().isEmpty() 
                && !tfDestination.getText().isEmpty();
    }
    
    
    private void editExtension(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            lExample.setText("ex: " + tfPrefix.getText() + "####" + e.getItem().toString());
        }
    }
    
    private void updateExample(){
        lExample.setText("ex: " + tfPrefix.getText() + "####" + cbType.getSelectedItem().toString());
    }
    
    
    private void switchComponents(ArrayList<JComponent> list) {
        if (!list.isEmpty()) {
            boolean status = list.get(0).isEnabled();
            list.stream().forEach(c -> c.setEnabled(!status));
        }            
    }
    
    private void switchComponents(ArrayList<JComponent> list, boolean enabled) {
        if (!list.isEmpty()) {
            list.stream().forEach(c -> c.setEnabled(enabled));
        }            
    }
    

    @Override
    public void addMessage(ProgressMessageEvent message) {
        String msg = "";
        if (message.getStatus() == ProgressMessageEvent.ERROR)
            msg += "ERROR: " + message.getMessage();
        else
            msg += message.getMessage();
        msg += "\n";
        taDetail.append(msg);
        taDetail.setCaretPosition(taDetail.getDocument().getLength());
    }

    @Override
    public void editCount(ProgressCountEvent event) {
        int current = event.getCurrent();
        int total = event.getTotal();
        pbProgress.setMaximum(total);
        pbProgress.setValue(current);
        pbProgress.setString(current + "/" + total);
    }
    
    @Override
    public void changeStatus(boolean running) {
        this.switchComponents(selection, !running);
    }
}
