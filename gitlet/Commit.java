package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable ,Dumpable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String parent;

    public String getSecondParent() {
        return secondParent;
    }

    public void setSecondParent(String secondParent) {
        this.secondParent = secondParent;
    }

    private String secondParent;
    private String date;
    private Map<String,String> tracked = new HashMap<>();
    private String hash;
    private boolean split = false;

    public Set<String> getSplitName() {
        return splitName;
    }

    
    private Set<String> splitName = new HashSet<>();

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }





    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        if (parent == null){
            this.date = initial();
        }
        this.date = now();

    }

    public void setTracked(Map<String,String> newTracks){
       tracked = newTracks;
    }

    public String getHash(){
        return hash;
    }

    public void addToMap(String file,String blob){
        tracked.put(file,blob);
    }

    public void removeFromMap(String file){
        tracked.remove(file);
    }

    public Map<String, String> getTracked(){
        return tracked;
    }

    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }

    public String getDate() {
        return date;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    private String now(){
        return new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").format(new Date()) + " +0630";

    }

    private String initial(){
        return new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").format( new Date(70, Calendar.JANUARY, 1, 0, 0, 0)) + " +0630";
    }


    @Override
    public void dump() {
        System.out.println("msg: "+ this.message);
        System.out.println("parent: "+this.parent);
        System.out.println("2nd parent: "+this.secondParent);
        System.out.println("hash: "+this.hash);
        System.out.println("Map Size: "+this.tracked.size());
        System.out.println("isSplit: "+ this.isSplit());


    }



    /* TODO: fill in the rest of this class. */
}
