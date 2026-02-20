# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## P+
The following people are going for P+:
- Melker Trané (processNode)
- Edwin Nordås Jogensjö (getReturnType)
- Vidar Nykvist (modify)

## Project

Name: SirixDB

URL: [Sirix github repo](https://github.com/sirixdb/sirix)

Purpose: The sirix database has the main goal of handling the history of a database in a neat way.

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

Lizard did not seem to take multiple exit points or exceptions (throws) into account when measuring the CCN. When adjusting for this, we seem to get the same resulsts as Lizard.

## Refactoring

TODO: Need to add for all functions

### `modify`

The high cyclomatic complexity is not justified in this function, for example the branch where we do a full replacement can be lifted out into its own helper function and then the switch case that does a numeric full replacement can be brought out into an additional helper function. 

#### P+ Implementation (Vidar Nykvist)
See branch `vidar-refactor-modify-function` for the implementation. Or follow [this](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/vidar-refactor-modify-function/bundles/sirix-query/src/main/java/io/sirix/query/json/JsonDBObject.java) url.

The `modify` function now has a CC of 5 instead, the helper functions (`fullReplaceMent`, `fullReplacementNumerical`) have each a CC of 7 respectively.   

### `processNode`
The high complexity is not needed. We can split the function into smaller parts which each take care of a insert location.

#### P+ Implementation (Melker Trané)
For an implimentation of the refactor, see branch refactor-melker and file bundles/sirix-core/src/main/java/io/sirix/service/json/shredder/JsonResourceCopy.java

Please see [Commit](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/commit/d0d1f563291476472e126a3b5b856b987bbe1192#diff-7f72cbacc86cd63a6f669fbbb93e8e93bd3b89dd35cd47e4ca0f1740ecced9e1) and [File](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/refactor-melker/bundles/sirix-core/src/main/java/io/sirix/service/json/shredder/JsonResourceCopy.java)

The processNode function now has a CCN of 4 while the 3 helper functions each has a CCN of 14.

### `getReturnType`
Similar to above, the high complexity is not needed. The function can be split into two additional functions. One that takes care of the numerical types, and one that takes care of the other types.

#### P+ Implementation (Edwin Nordås Jogensjö)
See the branch p-plus-edwin in `bundles/sirix-core/src/main/java/io/sirix/service/xml/xpath/operators/SubOpAxis.java`. The refactored `getReturnType` now has a CCN of 2, while the one for numerical types have CCN = 4 and the other have CCN = 7.

## Coverage

For our ad hoc coverage tool we have a CoverageRegister singleton. This singleton has fixed length array of boolean values where the value at a certain position correspond to whether a certain branch has been covered or not. In every branch we add a line which "registers" that branch as visited (eg. 'CoverageRegister.register(3)' for the forth branch). When all tests are executed we call a 'getReport' method on the singleton which returns a string with contains information on what branches were covered and what branches were not.

We also created another coverage tool, CoverageTool, which works similar to the above. However, the CoverageRegister output was not always visible in the standard output, and therefore we have this additional tool that writes the coverage to a file each time it is updated. 

## Coverage improvement

The branches that contain the improvements is called "improved-coverage"
 
Below is a table that shows the branch coverage for the different methods before and after the tests were added. 

| Method        | Current       | Improved     | Extra (P+)   |
| :-----------: | :-----------: | :-----------:| :----------: |
| `serialize`   |               |              |              |
| `modify`      |  4            |  6           | 8            |
|`isNCStartChar`|  2            |  4           |              |
|`getReturnType`| 20            | 22           | 24           |
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

See branch extra-coverage-melker and the file bundles/sirix-core/src/test/java/io/sirix/service/json/shredder/JsonResourceCopyTest.java

Please see [File](https://github.com/DD2480-Group-14/Assignment3-Code-Complexity-Coverage-SirixDB/blob/extra-coverage-melker/bundles/sirix-core/src/test/java/io/sirix/service/json/shredder/JsonResourceCopyTest.java)

### getReturnType

The method can compare either numerical values or other values. If both input types are of numerical values, the method returns `DOUBLE`, `FLOAT` or `DECIMAL` as the type depending on the primitive base type of the values. The coverage of this was improved by creating a test that asserts that the method returns double if the first operand is a double, and the other is another numerical type. We also added a test that assures that the method returns a `TIME` type if the parameters are of type `TIME` and `DAY_TIME_DURATION`.

#### Extra tests P+ (Edwin Nordås Jogensjö)
One extra test could be made to improve coverage for when the first input parameter is of type `DECIMAL`, and the other type is `FLOAT`. This covers another branch in the function, and asserts that the return is of type `FLOAT`. 

Additionally, one other test was made to assert that the program throws an error. However, this test needed a slight modification of the code since the way it was written before made it impossible to reach this branch. When trying to convert the two input parameters to their primitive base types, the function expects an `IllegalStateException` to be thrown if one of the parameters does not have a primitive base type. However, this exception will not be thrown anywhere by the program, but instead a `SiriXPathException`. Therefore, I modified the try-catch statement in `getReturnType` to expect this exception, and added a test for when this should be thrown.

See the branch `p-plus-edwin` in file `bundles/sirix-core/src/test/java/io/sirix/service/xml/xpath/operators/SubOpAxisTest.java` for implementation.

## Self-assessment: Way of working

As in the first assignment, we still consider ourselves to be in the "in use" state. This is mainly because this assignment was quite differnt from the first two, which required us to form new practices and foundations. And because of the relative short time period between this and the last assignment, we have not spent that much time to fully flesh out all practices for this assignment. While we have gotten to know each other better, our commucation has improved. For the next assignment, which seems to more like this one compared to the first two, we hope to have a good plan going into the assignment, so we do not have to spend as much time to change/redo stuff when different approaches are taken by different team members.
