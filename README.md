# Challenge 3 — Security 2

De Biasi Alessio - matriculation number 870288

This program is written in Java and uses the build tool Gradle.

### Explanation

The program solves the role reachability problem, where the users, the roles, the user-to-role
assignments, the _can-assign_ rules, the _can-revoke_ rules, and the goal role are stored inside a
`.arbac` file.

To do so, the program first applies pruning, using both _forward_ and _backward slicing_ iteratively
until a fixpoint is reached.

Then, the program starts from the initial _user-to-role_ assignments and computes recursively the
next possible assignments by trying to apply all the _can-assign_ and all the _can-revoke_ rules
until all the possible states have been analyzed, or the goal role is reached.

#### Stack overflow problems

Since the program solves the role reachability problem in a recursive way, there may be some
executions that recurse a lot, leading to a `StackOverflowError`.

In these cases, it is enough to increase the size of the stack by using the option:

```
-Xss=<new stack size>m
```

(for example, -Xss=64m to set the stack size to 64 MiB) when running the program.

#### Notes

I have used the _lombok_ library to automatically generate constructors, getters, and
`equals`/`hashCode` methods. This is why they are not explicitly present, but they are still used.

Moreover, the program does not represent directly _user-to-role_ assignments but condense all the
assignments that mention the same user into a unique object that stores the user and all the roles
assigned to him/her according to the _user-to-role_ assignment. I called such objects
_users-to-role_ assignments.
