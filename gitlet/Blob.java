package gitlet;
import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Blob implements Serializable ,Dumpable{

    final File blobs = Repository.blobs;
    private byte[] contents;
    private String hash;

    public byte[] getContents() {
        return contents;
    }

    public  Blob (byte[] contents){
        this.contents = contents;
        this.hash = sha1(serialize(this));
    }

    public String getHash(){
        return hash;
    }


    @Override
    public void dump() {
        System.out.println("hash: "+ this.hash);
    }
}
