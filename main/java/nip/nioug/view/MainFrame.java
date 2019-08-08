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
    private final String COPY = "Copy";
    private final String STOP = "Stop";
    private final String[] TYPES = {".JPG", ".JPEG", ".TIFF"};
    private final Color myBlue = new Color(60,120,150);
    private final String invite = "Please select the source and destination folder.\n"
            + "You can also chose a prefix for all your pictures and their extension.\n"
            + "Existing files won't be replaced in the destination directories.\n"
            + "(Prefix and extension are not case sensitive.)\n";

    private JPanel panel;
    
    private ArrayList<JComponent> selection = new ArrayList<>();
    private JTextField tfSource = new JTextField(50);    
    private MyButton bChooseSource = new MyButton("...");
    private JTextField tfDestination = new JTextField(50);  
    private MyButton bChooseDestination = new MyButton("...");
    private JTextField tfPrefix = new JTextField(6);  
    private JComboBox cbType = new JComboBox(TYPES);
    private JLabel lExample = new JLabel("ex: ####.JPG");    
    private MyButton bCopy = new MyButton("Copy");  
    private boolean copying = false;
    
    private JProgressBar pbProgress = new JProgressBar(0,100);    
    private JTextArea taDetail = new JTextArea(invite);    
    private JScrollPane scrollDetail = new JScrollPane(taDetail);
    private JButton bDisplay = new JButton("Show detail");    
    
    private final JFileChooser fc;
    
    private FilesHandler filesHandler;

    
    public MainFrame()
    {
        this.setTitle(BackupInfos.TITLE);
        this.setSize(510,510);      // (500,500) when processing
        this.setResizable(false);
        Container c = this.getContentPane();
        c.setLayout(null);
        c.setBackground(Color.WHITE);
        this.setDisplay(c);
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
 
    
    private void setDisplay(Container c) {
        Border bordered = BorderFactory.createLineBorder(myBlue);
        
        panel = new JPanel(null);
        panel.setBounds(0, 0, 500, 500);
        panel.setBackground(Color.WHITE);
        
        tfSource.setBorder(BorderFactory.createTitledBorder(
                bordered, "Source", 0, 0, null, Color.GRAY));
        tfSource.setBounds(10, 5, 440, 40);
        panel.add(tfSource);
        selection.add(tfSource);
        
        bChooseSource.setBounds(455, 13, 30, 30);
        bChooseSource.addActionListener(l -> fillTextField(tfSource));
        panel.add(bChooseSource);
        selection.add(bChooseSource);
     
        tfDestination.setBorder(BorderFactory.createTitledBorder(
                bordered, "Destination", 0, 0, null, Color.GRAY));
        tfDestination.setBounds(10, 50, 440, 40);
        panel.add(tfDestination);
        selection.add(tfDestination);
        
        bChooseDestination.setBounds(455, 58, 30, 30);
        bChooseDestination.addActionListener(l -> fillTextField(tfDestination));
        panel.add(bChooseDestination);
        selection.add(bChooseDestination);

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
        
        bCopy.setBounds(400, 95, 85, 40);
        bCopy.addActionListener(a -> performCopy());
        panel.add(bCopy);
        //selection.add(bCopy);
        
        pbProgress.setBounds(10, 145, 475, 25);
        pbProgress.setStringPainted(true);
        pbProgress.setString("- / -");
        panel.add(pbProgress);
        
        
        taDetail.setEditable(false);
        scrollDetail.setBounds(10, 180, 475, 280);
        scrollDetail.setBackground(Color.WHITE);
        scrollDetail.setBorder(bordered);
        panel.add(scrollDetail);        
        
        c.add(panel);        
    }
    
    
    private void fillTextField(JTextField comp){
        int returnVal = fc.showOpenDialog(MainFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String file = fc.getSelectedFile().toPath().toString();
            comp.setText(file);
        }
    }
    
    
    private void performCopy() {
        if (!copying) {
            if (this.checkValues()) {
                this.addMessage(new ProgressMessageEvent(
                        ProgressMessageEvent.SUCCESS, 
                        "Backup started"));
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
        else {
            this.filesHandler.interrupt();
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
    
    
    private void switchComponents(ArrayList<JComponent> list, boolean enabled) {
        if (!list.isEmpty()) {
            list.stream().forEach(c -> c.setEnabled(enabled));
            if (enabled) {
                bCopy.setText(COPY);
            } 
            else {
                bCopy.setText(STOP);
            }
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
        pbProgress.setMaximum(event.getTotal());
        pbProgress.setValue(event.getCurrent());
        pbProgress.setString(event.toString());
    }
    
    @Override
    public void changeStatus(boolean running) {
        this.switchComponents(selection, !running);
        copying = running;
    }
}
