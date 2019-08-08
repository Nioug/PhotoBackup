/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.photobackup.listener;

/**
 *
 * @author Graulich Etienne
 */
public class ProgressCountEvent {
    private int total;
    private int copied;
    private int uncopied;
    private int errors;
    
    public ProgressCountEvent() {
        this.total = 0;
    }
    
    public ProgressCountEvent(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public int getCurrent() {
        return errors + uncopied + copied;
    }  

    public int getErrors() {
        return errors;
    }

    public int getUncopied() {
        return uncopied;
    } 
    
    public int getCopied() {
        return copied;
    } 
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public void increaseErrors() {
        errors++;
    }
    
    public void increaseUncopied() {
        uncopied++;
    }
    
    public void increaseCopied() {
        copied++;
    }
    
    @Override 
    public String toString() {
        return getCurrent() + "/" + total;
    }
    
}
