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
The purpose of the function is to modify a key-value pair in a json object in the db. Depending on what datatype the existing value is and what datatype it is changed to, it branched accordingly.

## Coverage

Current coverage is 4/17

The replace function is only tested with string, number and an object.


## Refactoring plan

The high cyclomatic complexity is not justified in this function, for example the branch where we do a full replacement can be lifted out into its own helper function and then the switch case that does a numeric full replacement can be brought out into an additional helper function. 

See branch `vidar-refactor-modify-function` for the implementation.

or follow [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/vidar-refactor-modify-function/bundles/sirix-query/src/main/java/io/sirix/query/json/JsonDBObject.java) url.

### Cyclomatic complexity after refactoring

This brings down the complexity to the following:

5 `if`
4 `&&`
6 `return`

Total: pi - s + 2 = 10 - 6 + 2 = 5

#### Helper functions

The helper function `fullReplaceMent` gets a complexity of 7

6 `if`
1 `implicit return`
Total: 6 + 2 -1 = 7 


The helper function `fullReplacementNumeric` gets a complexity of 7

6 `case`
1 `implicit return`
Total: 6 + 2 -1 = 7 
