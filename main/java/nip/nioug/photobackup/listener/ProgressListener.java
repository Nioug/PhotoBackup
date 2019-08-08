/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.photobackup.listener;

import java.util.EventListener;

/**
 *
 * @author Graulich Etienne
 */
public interface ProgressListener extends EventListener {
    void addMessage(ProgressMessageEvent message);
    void editCount(ProgressCountEvent event);
    void changeStatus(boolean running);
}
