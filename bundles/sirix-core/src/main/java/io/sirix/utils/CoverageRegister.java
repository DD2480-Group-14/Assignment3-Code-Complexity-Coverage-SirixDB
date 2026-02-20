package io.sirix.utils;

public class CoverageRegister {
    private static int branches;
    private static final boolean[] register = new boolean[1024]; // More than 1024 branches will not be covered

    /**
     * Registers a specific branch as covered
     */
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

    /**
     * Prints the "report" to the standard output
     */
    public static void printReport() {
        System.out.println(getReport()); 
    }

    /**
     * Use this to set the total number of branches
     */
    public static void setBranchCount(int count) {
        branches = count;
    } 


}
