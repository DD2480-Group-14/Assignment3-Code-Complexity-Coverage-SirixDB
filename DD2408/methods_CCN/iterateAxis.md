# iterateAxis
## Function

### Path
`bundles/sirix-saxon/src/main/java/org/sirix/saxon/wrapper/NodeWrapper.java`

### Method declaration
```java
public AxisIterator iterateAxis(final byte axisNumber, final NodeTest nodeTest)
``` 

## Cyclomatic Complexity

### Lizard
30

### Manual count
8 `if`
20 `case`
1 `catch`

1 `return`
1 `throw`


Total: pi - s + 2 = 29 - 2 + 2 = 29

This might differ since Lizard might not recognize this function as multi-exit with the throw and return.
If we recognize this function as having 1 exit then the total would instead become 30.

pi + 1 = 29 + 1 = 30

## Code Length
In terms of LOC the function is quite long with lizard reporting LOC of 113.

## Purpose
The purpose of the function is to be a iterator factory, depending on what axis is passed in, the corresponding iterator is constructed and returned. Since different axis require different constructions a giant switch case is used which increases the cyclomatic complexity by quite alot.

## Refactoring plan

The high cyclomatic complexity is not justified in this function each case can be brought out into their own helper function.

### Cyclomatic complexity

This brings the complexity down to 

14 `case`
1 `if`
1 `catch`

1 `throw`
1 `return`

Total: 16 - 2 + 2 
Lizard: 17 (see previous reasoning)

Which is a decrease of around 41% 

### Before refactoring

```java
	@Override
	public AxisIterator iterateAxis(final byte axisNumber, final NodeTest nodeTest) {
		AxisIterator returnVal = null;
		try {
			final NodeReadTrx rtx = createRtxAndMove();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NODE TEST: " + nodeTest);
			}

			switch (axisNumber) {
			case Axis.ANCESTOR:
				if (getNodeKind() == Kind.DOCUMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AncestorAxis(rtx)), nodeTest);
				}
				break;
			case Axis.ANCESTOR_OR_SELF:
				if (getNodeKind() == Kind.DOCUMENT.getId()) {
					returnVal = Navigator.filteredSingleton(this, nodeTest);
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AncestorAxis(rtx, IncludeSelf.YES)), nodeTest);
				}
				break;
			case Axis.ATTRIBUTE:
				if (getNodeKind() != Kind.ELEMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AttributeAxis(rtx)), nodeTest);
				}
				break;
			case Axis.CHILD:
				if (rtx.hasFirstChild()) {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new ChildAxis(rtx)), nodeTest);
				} else {
					returnVal = EmptyIterator.getInstance();
				}
				break;
			case Axis.DESCENDANT:
				if (hasChildNodes()) {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new DescendantAxis(rtx)), nodeTest);
				} else {
					returnVal = EmptyIterator.getInstance();
				}
				break;
			case Axis.DESCENDANT_OR_SELF:
				returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
						new DescendantAxis(rtx, IncludeSelf.YES)), nodeTest);
				break;
			case Axis.FOLLOWING:
				returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
						new FollowingAxis(rtx)), nodeTest);
				break;
			case Axis.FOLLOWING_SIBLING:
				switch (mNodeKind) {
				case DOCUMENT:
				case ATTRIBUTE:
				case NAMESPACE:
					returnVal = EmptyIterator.getInstance();
					break;
				default:
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new FollowingSiblingAxis(rtx)), nodeTest);
					break;
				}

			case Axis.NAMESPACE:
				if (getNodeKind() != Kind.ELEMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = NamespaceIterator.makeIterator(this, nodeTest);
				}
				break;
			case Axis.PARENT:
				if (rtx.getParentKey() == Kind.DOCUMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new ParentAxis(rtx)), nodeTest);
				}
			case Axis.PRECEDING:
				returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
						new PrecedingAxis(rtx)), nodeTest);
				break;
			case Axis.PRECEDING_SIBLING:
				switch (mNodeKind) {
				case DOCUMENT:
				case ATTRIBUTE:
				case NAMESPACE:
					returnVal = EmptyIterator.getInstance();
					break;
				default:
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new PrecedingSiblingAxis(rtx)), nodeTest);
					break;
				}

			case Axis.SELF:
				returnVal = Navigator.filteredSingleton(this, nodeTest);
				break;

			case Axis.PRECEDING_OR_ANCESTOR:
				returnVal = new Navigator.AxisFilter(
						new Navigator.PrecedingEnumeration(this, true), nodeTest);
				break;
			default:
				throw new IllegalArgumentException("Unknown axis number " + axisNumber);
			}
		} catch (final SirixException exc) {
			LOGGER.error(exc.toString());
		}
		return returnVal;
	}
```

### After refactoring

```java
	@Override
	public AxisIterator iterateAxis(final byte axisNumber, final NodeTest nodeTest) {
		AxisIterator returnVal = null;
		try {
			final NodeReadTrx rtx = createRtxAndMove();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("NODE TEST: " + nodeTest);
			}

			switch (axisNumber) {
			case Axis.ANCESTOR:
				returnVal = iterateAncestor(rtx, nodeTest);
				break;
			case Axis.ANCESTOR_OR_SELF:
				returnVal = iterateAncestorOrSelf(rtx, nodeTest);
				break;
			case Axis.ATTRIBUTE:
				returnVal = iterateAttribute(rtx, nodeTest);
				break;
			case Axis.CHILD:
				returnVal = iterateChild(rtx, nodeTest);
				break;
			case Axis.DESCENDANT:
				returnVal = iterateDescendant(rtx, nodeTest);
				break;
			case Axis.DESCENDANT_OR_SELF:
				returnVal = iterateDescendantOrSelf(rtx, nodeTest);
				break;
			case Axis.FOLLOWING:
				returnVal = iterateFollowing(rtx, nodeTest);
				break;
			case Axis.FOLLOWING_SIBLING:
				returnVal = iterateFollowingSibling(rtx, nodeTest);
				break;
			case Axis.NAMESPACE:
				returnVal = iterateNameSpace(nodeTest);
				break;
			case Axis.PARENT:
				returnVal = iterateParent(rtx, nodeTest);
				break;
			case Axis.PRECEDING:
				returnVal = iteratePreceding(rtx, nodeTest);
				break;
			case Axis.PRECEDING_SIBLING:
				returnVal = iteratePrecedingSibling(rtx, nodeTest);
				break;
			case Axis.SELF:
				returnVal = iterateSelf(nodeTest);
				break;
			case Axis.PRECEDING_OR_ANCESTOR:
				returnVal = iteratePrecedingOrAncestor(nodeTest);
				break;
			default:
				throw new IllegalArgumentException("Unknown axis number " + axisNumber);
			}
		} catch (final SirixException exc) {
			LOGGER.error(exc.toString());
		}
		return returnVal;
	}

	private AxisIterator iterateAncestor(NodeReadTrx rtx, NodeTest nodeTest) {
		AxisIterator returnVal;
				if (getNodeKind() == Kind.DOCUMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AncestorAxis(rtx)), nodeTest);
				}
		return returnVal;
	}

	private AxisIterator iterateAncestorOrSelf(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
				if (getNodeKind() == Kind.DOCUMENT.getId()) {
					returnVal = Navigator.filteredSingleton(this, nodeTest);
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AncestorAxis(rtx, IncludeSelf.YES)), nodeTest);
				}
		return returnVal;
	}

	private AxisIterator iterateAttribute(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
				if (getNodeKind() != Kind.ELEMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new AttributeAxis(rtx)), nodeTest);
				}
		return returnVal;
	}

	private AxisIterator iterateChild(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
				if (rtx.hasFirstChild()) {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new ChildAxis(rtx)), nodeTest);
				} else {
					returnVal = EmptyIterator.getInstance();
				}
		return returnVal;
	}
		
	private AxisIterator iterateDescendant(NodeReadTrx rtx, NodeTest nodeTest) {
		AxisIterator returnVal;
				if (hasChildNodes()) {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new DescendantAxis(rtx)), nodeTest);
				} else {
					returnVal = EmptyIterator.getInstance();
				}
		return returnVal;
	}

	private AxisIterator iterateDescendantOrSelf(NodeReadTrx rtx,NodeTest nodeTest) { 
		return new Navigator.AxisFilter(new SaxonEnumeration(
						new DescendantAxis(rtx, IncludeSelf.YES)), nodeTest);
	}
		
	private AxisIterator iterateFollowing(NodeReadTrx rtx, NodeTest nodeTest) {
				return new Navigator.AxisFilter(new SaxonEnumeration(
						new FollowingAxis(rtx)), nodeTest);
	}


	private AxisIterator iterateFollowingSibling(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
				switch (mNodeKind) {
				case DOCUMENT:
				case ATTRIBUTE:
				case NAMESPACE:
					returnVal = EmptyIterator.getInstance();
					break;
				default:
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new FollowingSiblingAxis(rtx)), nodeTest);
				}
		return returnVal;
		}

	private AxisIterator iterateNameSpace(NodeTest nodeTest) {
		AxisIterator returnVal;
		if (getNodeKind() != Kind.ELEMENT.getId()) {
			returnVal = EmptyIterator.getInstance();
		} else {
			returnVal = NamespaceIterator.makeIterator(this, nodeTest);
		}
		return returnVal;
	}


	private AxisIterator iterateParent(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
				if (rtx.getParentKey() == Kind.DOCUMENT.getId()) {
					returnVal = EmptyIterator.getInstance();
				} else {
					returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
							new ParentAxis(rtx)), nodeTest);
				}
			return returnVal;
		}

	private AxisIterator iteratePreceding(NodeReadTrx rtx,NodeTest nodeTest) {
		return new Navigator.AxisFilter(new SaxonEnumeration(new PrecedingAxis(rtx)), nodeTest);

	}

	private AxisIterator iteratePrecedingSibling(NodeReadTrx rtx,NodeTest nodeTest) {
		AxisIterator returnVal;
		switch (mNodeKind) {
			case DOCUMENT:
			case ATTRIBUTE:
			case NAMESPACE:
				returnVal = EmptyIterator.getInstance();
				break;
			default:
				returnVal = new Navigator.AxisFilter(new SaxonEnumeration(
						new PrecedingSiblingAxis(rtx)), nodeTest);
			}
		return returnVal;
	}

	private AxisIterator iterateSelf(NodeTest nodeTest) {
		return Navigator.filteredSingleton(this, nodeTest);
	}

	private AxisIterator iteratePrecedingOrAncestor(NodeTest nodeTest) {
		return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, true), nodeTest);
	}
```
