# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## P+
The following people are going for P+:
- Melker Trané (processNode)
- Vidar Nykvist (modify)

## Project

Name:

URL:

One or two sentences describing it

## Onboarding experience

The project could easily be built by cloning the repo and following the instructions in the project's readme. 
The required tools to build the project is Java 25 or later, and gradle 9.1 or later. However, gradle was not
necessary to install since you could use the wrapper, and therefore build using ```./gradlew test```. Other tools,
such as plugins, was installed automatically by the build script.

The build sometimes conclude with one error in the tests, and several tests are ignored or skipped. Additionally,
the build can sometimes fail due to different causes. Otherwise, building the project with ```./gradlew build -x test```
succeeds.

## Complexity

### 1. What are your results for five complex functions?

| Method        | NLOC (Lizard) | CCN (Lizard) | CCN (Manual) |
| :-----------: | :-----------: | :-----------:| :----------: |
| `serialize`   | 62            | 16           | 16           |
| `modify`      | 40            | 22           | 17           |
|`isNCStartChar`| 11            | 24           | 26           |
|`getReturnType`| 60            | 25           | 11           |
| `processNode` | 122           | 48           | 40           |

### 2. Are the functions just complex, or also long?

As seen in the table above, function length does not always correlate with complexity. The function `isNCStartChar` had a high complexity of 24-26 with only having 11 NLOC. Since functions exceeding 50 NLOC are generally considered long, the other five functions are would be categorized as long as well. All of them also achieve high cyclomatic complexity. 

### 3. What is the purpose of the functions?

#### `serialize`
The purpose of the method `serialize` is to act like a central component for transforming a `Sequence` of query results and convert them into JSON output. It is used when query results are to be presented to users or external systems, and to ensure that data types are serialized into valid JSON. The method first checks if the current result is the first and initializes a JSON "rest" array. It further iterates over the items in the Sequence, and each element is serialized according to its type, whether that's arrays, objects, structured database nodes, or atomic values.

#### `modify`
The purpose of the function is to modify a key-value pair in a json object in the db. Depending on what datatype the existing value is and what datatype it is changed to, it branched accordingly.

#### `isNCStartChar`
The purpose of the function is validate whether a character input is a valid XML non colonised name start character according to XML specification. It also checks multiple unicode ranges. As the XML spec requirements check over 20 different specific character ranges and each range check itself adds to the CC, this causes high cyclomatic complexity.

#### `getReturnType`
The function takes two operands as arguments, and returns the corresponding type for the sub operation. If the operands are not compatible, it throws errors. 

#### `processNode`
The purpose of the method is to take a read only JSON node object and insert a copy of that node in a specific location in a writeable JSON object.

### 4. Are exceptions taken into account in the given measurements?


### 5. Is the documentation clear w.r.t. all the possible outcomes?

## Refactoring

Plan for refactoring complex code:

Estimated impact of refactoring (lower CC, but other drawbacks?).

Carried out refactoring (optional, P+):

git diff ...

### `modify`

The high cyclomatic complexity is not justified in this function, for example the branch where we do a full replacement can be lifted out into its own helper function and then the switch case that does a numeric full replacement can be brought out into an additional helper function. 

#### P+ Implementation (Vidar Nykvist)
See branch `vidar-refactor-modify-function` for the implementation. Or follow [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/vidar-refactor-modify-function/bundles/sirix-query/src/main/java/io/sirix/query/json/JsonDBObject.java) url.

The `modify` function now has a CC of 5 instead, the helper functions (`fullReplaceMent`, `fullReplacementNumerical`) have each a CC of 7 respectively.   

### `processNode`
The high complexity is not needed. We can split the function into smaller parts which each take care of a insert location.

#### P+ Implementation (Melker Trané)
For an implimentation of the refactor, see branch refactor-melker. The processNode function now has a CCN of 4 while the 3 helper functions each has a CCN of 14.

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:

git diff ...

What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

1. How detailed is your coverage measurement?

2. What are the limitations of your own tool?

3. Are the results of your tool consistent with existing coverage tools?

## Coverage improvement

Show the comments that describe the requirements for the coverage.

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

Number of test cases added: two per team member (P) or at least four (P+).

| Method        | Current       | Improved     | Extra (P+)   |
| :-----------: | :-----------: | :-----------:| :----------: |
| `serialize`   |               |              |              |
| `modify`      |  4            |  6           | 8            |
|`isNCStartChar`|               |              |              |
|`getReturnType`|               |              |              |
| `processNode` | 10            | 17           | 27           |

### `modify`

The method `modify` is called in the `replace` function which is then called when executing a query including a `replace`, since the previous tests only tried to replace values with `string` and `object` datatypes, the `replace` function does not enter the `null` or `boolean` branch so we create 2 tests with queries that does just this.

See `testReplaceOperationBoolean` and `testReplaceOperationNull` at [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/improved-coverage/bundles/sirix-query/src/test/java/io/sirix/query/JsonMultipleUpdatesTest.java) url

#### Extra tests for P+ (Vidar Nykvist)

When the method replaces a value of a different datatype it has to do a full replacement, this was previously only tested in the case of a String but we can implement tests for Number and Boolean.

See `testReplaceOperationChangeStringToNumber` and `testReplaceOperationChangeStringToBoolean` in branch `vidar-extra-improve-coverage-modify` for the implementation. Or follow [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/vidar-extra-improve-coverage-modify/bundles/sirix-query/src/test/java/io/sirix/query/JsonMultipleUpdatesTest.java) url.

### processNode

The method has two "input" values which determine which branch is visited. The first value is where we should insert. The other is what type we are inserting. Depedning on the pair of these values, a specific branch is visited which calls a specific method (for example insertBooleanAsFirstChild or insertNumberAsLastChild).

To improve the coverage we made two new tests: one which tests all pairs where we insert as left sibling, and another where we tests all pairs where we insert as right sibling.

The assertions checks if the writable and readable databaseses looks the same after the insertions are made.

#### Extra tests for P+ (Melker Trané)
Two extra tests was made. The first tests every pair which inserts as first child, and the second tests every pair which inserts as last child.

When inserting as last child, we assert that we throw exceptions since this is not supported by the 'processNode' function.

See branch extra-coverage-melker

## Self-assessment: Way of working

Current state according to the Essence standard: ...

Was the self-assessment unanimous? Any doubts about certain items?

How have you improved so far?

Where is potential for improvement?

## Overall experience

What are your main take-aways from this project? What did you learn?

Is there something special you want to mention here?
