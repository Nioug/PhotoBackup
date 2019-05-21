/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.EventListenerList;
import nip.nioug.photobackup.BackupInfos;
import nip.nioug.photobackup.listener.*;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

/**
 *
 * @author Graulich Etienne
 */
public class FilesHandler extends Thread implements Serializable, BackupInfos
{
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
        fireChangeStatus(true);

        int total = countImages(source, regex);
        
        if (!Files.isDirectory(source))
            this.fireAddMessage(ProgressMessageEvent.ERROR, 
                    "\"" + source.getFileName() + "\" is not a valid directory");
        else if (!Files.isDirectory(destination))
            this.fireAddMessage(ProgressMessageEvent.ERROR, 
                    "\"" + destination.getFileName() + "\" is not a valid directory");
        else if (total > 0) {
            count = new ProgressCountEvent(0, total);
            duplicateFiles(source, regex);        
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, "DONE");
        }
        else {
            this.fireAddMessage(ProgressMessageEvent.SUCCESS, "No image found");
        }
        
        fireChangeStatus(false);
        this.interrupt();
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
    
    
    private int duplicateFiles(Path src, String regex) {
        int current = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            for (Path file: stream) {
                if (file.getFileName().toString().toLowerCase().matches(regex)) {
                    if (duplicate(file)) {
                        ++current;    
                        count.increaseCurrent();
                        fireEditCount(count);
                    }
                }
                else if (Files.isDirectory(file)) {
                    current += duplicateFiles(file, regex);
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            fireAddMessage(ProgressMessageEvent.ERROR, x.getMessage());
        }
        return current;
    }
    
    
    private boolean duplicate(Path file) 
    {
        final ImageMetadata metadata;
        try {
            metadata = Imaging.getMetadata(file.toFile());
        } catch (ImageReadException | IOException ex) {
            fireAddMessage(ProgressMessageEvent.ERROR, "no metadata found for " + file.toString());
            return false;
        }         
        
        if (metadata instanceof JpegImageMetadata) 
        {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            Calendar date = get_TIFF_TAG_DATE_TIME(jpegMetadata);
            
            if (date != null) 
            {
                Path sub = this.createSubDirectory(destination, 
                                   String.valueOf(date.get(Calendar.YEAR)));
                sub = this.createSubDirectory(sub, 
                                   String.format("%02d", date.get(Calendar.MONTH)+1));
                try {
                    copyFile(file, sub);   
                    fireAddMessage(ProgressMessageEvent.SUCCESS, 
                            file.getFileName().toString());
                    return true;
                } catch (FileAlreadyExistsException ex) {
                    fireAddMessage(ProgressMessageEvent.SUCCESS, 
                            file.getFileName().toString() + " already exists");
                    return true;
                } catch (IOException ex) {
                    return false;
                }
            }
            else {
                fireAddMessage(ProgressMessageEvent.ERROR, 
                        "no date found for: " + file.getFileName().toString());
                return false;
            }
        }
        else {
            fireAddMessage(ProgressMessageEvent.ERROR, 
                    "no metadata found for " + file.getFileName().toString());
            return false;
        }
    }
    
    
    private Calendar get_TIFF_TAG_DATE_TIME(final JpegImageMetadata jpegMetadata) 
    {
        final TagInfo tagInfo = TiffTagConstants.TIFF_TAG_DATE_TIME;
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            return null;
        } else {
            Calendar cal;
            String d = field.getValueDescription().substring(1, 11);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd");
            
            try {
                cal = Calendar.getInstance();
                cal.setTime(sdf.parse(d));
            } 
            catch (ParseException ex) {
                cal = null;
            }
            
            return cal;         
        }
    }
    
    public void copyFile(Path src, Path dest) 
            throws IOException, FileAlreadyExistsException
    {
        Files.copy(src, dest.resolve(src.getFileName()));        
    }
    
    
    public int countImages(Path src, String regex) {
        int tot = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            for (Path file: stream) {
                if (file.getFileName().toString().toLowerCase().matches(regex)) {
                    tot++;
                }
                else if (Files.isDirectory(file)) {
                    tot += countImages(file, regex);
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            // fireAddMessage(ProgressMessageEvent.ERROR, "counting in " + x.getMessage());
        }
        
        return tot;
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
