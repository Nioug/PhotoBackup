/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import javax.swing.event.EventListenerList;
import nip.nioug.photobackup.BackupInfos;
import nip.nioug.photobackup.listener.*;

/**
 *
 * @author Graulich Etienne
 */
public class FilesHandler extends Thread implements Serializable, BackupInfos
{
    private static boolean interrupt = false;
    private final Path destination;
    private final Path source;
    private final String regex;
    private ProgressCountEvent count;
    private final EventListenerList listeners = new EventListenerList();
    
    public FilesHandler(Path source, Path destination, String regex)
    {
        this.destination = Paths.get(destination.toString());
        this.source = Paths.get(source.toString());
        this.regex = regex;
    }
    
    
    @Override
    public void run() {
        interrupt = false;
        fireChangeStatus(true);

        int total = countImages(source, regex);
        
        if (!Files.isDirectory(source))
            this.fireAddMessage(ProgressMessageEvent.ERROR, 
                    "\"" + source.getFileName() + "\" is not a valid directory");
        
        else if (!Files.isDirectory(destination))
            this.fireAddMessage(ProgressMessageEvent.ERROR, 
                    "\"" + destination.getFileName() + "\" is not a valid directory");
        
        else if (total > 0) {
            count = new ProgressCountEvent(total);
            duplicateFiles(source, regex);        
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, "---");            
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, count.getCopied() + " file(s) copied");
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, count.getUncopied() + " file(s) uncopied");
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, count.getErrors() + " error(s) detected");
        }
        else {
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, "No image found");
        }
        
        fireChangeStatus(false);
        this.interrupt();
    }
    
    
    @Override
    public void interrupt() {
        interrupt = true;
    }
    

    public final Path createSubDirectory(Path path, String newDir) 
    {
        try {
            return Files.createDirectory(Paths.get(path + "\\" + newDir));
        } catch (IOException ex) {
            // fireAddMessage(ProgressMessageEvent.ERROR, ex.getMessage());
            return Paths.get(path + "\\" + newDir);
        }
    }
    
    
    private void duplicateFiles(Path src, String regex) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            for (Path file: stream) {
                if (interrupt)
                    break;
                   
                if (Files.isDirectory(file)) {
                    duplicateFiles(file, regex);
                }
                else if (file.getFileName().toString().toLowerCase().matches(regex)) {
                    try {
                        duplicate(file);
                    } catch (AccessDeniedException ex) {
                        fireAddMessage(ProgressMessageEvent.ERROR, "Access denied"); 
                        return;
                    } 
                }
                fireEditCount(count);
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            fireAddMessage(ProgressMessageEvent.ERROR, x.getMessage());
        }
    }
    
    
    public Date getDate(Path file) 
    {        
        Date date = null;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null)
                date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        } catch (ImageProcessingException | IOException ex) {
            fireAddMessage(ProgressMessageEvent.ERROR, 
                    "no metadata found for " + file.getFileName().toString());
        }
        return date;
    }
    
    
    private boolean duplicate(Path file) throws AccessDeniedException
    {
        Date date = getDate(file);
        if (date != null) 
        {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            Path sub = this.createSubDirectory(destination, 
                               String.valueOf(c.get(Calendar.YEAR)));
            sub = this.createSubDirectory(sub, 
                               String.format("%02d", c.get(Calendar.MONTH)+1));
            try {
                copyFile(file, sub);
                count.increaseCopied();
                fireAddMessage(ProgressMessageEvent.SUCCESS, 
                        file.getFileName().toString());
                return true;
            } catch (FileAlreadyExistsException ex) {
                count.increaseUncopied();
                fireAddMessage(ProgressMessageEvent.SUCCESS, 
                        file.getFileName().toString() + " already exists");
                return true;
            } catch (AccessDeniedException ex) {
                throw ex;
            } catch (IOException ex) {
                count.increaseErrors();
                return false;
            }
        }
        else {
            fireAddMessage(ProgressMessageEvent.ERROR, 
                    "no date found for: " + file.getFileName().toString());
            count.increaseErrors();
            return false;
        }       
    }

    
    private void copyFile(Path src, Path dest) 
            throws IOException, FileAlreadyExistsException, AccessDeniedException
    {
        Files.copy(src, dest.resolve(src.getFileName()));        
    }
    
    
    public int countImages(Path src, String regex) {
        int total = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            for (Path file: stream) {
                if (file.getFileName().toString().toLowerCase().matches(regex)) {
                    total++;
                }
                else if (Files.isDirectory(file)) {
                    total += countImages(file, regex);
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            // fireAddMessage(ProgressMessageEvent.ERROR, "counting in " + x.getMessage());
        }
        
        return total;
    }
    
    
    
    public void addProgressListener(ProgressListener pl) {
        listeners.add(ProgressListener.class, pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(ProgressListener.class, pl);
    }
    
    public ProgressListener[] getProgressListeners() {
        return listeners.getListeners(ProgressListener.class);
    }    
    
    
    protected void fireAddMessage(int status, String message) {
        for (ProgressListener pl : getProgressListeners()) {
            pl.addMessage(new ProgressMessageEvent(status, message));
        }
    }
    
    protected void fireEditCount(ProgressCountEvent event) {
        for (ProgressListener pl : getProgressListeners()) {
            pl.editCount(event);
        }
    }
    
    protected void fireChangeStatus(boolean running) {
        for (ProgressListener pl : getProgressListeners()) {
            pl.changeStatus(running);
        }
    }
}
