package io.sirix.service.xml.xpath.operators;

import static org.junit.Assert.assertNotEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.sirix.Holder;
import io.sirix.XmlTestHelper;
import io.sirix.exception.SirixException;
import io.sirix.service.xml.xpath.AbstractAxis;
import io.sirix.service.xml.xpath.expr.SequenceAxis;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;


public class AbstractObAxisTest {

    private Holder holder;


    @After
    public void tearDown() throws SirixException {
        holder.close();
        XmlTestHelper.deleteEverything();

    }

    @Test
    public void testReset() throws SirixException {
        // Setup
        XmlTestHelper.deleteEverything();
        XmlTestHelper.createTestDocument();
        holder = Holder.generateRtx();

        AbstractAxis op1 = new SequenceAxis(holder.getXmlNodeReadTrx());
        AbstractAxis op2 = new SequenceAxis(holder.getXmlNodeReadTrx());
        AbstractObAxis axis = new SubOpAxis(holder.getXmlNodeReadTrx(), op1, op2);

        long before = axis.getStartKey();
        axis.reset(-1);
        assertNotEquals(before, axis.getStartKey());
    }
}
