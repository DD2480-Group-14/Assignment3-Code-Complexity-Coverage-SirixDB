# getTreeCellRendererComponent
Path: /bundles/sirix-gui/src/main/java/org/sirix/gui/view/tree/TreeCellRenderer.java

## LOC
According to Lizard the function had 111 lines of code.

## Cyclomatic Complexity

### Lizard
Lizard gives a CCN of 23.

### Manual count
Number of branches:
if: 7
else if: 0
case: 11
catch: 0
while: 0
for: 2
||: 2
&&: 0
total: 22

Number of exits:
return: 1
throw: 1
total: 2

We get a CCN of 2 + 22 - 2 = 22, which is one less than what Lizard provided. This could be explained if Lizard does not count "throw" as a real exit point.

## Purpose
The purpose of the method is to get a component representation of a tree cell. In java, a component is an object that has a graphical representation, which can be displayed.