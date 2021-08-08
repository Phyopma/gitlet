package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

public class Wrapper {

    File commits = Repository.commits;
    File refs = Repository.refs;
    File HEAD = Repository.HEAD;
    File CWD = Repository.CWD;
    File blobs = Repository.blobs;
    File GITLET_DIR = Repository.GITLET_DIR;
    final File stages = Repository.stages;




    public void init() throws IOException {
        if(GITLET_DIR.exists()){
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
        Repository.setUp();
        Commit commit = new Commit("initial commit",null);
        submitCommit(commit,"master");

        // setup HEAD to
        setHead("master");

        // setup stage area
        StageArea stage = new StageArea();
        writeObject(stages,stage);
    }

    public void add(String n) throws IOException {
        File now = join(CWD,n);
        if(!now.exists()){
            throw error("File does not exist.");
        }
        StageArea sa = readObject(stages,StageArea.class);

        // File(now) => change to blobs obj => hash
        Blob b = new Blob(readContents(now));
        String hashBlob = b.getHash();

        // HEAD -> commit => deserialized

        Commit current = retrieveCurrentCommit();


        // commit -> Map <- find File(now) . if found, compare two blobs (hash)
        if (current.getTracked().containsKey(n)){
            // not equal(hash) , add to sa.add
            if (!current.getTracked().get(n).equals(hashBlob)){
                createBlob(b);
                sa.stageToAdd(n,hashBlob);
            }
            if (sa.getRemove().contains(n)){
                sa.getRemove().remove(n);
            }
        }


        // not found , add to sa.add
        else{

            createBlob(b);
            sa.stageToAdd(n,hashBlob);
        }


        // save sa to file
        writeObject(stages,sa);
    }


    public void rm(String n){
        StageArea sa = readObject(stages,StageArea.class);
        File now = join(CWD,n);

        Commit current = retrieveCurrentCommit();

        // find now in sa.add,
        if(sa.getAdd().containsKey(n)){
            // if found, remove from sa.add
            sa.getAdd().remove(n);

        }
        // if not , error
        // find now in current commit.map

        else if(current.getTracked().containsKey(n)){
            // if found, add to sa.remove
            sa.getRemove().add(n);
            restrictedDelete(n);
        }
        else if(!current.getTracked().containsKey(n)) {
            sa.getRemove().add(n);
        }
        // if not, error
        else{
            message("No reason to remove the file.");
            System.exit(0);
        }

//        // delete now from CWD
//        if(now.exists()){
//            restrictedDelete(now);
//        }
        writeObject(stages,sa);
    }


    public void commit(String msg ) throws IOException {

        // retrieve stage obj
        StageArea sa = readObject(stages,StageArea.class);

        if (sa.getAdd().isEmpty() && sa.getRemove().isEmpty()){
            throw error("No changes added to the commit.");
        }


        // retrieve current commit obj
        Commit parentCommit = retrieveCurrentCommit();
        String parentHash = parentCommit.getHash();

        // create new commit
        Commit newCommit = new Commit(msg, parentHash);
        newCommit.setTracked(parentCommit.getTracked());

        // add stage->Map -> file to new commit
        stageToCommit(newCommit);

        // submit new commit
        submitCommit(newCommit, currentBranch());
        // clear stage obj
        sa.clearStage();


        // save stage obj
        writeObject(stages,sa);

    }


    public void log(){
        Commit current = retrieveCurrentCommit();
        while (current.getParent()!= null){
            logFormat(current);
            current = retrieveCommit(current.getParent());
        }
        logFormat(current);
    }

    public void globalLog() {
        List<String> tmp = plainFilenamesIn(commits);
        assert tmp != null;
        for (String s : tmp) {
            Commit gg = retrieveCommit(s);
            logFormat(gg);

        }

    }

    public void find(String arg) {
        boolean found = false;
        for (String commitHash :
                Objects.requireNonNull(plainFilenamesIn(commits))) {
            Commit current = retrieveCommit(commitHash);
            if ( current.getMessage().equals(arg)){
//                System.out.println("===");
                System.out.println(commitHash);
//                System.out.println();
                found = true;
            }
        }
        if(!found){
            message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public void status() {

        /* branches*/
        System.out.println("=== Branches ===");
        // HEAD -> * select
        String curBranch = currentBranch();
        // refs/heads/-> prints
        for (String s :
                Objects.requireNonNull(plainFilenamesIn(refs))) {
            if(s.equals(curBranch)){
                System.out.println("*"+s);
                continue;
            }
            System.out.println(s);
        }

        System.out.println();



        /* staged files */
        System.out.println("=== Staged Files ===");
        //retrieve stage obj
        StageArea sa = readObject(stages,StageArea.class);
        // print out add Map
        for (String s :
                sa.getAdd().keySet()) {
            System.out.println(s);
        }
        System.out.println();


        /* removed files */
        System.out.println("=== Removed Files ===");
        // print out remove list
        for (String s :
                sa.getRemove()) {
            System.out.println(s);
        }
        System.out.println();

        Commit current = retrieveCurrentCommit();

        /* modifications not staged for commit */
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> cwd = plainFilenamesIn(CWD);
        Map<String,String> tracked = current.getTracked();
        Map<String, String> addStage = sa.getAdd();
        List<String> removeStage = sa.getRemove();
        assert cwd != null;
        for (String s :
                cwd) {
            File f = join(CWD,s);
            Blob b = new Blob(readContents(f));

            // Tracked in current, change in CWD, but not staged
            if (tracked.containsKey(s)){
                if (!tracked.get(s).equals(b.getHash()) && !addStage.containsKey(s) ){
                    System.out.println(s+ " (modified)");
                }
            }

            // staged for add , but different contents in CWD
            if (addStage.containsKey(s)){
                if (!addStage.get(s).equals(b.getHash())){
                    System.out.println(s+ " (modified)");
                }
            }

        }
        // staged for add , but deleted in CWD
        for (String s :
                addStage.keySet()) {
            if (!cwd.contains(s)){
                System.out.println(s+ " (deleted)");
            }
        }


        // not staged for remove, tracked in current, but deleted in CWD
        for (String s :
                tracked.keySet()) {
            if (!removeStage.contains(s) && !cwd.contains(s)){
                System.out.println(s+ " (deleted)");
            }
        }


        System.out.println();


        /* untracked files */
        System.out.println("=== Untracked Files ===");
        for (String s :
                Objects.requireNonNull(plainFilenamesIn(CWD))) {
            if (!current.getTracked().containsKey(s)){
                if (!sa.getAdd().containsKey(s)){
                    System.out.println(s);
                }
            }
//            if (sa.getRemove().contains(s)){
//                System.out.println(s);
//            }
        }

        System.out.println();

    }

    public void checkoutWithoutId(String file) throws IOException {
        //retrieve commit from HEAD
        Commit current = retrieveCurrentCommit();
        // find file in commit.Map
        if (current.getTracked().containsKey(file)){
            // if found, retrieve blobs mapped to that file
            String blobHash = current.getTracked().get(file);
            createNewVersion(blobHash,file);
        }
        // if not, error
        else{
            throw error("File does not exist in that commit.");
        }
    }

    public void checkoutWithId(String hash, String file) throws IOException {

        // if hash not found, error
        if (!plainFilenamesIn(commits).contains(hash)){
            message("No commit with that id exists.");
            System.exit(0);
        }

        // retrieve commit form given hash
        Commit givenCommit = retrieveCommit(hash);

        // find file in commit.Map
        if (givenCommit.getTracked().containsKey(file)){
            // if found, retrieve blobs
            String blobHash = givenCommit.getTracked().get(file);
            createNewVersion(blobHash,file);
        }
        // if not, error
        else{
            message("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public void checkoutBranch(String branch) throws IOException {
        if (!Objects.requireNonNull(plainFilenamesIn(refs)).contains(branch)){
            message("No such branch exists.");
            System.exit(0);
        }
        String curBranch = currentBranch();
        if ( curBranch.equals(branch)){
            message("No need to checkout the current branch.");
            System.exit(0);

        }
        Commit curCommit = retrieveCurrentCommit();
        setHead(branch);
        Commit target = retrieveCurrentCommit();
        for (String s: Objects.requireNonNull(plainFilenamesIn(CWD))
             ) {
            if (!curCommit.getTracked().containsKey(s)){
                if (target.getTracked().containsKey(s)){
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }


        //retrieve commit from HEAD
        helpCheckout(curCommit,target);


    }


    public void branch(String arg) throws IOException {
        if(Objects.requireNonNull(plainFilenamesIn(refs)).contains(arg)){
            throw error("A branch with that name already exists.");
        }
        File newBranch = join(refs,arg);
        newBranch.createNewFile();
        Commit current = retrieveCurrentCommit();
        current.setSplit(true);
        current.getSplitName().add(currentBranch());
        current.getSplitName().add(arg);
        File f = join(commits,current.getHash());
        writeObject(f,current);
        writeContents(newBranch,current.getHash());
    }

    public void rmBranch(String arg) throws IOException {
        if (!Objects.requireNonNull(plainFilenamesIn(refs)).contains(arg)){
            message("A branch with that name does not exist.");
            System.exit(0);
        }

        String currentBranch = currentBranch();
        if (currentBranch.equals(arg)){
            message("Cannot remove the current branch.");
            System.exit(0);
        }

        File delete = join(refs,arg);
        delete.delete();

    }

    public void reset(String arg) throws IOException {
        if (!Objects.requireNonNull(plainFilenamesIn(commits)).contains(arg)){
            message("No commit with that id exists.");
            System.exit(0);
        }
        Commit curCommit = retrieveCurrentCommit();
        Commit target = retrieveCommit(arg);
        for (String s :
                Objects.requireNonNull(plainFilenamesIn(CWD))) {
            if (!curCommit.getTracked().containsKey(s)){
                if (target.getTracked().containsKey(s)){
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                else {
                    rm(s);
                }

            }
        }

        helpCheckout(curCommit,target);
        File f = join(refs,currentBranch());
        f.createNewFile();
        writeContents(f,target.getHash());

    }

    public void merge(String arg) throws IOException {
        StageArea sa = readObject(stages,StageArea.class);

        if (!sa.getRemove().isEmpty() || !sa.getAdd().isEmpty()){
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!Objects.requireNonNull(plainFilenamesIn(refs)).contains(arg)){
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (arg.equals(currentBranch())){
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit current = retrieveCurrentCommit();
        for (String s :
                Objects.requireNonNull(plainFilenamesIn(CWD))) {
            if (!current.getTracked().containsKey(s)){
                if (!sa.getAdd().containsKey(s)){
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            if (sa.getRemove().contains(s)){
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        String givenHash = readContentsAsString(join(refs,arg));
        Commit given = retrieveCommit(givenHash);
        //here
        Commit split = null;
        Commit givenSplit = retrieveCommit(given.getHash());
        Commit currentSplit = retrieveCommit(current.getHash());


        //here to correct

        Queue<String> fromGiven = new ArrayDeque<>();
        Queue<String> fromCurrent = new ArrayDeque<>();
        Set<String> splitList = new HashSet<>();
        while(Objects.nonNull(givenSplit.getParent())) {
            fromGiven.add(givenSplit.getParent());
            if (Objects.nonNull((givenSplit.getSecondParent()))) {
                fromGiven.add(givenSplit.getSecondParent());
            }
            if (givenSplit.isSplit()) {
                splitList.add(givenSplit.getHash());
            }
            String tmp = fromGiven.remove();
            givenSplit = retrieveCommit(tmp);


        }
        while(Objects.nonNull(currentSplit.getParent())) {
            fromCurrent.add(currentSplit.getParent());
            if (Objects.nonNull((currentSplit.getSecondParent()))) {
                fromCurrent.add(currentSplit.getSecondParent());
            }
            if ( currentSplit.isSplit() && splitList.contains(currentSplit.getHash()) ) {
                split = currentSplit;
                break;
            }
            String tmp = fromCurrent.remove();
            currentSplit = retrieveCommit(tmp);


        }
        if (split == null){
            split = currentSplit;
        }





//        Queue<String> bfs = new LinkedList<>();
//        while (!(split.isSplit()  &&  split.getSplitName().contains(arg) )) {
//            bfs.add(split.getParent());
//            if (Objects.nonNull(split.getSecondParent())){
//                bfs.add(split.getSecondParent());
//            }
//            String tmp = bfs.remove();
//            if (tmp == null){
//                break;
//            }
//            split = retrieveCommit(tmp);

//            if (Objects.isNull(split.getSecondParent())){
//                split = retrieveCommit(split.getParent());
//            }
//            else{
//                split = retrieveCommit(split.getSecondParent());
//            }

//        }



        if (split.getHash().equals(given.getHash())){
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (split.getHash().equals(current.getHash())){
            checkoutBranch(arg);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        Map<String,String> splitTracked = split.getTracked();
        Map<String,String> givenTracked = given.getTracked();
        Map<String,String> currentTracked = current.getTracked();
        Set<String> all = new HashSet<>(splitTracked.keySet());
        all.addAll(givenTracked.keySet());
        all.addAll(currentTracked.keySet());

        for (String s :
                all) {
            if (!Objects.equals(givenTracked.get(s),(currentTracked.get(s)))){
                if(Objects.equals(splitTracked.get(s),(currentTracked.get(s)))){
                    if (Objects.isNull(givenTracked.get(s))){
                        restrictedDelete(join(CWD,s));
                        sa.stageToRemove(s);
                    }
                    else{
                        createNewVersion(givenTracked.get(s),s);
                        sa.stageToAdd(s,givenTracked.get(s));
                    }
                }
                else{
                   if (!Objects.equals(splitTracked.get(s),(givenTracked.get(s))) ){
                        message("Encountered a merge conflict.");
                       byte[] givenContent = "".getBytes(StandardCharsets.UTF_8);
                       byte[] currentContent = "".getBytes(StandardCharsets.UTF_8);

                       if (Objects.nonNull(givenTracked.get(s))){
                            givenContent = readObject(join(blobs,givenTracked.get(s)),Blob.class).getContents();
                        }
                        if (Objects.nonNull(currentTracked.get(s))){
                            currentContent = readObject(join(blobs,currentTracked.get(s)),Blob.class).getContents();
                        }


                        File now = join(CWD,s);
                        now.createNewFile();
                        writeContents(now,"<<<<<<< HEAD\n",currentContent,"=======\n",givenContent,">>>>>>>\n");
                        Blob bb = new Blob(readContents(now));
                        createBlob(bb);
                        sa.stageToAdd(s,bb.getHash());

                   }
                    else if (Objects.isNull(currentTracked.get(s))){
                        restrictedDelete(join(CWD,s));
                        sa.stageToRemove(s);
                    }
                    else{
                        createNewVersion(currentTracked.get(s),s);
                        sa.stageToAdd(s,currentTracked.get(s));
                    }

                }

            }else {
                sa.stageToAdd(s,currentTracked.get(s));
            }
        }
        writeObject(stages,sa);
        String currentBranch = currentBranch();
        Commit newCommit = new Commit("Merged "+arg+" into "+currentBranch()+".",current.getHash());
        setHead(arg);
        Commit targetCommit = retrieveCurrentCommit();
        setHead(currentBranch);
        newCommit.setSecondParent(targetCommit.getHash());
        given.setSplit(true);
        given.getSplitName().add(currentBranch);
        given.getSplitName().add(arg);
        File f = join(commits,given.getHash());
        writeObject(f,given);
        stageToCommit(newCommit);
        submitCommit(newCommit, currentBranch());
        sa.clearStage();
        writeObject(stages,sa);
    }


    public void addRemote(String remote_name, String remote_dir) {


    }





    // helper functions





    private void helpCheckout(Commit current,Commit target) throws IOException {
        if (!current.getHash().equals(target.getHash())){
            for (String s :
                    target.getTracked().keySet()) {
                String blobHash = target.getTracked().get(s);
                createNewVersion(blobHash,s);
            }
            for (String s :
                    current.getTracked().keySet()) {
                if (!target.getTracked().containsKey(s)){
                    File delete = join(CWD,s);
                    restrictedDelete(delete);
                }
            }
        }

    }



    private void createNewVersion(String blobHash, String file) throws IOException {
        File blob = join(blobs,blobHash);
        Blob b = readObject(blob,Blob.class);
        // overwrite or create new file with contents in retrieved blob
        File newVersion = join(CWD,file);
        newVersion.createNewFile();
        writeContents(newVersion, (Object) b.getContents());
    }

    private String currentBranch(){
        File path = new File(readContentsAsString(HEAD));
        return path.getName();
    }

    private void logFormat(Commit current){
        System.out.println("===");
        System.out.println("commit "+ current.getHash());
        if (current.getSecondParent() != null){
            System.out.println("Merge: "+current.getParent().substring(0,7)+" "+current.getSecondParent().substring(0,7));
        }
        System.out.println("Date: "+ current.getDate());
        System.out.println(current.getMessage());
        System.out.println();
    }

    private void stageToCommit(Commit newCommit){
        StageArea sa = readObject(stages,StageArea.class);
        for (String s:sa.getAdd().keySet()
        ) {
            newCommit.getTracked().put(s,sa.getAdd().get(s));
        }
        for (String s:sa.getRemove()
        ) {
            newCommit.getTracked().remove(s);
        }
    }

    private void submitCommit(Commit commit,String branch) throws IOException {
        // write commit to hash-named file in commits folder
        final String hash = sha1(serialize(commit));
        commit.setHash(hash);
        final File current = join(commits,hash);
        current.createNewFile();
        writeObject(current,commit);

        // set refs/heads/branch to current commit
        final File branchName = join(refs,branch);
        branchName.createNewFile();
        writeContents(branchName,hash);

    }

    private void setHead(String branch){
        writeContents(HEAD,join("refs","heads",branch).getPath());
    }

    private Commit retrieveCurrentCommit(){
        String commitPath = readContentsAsString(HEAD);
        File path = join(GITLET_DIR, commitPath);
        String hash = readContentsAsString(path);
        return retrieveCommit(hash);
    }

    private Commit retrieveCommit(String hash){
        File cPath = join(commits,hash);
        return readObject(cPath,Commit.class);
    }


    private void createBlob(Blob b) throws IOException {
        File f = join(blobs,b.getHash());
        f.createNewFile();
        writeObject(f,b);
    }



}

