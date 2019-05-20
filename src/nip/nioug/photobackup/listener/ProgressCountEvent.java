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
    private int current;
    
    public ProgressCountEvent() {
        this.total = 0;
        this.current = 0;
    }

    public ProgressCountEvent(int current, int total) {
        this.total = total;
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public int getCurrent() {
        return current;
    }  
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public void setCurrent(int current) {
        this.current = current;
    }
    
    public void increaseCurrent() {
        current++;
    }
    
    @Override public String toString() {
        return current + "/" + total;
    }
}
