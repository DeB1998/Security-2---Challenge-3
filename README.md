# Challenge 3 - Security 2

De Biasi Alessio - matriculation number 870288

### Explanation

The program solves the role reachabilty problem, where the problem information are included inside a
.arbac file.

To do so, the program first applies pruning, using both forward and backward slicing iteratively
until a fixpoint is reached.

Then, the program starts from the initial _user-to-role_ assignments and computes recursively the
next states by trying to apply all the _can-assign_ and all the _can-revoke_ rules until all the
possible states have been analyzed, or the goal role is reached.

#### Stack overflow problems

If the program crashes due to a StackOverflowError, try to increase the size of the stack by using
the option -Xss=<new size>m (for example, -Xss=64m to set the stack size to 64 MiB)

#### Notes

I have used the _lombok_ library to automatically generate constructors, getters and equals/hashCode
methods.
