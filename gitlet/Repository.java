package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    //
    public static final File commits = join(GITLET_DIR,"objects","commits");
    public static final File blobs = join(GITLET_DIR,"objects","blobs");
    public static final File refs = join(GITLET_DIR,"refs","heads");
    public static final File HEAD = join(GITLET_DIR,"HEAD");
    public static final File stages = join(GITLET_DIR,"stages");
    public static final File remotes = join(GITLET_DIR, "refs","remotes");


    public static void setUp() throws IOException {
        commits.mkdirs();
        blobs.mkdirs();
        refs.mkdirs();
        HEAD.createNewFile();
        stages.createNewFile();
        remotes.mkdirs();
    }

    /* TODO: fill in the rest of this class. */
}
