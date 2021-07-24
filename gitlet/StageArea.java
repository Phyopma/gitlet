package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static gitlet.Utils.*;

public class StageArea implements Serializable, Dumpable {

    final File stages = Repository.stages;

    public Map<String, String> getAdd() {
        return add;
    }

    public List<String> getRemove() {
        return remove;
    }

    private Map<String, String> add = new HashMap<>();
    private List<String> remove = new ArrayList<>();


    public void stageToAdd(String file,String blobs){
        add.put(file,blobs);
    }


    public void stageToRemove(String file){
        remove.add(file);
    }

    public void clearStage(){
        add.clear();
        remove.clear();
    }

    @Override
    public void dump() {
        System.out.println("Add Size: "+add.size());
        System.out.println("Remove Size: "+remove.size());
    }
}
