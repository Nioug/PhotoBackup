/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.photobackup;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFrame;
import nip.nioug.model.FilesHandler;
import nip.nioug.view.MainFrame;
import org.apache.commons.imaging.ImageReadException;


/**
 *
 * @author Graulich Etienne
 */
public class PhotoBackup implements BackupInfos {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.commons.imaging.ImageReadException
     */
    public static void main(String[] args) throws IOException, ImageReadException {
//        String dest = "C:\\Users\\Nioug\\Pictures";
//        
//        Path src = Paths.get("C:\\Users\\Nioug\\Pictures\\test");
//        FilesHandler fh = new FilesHandler(src, src, BackupInfos.CANON_REGEX);
//        System.out.println(fh.countImages(Paths.get(dest), FilesHandler.CANON_REGEX));
//        System.out.println(fh.duplicateFiles(FilesHandler.CANON_REGEX));
        
        MainFrame fen = new MainFrame();
        fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fen.setVisible(true);	
    }
    
}
