## Gitlet: Version Control System

### Author: Phyo Pyae Moe Aung

### Description

The repository implements a localized version control system that follows the design documentation of git (https://git-scm.com/book/en/v2).
The system supports basic snapshotting, branching/merging and sharing projects.

### Internal Structures


Real Git distinguishes several different kinds of objects. For the purpose of the simplicity, the important ones are


- **_blobs_**: The saved contents of files. Since Gitlet saves many versions of files, a single file might correspond to multiple blobs: each being tracked in a different commit.
- **_trees_**: Directory structures mapping names to references to blobs and other trees (subdirectories).
- **_commits_**: Combinations of log messages, other metadata (commit date, author, etc.), a reference to a tree, and references to parent commits. The repository also maintains a mapping from branch heads to references to commits, so that certain important commits have symbolic names.

![two_developed_versions.png](readme_resources%2Ftwo_developed_versions.png)
![commits-and-blobs.png](readme_resources%2Fcommits-and-blobs.png)
Gitlet simplifies from Git still further by

- Incorporating trees into commits and not dealing with subdirectories (so there will be one “flat” directory of plain files for each repository).
- Limiting ourselves to merges that reference two parents (in real Git, there can be any number of parents.)
- Having our metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference.

Every object–every blob and every commit in our case–has a unique integer id that serves as a reference to the object. An interesting feature of Git is that these ids are universal: unlike a typical Java implementation, two objects with exactly the same content will have the same id on all systems (i.e. my computer, your computer, and anyone else’s computer will compute this same exact id). In the case of blobs, “same content” means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the same parent reference. The objects in a repository are thus said to be content addressable.

Both Git and Gitlet accomplish this the same way: by using a cryptographic hash function called SHA-1 (Secure Hash 1), which produces a 160-bit integer hash from any sequence of bytes. Cryptographic hash functions have the property that it is extremely difficult to find two different byte streams with the same hash value (or indeed to find any byte stream given just its hash value), so that essentially, we may assume that the probability that any two objects with different contents have the same SHA-1 hash value is 2-160 or about 10-48. Basically, we simply ignore the possibility of a hashing collision, so that the system has, in principle, a fundamental bug that in practice never occurs!

### The Commands

#### init

- **Usage**: `java src.Main init`

- **Description**: Creates a new Gitlet version-control system in the current directory (`.src` folder). This system will automatically start with one commit: a commit that contains no files and has the commit message `initial commit`. It will have a single branch: `master`, which initially points to this initial commit, and `master` will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 (this is called “The (Unix) Epoch”, represented internally by the time 0.)

- **Runtime**: Should be constant relative to any significant measure.

- **Failure cases**: If there is already a Gitlet version-control system in the current directory, it should abort. It should NOT overwrite the existing system with a new one. Should print the error message `A Gitlet version-control system already exists in the current directory.`

#### add

- **Usage**: `java src.Main add [file name]`

- **Description**: Adds a copy of the file as it currently exists to the staging area (see the description of the `commit` command). Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area is in `.src`. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version). The file will no longer be staged for removal (see `src rm`), if it was at the time of the command.

- **Runtime**: In the worst case, this should run in linear time relative to the size of the file being added and $ lgN $, for $N$ number of files in the commit.

- **Failure cases**: If the file does not exist, print the error message `File does not exist.` and exit without changing anything.

- **Differences from real git**: In real git, multiple files may be added at once. In src, only one file may be added at a time.

#### commit

- **Usage**: `java src.Main commit [message]`
- **Description**: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the `rm` command.
- **Runtime**: should be constant with respect to any measure of number of commits. Runtime must be no worse than linear with respect to the total size of files the commit is tracking. Additionally, this command has a memory requirement: Committing must increase the size of the `.src` directory by no more than the total size of the files staged for addition at the time of commit, not including additional metadata. This means Gitlet doesn’t store redundant copies of versions of files that a commit receives from its parent.

- **Failure cases**: If no files have been staged, abort. Print the message `No changes added to the commit.` Every commit must have a non-blank message. If it doesn’t, print the error message `Please enter a commit message.` It is not a failure for tracked files to be missing from the working directory or changed in the working directory (Such as Unix's `rm` command). The command ignores everything outside the `.src` directory entirely.

- **Differences from real git**: In real git, commits may have considerably more metadata.

#### rm

- **Usage**: `java src.Main rm [file name]`

- **Description**: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).

- **Runtime**: Should run in constant time relative to any significant measure.

- **Failure cases**: If the file is neither staged nor tracked by the head commit, print the error message `No reason to remove the file.`

#### log

- **Usage**: `java src.Main log`

- **Description**: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with `git log --first-parent`). This set of commit nodes is called the commit’s history. For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.
- **Runtime**: Should be linear with respect to the number of nodes in head’s history.

#### global-log

- **Usage**: `java src.Main global-log`

- **Description**: Like log, except displays information about all commits ever made. The order of the commits does not matter.
- **Runtime**: Linear with respect to the number of commits ever made.
- **Failure Cases**: None

### find

- **Usage**: `java src.Main find [commit message]`

- **Description**: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks.

- **Runtime**: Should be linear relative to the number of commits.

### status

- **Usage**: `java src.Main status`
- **Description**: Displays what branches currently exist, and marks the current branch with a \*. Also displays what files have been staged for addition or removal.
- **Runtime**: Depends only on the amount of data in the working directory plus the number of files staged to be added or deleted plus the number of branches.

#### checkout

Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you’ll see 3 numbered points. Each corresponds to the respective usage of checkout.

- **Usages**:

  1. `java src.Main checkout -- [file name]`
  2. `java src.Main checkout [commit id] -- [file name]`
  3. `java src.Main checkout [branch name]`

- **Descriptions**:

  1. Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
  2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
  3. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see **Failure cases** below).

- **Runtime**:

  1. Should be linear relative to the size of the file being checked out.
  2. Should be linear with respect to the total size of the files in the commit’s snapshot. Should be constant with respect to any measure involving number of commits. Should be constant with respect to the number of branches.

- **Failure Cases**:
  1. If the file does not exist in the previous commit, abort, printing the error message `File does not exist in that commit.` Do not change the CWD.
  2. If no commit with the given id exists, print `No commit with that id exists.` Otherwise, if the file does not exist in the given commit, print the same message as for failure case 1. Do not change the CWD.
  3. If no branch with that name exists, print `No such branch exists.` If that branch is the current branch, print `No need to checkout the current branch.` If a working file is untracked in the current branch and would be overwritten by the checkout, print `There is an untracked file in the way; delete it, or add and commit it first.` and exit; perform this check before doing anything else. Do not change the CWD.
- **Differences from real git**: Differences from real git: Real git does not clear the staging area and stages the file that is checked out. Also, it won’t do a checkout that would overwrite or undo changes (additions or removals) that you have staged.

#### branch

- **Usage**: `java src.Main branch [branch name]`
- **Description**: Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called “master”.
- **Runtime**: Should be constant relative to any significant measure.
- **Failure cases**: If a branch with the given name already exists, print the error message `A branch with that name already exists.`

#### rm-branch

- **Usage**: `java src.Main rm-branch [branch name]`
- **Description**: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
- **Runtime**: Should be constant relative to any significant measure.
- **Failure cases**: If a branch with the given name does not exist, aborts. Print the error message `A branch with that name does not exist.` If you try to remove the branch you’re currently on, aborts, printing the error message `Cannot remove the current branch.`

#### reset

- **Usage**: `java src.Main reset [commit id]`
- **Description**: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The [commit id] may be abbreviated as for `checkout`. The staging area is cleared. The command is essentially `checkout` of an arbitrary commit that also changes the current branch head.
- **Runtime**: Should be linear with respect to the total size of files tracked by the given commit’s snapshot. Should be constant with respect to any measure involving number of commits.
- **Failure case**: If no commit with the given id exists, print `No commit with that id exists.` If a working file is untracked in the current branch and would be overwritten by the reset, print `There is an untracked file in the way; delete it, or add and commit it first.` and exit; perform this check before doing anything else.
- **Differences from real git**: This command is closest to using the `--hard option`, as in `git reset --hard [commit hash]`.

### merge

- **Usage**: `java src.Main merge [branch name]`
- **Description**: Merges files from the given branch into the current branch. For below, define split point as the last common ancestor commit for the given branch and the current branch.
  1. Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch (checked out from the commit at the front of the given branch). These files should then all be automatically staged.
  2. Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.
  3. Any files that have been modified in both the current and given branch in the same way (i.e., both files now have the same content or were both removed) are left unchanged by the merge. If a file was removed from both the current and given branch, but a file of the same name is present in the working directory, it is left alone and continues to be absent (not tracked nor staged) in the merge.
  4. Any files that were not present at the split point and are present only in the current branch should remain as they are.
  5. Any files that were not present at the split point and are present only in the given branch should be checked out and staged.
  6. Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).
  7. Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.
  8. Any files modified in different ways in the current and given branches are in conflict. “Modified in different ways” can mean that the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches.
- **Runtime**: $O(NlgN+D)$, where $N$ is the total number of ancestor commits for the two branches and $D$ is the total amount of data in all the files under these commits
- **Failure cases**: If there are staged additions or removals present, print the error message `You have uncommitted changes.` and exit. If a branch with the given name does not exist, print the error message `A branch with that name does not exist.` If attempting to merge a branch with itself, print the error message `Cannot merge a branch with itself.` If merge would generate an error because the commit that it does has no changes in it, just let the normal commit error message for this go through. If an untracked file in the current commit would be overwritten or deleted by the merge, print `There is an untracked file in the way; delete it, or add and commit it first.` and exit; perform this check before doing anything else.
- **Differences from real git**: Real Git does a more subtle job of merging files, displaying conflicts only in places where both files have changed since the split point. Real Git has a different way to decide which of multiple possible split points to use. Real Git will force the user to resolve the merge conflicts before committing to complete the merge. Gitlet just commits the merge, conflicts and all, so that you must use a separate commit to resolve problems. Real Git will complain if there are unstaged changes to a file that would be changed by a merge.

## Going Remote

The true power of git is really in its remote features, allowing collaboration with other people over the internet. The point is that both you and your friend could be collaborating on a single code base. If you make changes to the files, you can send them to your friend, and vice versa. And you’ll both have access to a shared history of all the changes either of you have made.

#### add-remote

- **Usage**: `java src.Main add-remote [remote name] [name of remote directory]/.src`
- **Description**: Saves the given login information under the given remote name. Attempts to push or pull from the given remote name will then attempt to use this `.src` directory. By writing, e.g., java src.Main add-remote other ../testing/otherdir/.src you can provide tests of remotes that will work from all locations. Always use forward slashes in these commands. The program will convert all the forward slashes into the path separator character (forward slash on Unix and backslash on Windows).
- **Failure cases**: If a remote with the given name already exists, print the error message: `A remote with that name already exists.` We don’t check if the user name and server information are legit.

#### rm-remote

- **Usage**: `java src.Main rm-remote [remote name]`
- **Description**: Remove information associated with the given remote name. The idea here is that if you ever wanted to change a remote that you added, you would have to first remove it and then re-add it.
- **Failure cases**: If a remote with the given name does not exist, print the error message: `A remote with that name does not exist.`

#### push

- **Usage**: `java src.Main push [remote name] [remote branch name]`
- **Description**: Attempts to append the current branch’s commits to the end of the given branch at the given remote.

  **Details**: This command only works if the remote branch’s head is in the history of the current local head, which means that the local branch contains some commits in the future of the remote branch. In this case, append the future commits to the remote branch. Then, the remote should reset to the front of the appended commits (so its head will be the same as the local head). This is called fast-forwarding. If the Gitlet system on the remote machine exists but does not have the input branch, then simply add the branch to the remote Gitlet.

- **Failure cases**: If the remote branch’s head is not in the history of the current local head, print the error message `Please pull down remote changes before pushing.` If the remote .src directory does not exist, print `Remote directory not found.`

#### fetch

- **Usage**: `java src.Main fetch [remote name] [remote branch name]`
- **Description**: Brings down commits from the remote Gitlet repository into the local Gitlet repository. Basically, this copies all commits and blobs from the given branch in the remote repository (that are not already in the current repository) into a branch named `[remote name]/[remote branch name]` in the local `.src` (just as in real Git), changing `[remote name]/[remote branch name]` to point to the head commit (thus copying the contents of the branch from the remote repository to the current one). This branch is created in the local repository if it did not previously exist.
- **Failure cases**: If the remote Gitlet repository does not have the given branch name, print the error message `That remote does not have that branch.` If the remote `.src` directory does not exist, print `Remote directory not found.`

#### pull

- **Usage**: `java src.Main pull [remote name] [remote branch name]`
- **Description**: Fetches branch `[remote name]/[remote branch name]` as for the fetch command, and then merges that fetch into the current branch.
- **Failure cases**: Just the failure cases of `fetch` and `merge` together.
