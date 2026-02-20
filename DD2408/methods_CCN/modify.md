# modify 
## Function

### Path
`bundles/sirix-query/src/main/java/io/sirix/query/json/JsonDBObject.java`

### Method declaration
```java
private void modify(QNm field, Sequence value) 
``` 

## Cyclomatic Complexity

### Lizard
22

### Manual count
11 `if`
6 `case`
4 `&&`

5 `return`
1 undeclared `return`

Total: pi - s + 2 = 21 - 6 + 2 = 17

Lizard seems to use another formula that only counts decision points + 1 (it does not seem to recognize this as multi-exit) in that case we would count 11+6+4+1=22 which is the same as lizard.

## Code Length
In terms of LOC the function is long with lizard reporting LOC of 49, a function should ideally fit inside one screen view which is not the case here (it depends on the font size and viewport ofc).

## Purpose
The purpose of the function is to modify a key-value pair in a json object in the db. Depending on what datatype the key-value pair is it is branched accordingly.

## Refactoring plan

The high cyclomatic complexity is not justified in this function, for example the switch case can that does a full replacement can be lifted out into its own helper function.

See branch `vidar-refactor-modify-function` for the implementation.

or follow [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/vidar-refactor-modify-function/bundles/sirix-query/src/main/java/io/sirix/query/json/JsonDBObject.java) url.

### Cyclomatic complexity

This brings down the complexity to the following:

5 `if`
4 `&&`
6 `return`

Total: pi - s + 2 = 10 - 6 + 2 = 5
Lizard now reports CCN as 10 which we also can count to if we use the same formula as previously mentioned 5 + 4 + 1 = 10.

Regardless of which formula we use we can see that the complexity decreases either by:
45% if using lizard formula
70% if using the other

The result is however the the helper function `fullReplacement` gets a high complexity with lizard reporting 13 but this is also a decrease of 40% compared to the previous 22. 
