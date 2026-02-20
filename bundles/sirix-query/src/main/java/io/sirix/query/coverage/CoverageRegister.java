package io.sirix.query.coverage;

import java.io.FileWriter;
import java.io.IOException;

public class CoverageRegister {
    private static int branches = 20;
    private static final boolean[] register = new boolean[20];

    public static void register(int id) {
        register[id] = true;
    }

    public static int getHitCount() {
        int hits = 0;
        for (int i = 0; i < branches; i++) {
            if (register[i]) {
                hits++;
            }
        }
        return hits;
    }

    public static String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("HitRate: ").append(getHitCount()).append("/").append(branches).append("\n");

        for (int i = 0; i < branches; i++) {
            sb.append("Branch ID ").append(i).append(": ").append(register[i]).append("\n");
        } 
        return sb.toString();
    }

    public static void printReport() {
        try (FileWriter  writer = new FileWriter("serialize_coverage.txt")){
            writer.write(getReport());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setBranchCount(int count) {
        branches = count;
    } 
}
