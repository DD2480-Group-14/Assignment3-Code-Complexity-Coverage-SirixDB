package io.sirix.service.xml.xpath.operators;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;

public class CoverageTool {
    static ArrayList<String> branches = new ArrayList<>();
    static boolean initialized = false;
    static double numCovered = 0;

    /**
     * Initialize the branch array if not yet initialized
     */ 
    static void initializeBranches() {
        for (int i = 0; i < 26; ++i) {
            branches.addLast("ID: " + i + "  false\n");
        }
        initialized = true;
    }

    /**
     * Cover the branch with the given branch ID.
     * The entire branch array is written to the
     * file each time this function is called.
     */
    static void cover(int branchId) {
        if(!CoverageTool.initialized) {
            initializeBranches();
        }

        if(!branches.get(branchId).contains("true")) {
            numCovered += 1;
            branches.set(branchId, "ID: " + branchId + "  true\n");
        }

        try {
            StringBuilder sb = new StringBuilder();
            for(String branch : branches) {
                sb.append(branch);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter("CoverageGetReturnTypeBefore.txt"));
            writer.write(sb.toString());
            StringBuilder totalCoverage = new StringBuilder();
            totalCoverage.append("Total coverage: ").append(numCovered / 26).append("\n");
            writer.append(totalCoverage.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
