/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.sirix.service.xml.xpath.operators;

import io.sirix.api.Axis;
import io.sirix.api.xml.XmlNodeReadOnlyTrx;
import io.sirix.exception.SirixXPathException;
import io.sirix.node.interfaces.Node;
import io.sirix.service.xml.xpath.AtomicValue;
import io.sirix.service.xml.xpath.XPathError;
import io.sirix.service.xml.xpath.types.Type;
import io.sirix.utils.TypedValue;


/**
 * <p>
 * Performs an arithmetic subtraction on two input operators.
 * </p>
 */
public class SubOpAxis extends AbstractObAxis {

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   * @param mOp1 First value of the operation
   * @param mOp2 Second value of the operation
   */
  public SubOpAxis(final XmlNodeReadOnlyTrx rtx, final Axis mOp1, final Axis mOp2) {
    super(rtx, mOp1, mOp2);
  }

  /**
   * {@inheritDoc}
   * 
   */
  @Override
  public Node operate(final AtomicValue mOperand1, final AtomicValue mOperand2)
      throws SirixXPathException {

    final Type returnType = getReturnType(mOperand1.getTypeKey(), mOperand2.getTypeKey());
    final int typeKey = asXmlNodeReadTrx().keyForName(returnType.getStringRepr());

    final byte[] value;

    switch (returnType) {
      case DOUBLE:
      case FLOAT:
      case DECIMAL:
      case INTEGER:
          CoverageTool.cover(0);
        final double dOp1 = Double.parseDouble(new String(mOperand1.getRawValue()));
        final double dOp2 = Double.parseDouble(new String(mOperand2.getRawValue()));
        value = TypedValue.getBytes(dOp1 - dOp2);
        break;
      case DATE:
      case TIME:
      case DATE_TIME:
      case YEAR_MONTH_DURATION:
      case DAY_TIME_DURATION:
          CoverageTool.cover(1);
        throw new IllegalStateException(
            "Add operator is not implemented for the type " + returnType.getStringRepr() + " yet.");
      default:
          CoverageTool.cover(2);
        throw new XPathError(XPathError.ErrorType.XPTY0004);

    }

    CoverageTool.cover(3);
    return new AtomicValue(value, typeKey);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Type getReturnType(final int mOp1, final int mOp2) throws SirixXPathException {

    Type type1;
    Type type2;
    try {
      type1 = Type.getType(mOp1).getPrimitiveBaseType();
      type2 = Type.getType(mOp2).getPrimitiveBaseType();
    } catch (final IllegalStateException e) {
        CoverageTool.cover(4);
      throw new XPathError(XPathError.ErrorType.XPTY0004);
    }

    if (type1.isNumericType() && type2.isNumericType()) {
            CoverageTool.cover(5);

      // if both have the same numeric type, return it
      if (type1 == type2) {
            CoverageTool.cover(6);
        return type1;
      }

      if (type1 == Type.DOUBLE || type2 == Type.DOUBLE) {
            CoverageTool.cover(7);
        return Type.DOUBLE;
      } else if (type1 == Type.FLOAT || type2 == Type.FLOAT) {
            CoverageTool.cover(8);
        return Type.FLOAT;
      } else {
            CoverageTool.cover(9);
        assert (type1 == Type.DECIMAL || type2 == Type.DECIMAL);
        return Type.DECIMAL;
      }

    } else {

      switch (type1) {
        case DATE:
            CoverageTool.cover(10);
          if (type2 == Type.YEAR_MONTH_DURATION || type2 == Type.DAY_TIME_DURATION) {
            CoverageTool.cover(11);
            return type1;
          } else if (type2 == Type.DATE) {
            CoverageTool.cover(12);
            return Type.DAY_TIME_DURATION;
          }
          break;
        case TIME:
            CoverageTool.cover(13);
          if (type2 == Type.DAY_TIME_DURATION) {
            CoverageTool.cover(14);
            return type1;
          } else if (type2 == Type.TIME) {
            CoverageTool.cover(15);
            return Type.DAY_TIME_DURATION;
          }
          break;
        case DATE_TIME:
            CoverageTool.cover(16);
          if (type2 == Type.YEAR_MONTH_DURATION || type2 == Type.DAY_TIME_DURATION) {
            CoverageTool.cover(17);
            return type1;
          } else if (type2 == Type.DATE_TIME) {
            CoverageTool.cover(18);
            return Type.DAY_TIME_DURATION;
          }
          break;
        case YEAR_MONTH_DURATION:
            CoverageTool.cover(19);
          if (type2 == Type.YEAR_MONTH_DURATION) {
            CoverageTool.cover(20);
            return type2;
          }
          break;
        case DAY_TIME_DURATION:
            CoverageTool.cover(21);
          if (type2 == Type.DAY_TIME_DURATION) {
            CoverageTool.cover(22);
            return type2;
          }
          break;
        default:
            CoverageTool.cover(22);
          throw new XPathError(XPathError.ErrorType.XPTY0004);
      }
            CoverageTool.cover(23);
      throw new XPathError(XPathError.ErrorType.XPTY0004);
    }
  }

}
