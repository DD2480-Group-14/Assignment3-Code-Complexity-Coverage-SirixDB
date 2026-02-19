# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

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
| `iterateAxis` | 113           | 30           | 29           |
|`isNCStartChar`| 11            | 24           | 26           |
| `checkNodes`  | 52            | 18           | 17           |
| `processNode` |               |              |              |
| `modify`      | 49            | 22           | 17           |

### 2. Are the functions just complex, or also long?

As seen in the table above, function length does not always correlate with complexity. The function `isNCStartChar` had a high complexity of 24-26 with only having 11 NLOC. Since functions exceeding 50 NLOC are generally considered long, the other five functions are would be categorized as long as well. All of them also achieve high cyclomatic complexity. 

### 3. What is the purpose of the functions?

#### `serialize`
The purpose of the method `serialize` is to act like a central component for transforming a `Sequence` of query results and convert them into JSON output. It is used when query results are to be presented to users or external systems, and to ensure that data types are serialized into valid JSON. The method first checks if the current result is the first and initializes a JSON "rest" array. It further iterates over the items in the Sequence, and each element is serialized according to its type, whether that's arrays, objects, structured database nodes, or atomic values.

#### `iterateAxis`
The purpose of the function is to be a iterator factory, depending on what axis is passed in, the corresponding iterator is constructed and returned. Since different axis require different constructions a giant switch case is used which increases the cyclomatic complexity by quite alot.

#### `isNCStartChar`
The purpose of the function is validate whether a character input is a valid XML non colonised name start character according to XML specification. It also checks multiple unicode ranges. As the XML spec requirements check over 20 different specific character ranges and each range check itself adds to the CC, this causes high cyclomatic complexity.

#### `checkNodes`
The method checks two Xml tree nodes for different node types. For example, if the node is an `Element`, it checks for several things, including matching names, attribute and namespace keys. The parameters are two read-only transactions of one old and one new revision.

#### `processNode`

#### `modify`
The purpose of the method is to modify a key-value pair in a json object in the db. Depending on what datatype the key-value pair is it is branched accordingly.

### 4. Are exceptions taken into account in the given measurements?


### 5. Is the documentation clear w.r.t. all the possible outcomes?

## Refactoring

### Refactoring plan

#### `serialize`
The high complexity is not necessary. The function could be split up into to multiple smaller functions. For example we could have one function for each data type `Atomic`, `StructuredDBItem`, `Array`, `Object` that serializes specifically that object. 

#### `iterateAxis`
The high cyclomatic complexity is not justified in this function each case can be brought out into their own helper function.

This brings the complexity down to 
Manual: 16 
Lizard: 17 
Which is a decrease of around 41% 

#### `isNCStartChar`
In this the case, the high complexity of the function we would argue is justified. The code is not complex because of its cyclomatic complexity instead it just has a "complex" logical check that brings high cyclomatic complexity. I guess you could add paranthesis do make it more readable but it would still not affect the cyclomatic complexity. 

#### `checkNodes`
The high cyclomatic compleixty is not justified here. We can reduce it by breaking out each case in the switch to a corresponding helper function. This would bring the cyclomatic complexity of the function down and even though the `Element` case has higher cyclomatic complexity than the other cases its cyclomatic complexity would still be in the "simple" range according to the ranges found at https://en.wikipedia.org/wiki/Cyclomatic_complexity.

#### `processNode`

#### `modify`
The high cyclomatic complexity is not justified in this function the switch case can be lifted out into its own helper function. We could also lift out the entire part that runs `replaceObjectRecordValue` into its own helper function.

This would bring down the cyclomatic complexity to the following:
Lizard: 10
Manual: 5

Regardless of which formula we use we can see that the complexity decreases either by: 45% if using lizard formula, 70% if using the other. The result is however the the helper function `fullReplacement` gets a high complexity with lizard reporting 13 but this is also a decrease of 40% compared to the previous 22. 

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

## Self-assessment: Way of working

Current state according to the Essence standard: ...

Was the self-assessment unanimous? Any doubts about certain items?

How have you improved so far?

Where is potential for improvement?

## Overall experience

What are your main take-aways from this project? What did you learn?

Is there something special you want to mention here?
