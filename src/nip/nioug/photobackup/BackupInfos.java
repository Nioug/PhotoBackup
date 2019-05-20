/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nip.nioug.photobackup;

/**
 *
 * @author Graulich Etienne
 */
public interface BackupInfos 
{
    final public static String CANON_PREFIX = "IMG_";
    final public static String JPG_END = ".JPG";
    final public static String CANON_REGEX = "^img_(.*).(jpe{0,1}g)$";
    
    final public static String TITLE = "Photo Backup";
    
    public static String getRegex(String prefix) {
        String res = "^";
        if (prefix != null)
            res += prefix.toLowerCase();
        res += "(.*).(jpe{0,1}g)$";
        return res;
    }
    
    public static String getRegex(String prefix, String end) {
        String res = "^";
        
        if (prefix != null)
            res += prefix.toLowerCase();
        
        res += "(.*)";
        
        if (end != null && !end.isEmpty()) {
            if (end.charAt(0) != '.')
                res += ".";
            res += end.toLowerCase();
        }
        
        res += "$";
        
        return res;
    }
}
