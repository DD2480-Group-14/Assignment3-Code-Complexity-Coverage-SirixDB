package io.sirix.query.coverage;

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
        String result = "";
        for (int i = 0; i < branches; i++) {
            result += i + ": " + register[i] + "\n";
        } 
        return "HitRate: " + getHitCount() + "/" + branches + "\n" + result;
    }

    public static void printReport() {
        System.out.println(getReport()); 
    }

    public static void setBranchCount(int count) {
        branches = count;
    } 
}
