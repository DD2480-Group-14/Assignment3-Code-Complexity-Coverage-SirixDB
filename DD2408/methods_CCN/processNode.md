# getTreeCellRendererComponent
Path: /bundles/sirix-gui/src/main/java/org/sirix/gui/view/tree/TreeCellRenderer.java

## LOC
According to Lizard the function had 122 lines of code.

## Cyclomatic Complexity

### Lizard
Lizard gives a CCN of 48.

### Manual count
Number of branches:
if: 7
else if: 14
case: 26
catch: 0
while: 0
for: 0
||: 0
&&: 0
total: 47

Number of exits:
return: 1 (implicit)
throw: 8
total: 9

We get a CCN of 2 + 47 - 9 = 40, which is eight less than what Lizard provided. This could be explained if Lizard does not count "throw" as a real exit point.

## Purpose
The purpose of the method is to take a read only JSON node object and insert a copy of that node in a specific location in a writeable JSON object.
