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
public class ProgressMessageEvent {
    private final int status;
    private final String message;
    
    public static final int ERROR = 0;
    public static final int SUCCESS = 1;

    public ProgressMessageEvent(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }    
}
