/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.photobackup;

import nip.nioug.view.MainFrame;
import java.io.IOException;
import javax.swing.JFrame;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import nip.nioug.model.FilesHandler;


/**
 *
 * @author Graulich Etienne
 */
public class PhotoBackup implements BackupInfos {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
//        String dest = "C:\\Users\\Nioug\\Pictures";
//        
//        Path src = Paths.get("C:\\Users\\Nioug\\Pictures\\test\\IMG_6684.JPG");
//        FilesHandler fh = new FilesHandler(src, src, BackupInfos.CANON_REGEX);
        
        MainFrame fen = new MainFrame();
        fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fen.setVisible(true);	
    }
    
}
