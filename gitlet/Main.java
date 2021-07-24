package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        final File CWD = Repository.CWD;
        final File GITLET_DIR = Repository.GITLET_DIR;
        Wrapper wp = new Wrapper();
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init",args,1);
                wp.init();
                break;
            case "add":
                validateNumArgs("add",args,2);
                wp.add(args[1]);
                break;
            case "rm":
                validateNumArgs("rm",args,2);
                wp.rm(args[1]);
                break;
            case "commit":
                if (args.length < 2){
                    throw error("Please enter a commit message.");
                }
                validateNumArgs("commit",args,2);
                wp.commit(args[1]);
                break;
            case "log":
                validateNumArgs("log",args,1);
                wp.log();
                break;
            case "global-log":
                validateNumArgs("global-log",args,1);
                wp.globalLog();
                break;
            case "find":
                validateNumArgs("find",args,2);
                wp.find(args[1]);
                break;
            case "status":
                validateNumArgs("status",args,1);
                wp.status();
                break;
            case "checkout":
                switch (args.length){
                    case 3:
                        if (args[1].equals("--")){
                            wp.checkoutWithoutId(args[2]);
                        }
                        break;
                    case 4:
                        if (args[2].equals("--")){
                            wp.checkoutWithId(args[1],args[3]);
                        }
                        break;
                    case 2:
                        wp.checkoutBranch(args[1]);
                        break;
                }
                break;
            case "branch":
                validateNumArgs("branch",args,2);
                wp.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch",args,2);
                wp.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs("reset",args,2);
                wp.reset(args[1]);
                break;
            case "merge":
                validateNumArgs("merge",args,2);
                wp.merge(args[1]);
                break;
            case "add-remote":
                validateNumArgs("add-remote",args,3);
                wp.addRemote(args[1], args[2]);
            default:
                throw new RuntimeException(
                        String.format("Unknown command: %s", args[0]));
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
