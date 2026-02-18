package io.sirix.query.coverage;

import io.sirix.query.JsonDBSerializer;
import io.sirix.query.SirixCompileChain;
import io.sirix.query.SirixQueryContext;
import io.sirix.query.coverage.CoverageRegister;
import io.brackit.query.jdm.Sequence;
import io.brackit.query.Query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

public class CoverageTest {

    @Test
    void testFullSerializationPath() {
        
        try (var ctx = SirixQueryContext.create();
             var chain = SirixCompileChain.create()) {
            
            Sequence seq = new Query(chain, "('exampleDB', 2026, true)").evaluate(ctx);
            
            StringBuilder out = new StringBuilder();
            
            try (JsonDBSerializer serializer = new JsonDBSerializer(out, true)) {
                serializer.serialize(seq);
            }
            
            System.out.println("Output: " + out.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void report() {
        CoverageRegister.printReport();
    }
}
