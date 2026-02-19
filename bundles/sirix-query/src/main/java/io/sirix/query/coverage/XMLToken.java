package io.sirix.utils;

public class XMLToken {
    
    static {
        CoverageRegister.setBranchCount(26);
    }
    
    public static boolean isNCStartChar(final int ch) {
        // Branch 0: ch < 0x80
        if (ch < 0x80) {
            CoverageRegister.register(0);
            
            // Branch 1: ch >= 'A' && ch <= 'Z'
            if (ch >= 'A' && ch <= 'Z') {
                CoverageRegister.register(1);
                return true;
            }
            
            // Branch 2: ch >= 'a' && ch <= 'z'
            if (ch >= 'a' && ch <= 'z') {
                CoverageRegister.register(2);
                return true;
            }
            
            // Branch 3: ch == '_'
            if (ch == '_') {
                CoverageRegister.register(3);
                return true;
            }
            
            // Branch 4: No match in ch < 0x80 range
            CoverageRegister.register(4);
            return false;
        }
        
        // Branch 5: ch >= 0x80 (else of first ternary)
        CoverageRegister.register(5);
        
        // Branch 6: ch < 0x300 (second ternary)
        if (ch < 0x300) {
            CoverageRegister.register(6);
            
            // Branch 7: ch >= 0xC0 && ch != 0xD7 && ch != 0xF7
            if (ch >= 0xC0 && ch != 0xD7 && ch != 0xF7) {
                CoverageRegister.register(7);
                return true;
            }
            
            // Branch 8: No match in 0x80-0x300 range
            CoverageRegister.register(8);
            return false;
        }
        
        // Branch 9: ch >= 0x300 (else of second ternary)
        CoverageRegister.register(9);
        
        // Branch 10: ch >= 0x370 && ch <= 0x37D
        if (ch >= 0x370 && ch <= 0x37D) {
            CoverageRegister.register(10);
            return true;
        }
        
        // Branch 11: ch >= 0x37F && ch <= 0x1FFF
        if (ch >= 0x37F && ch <= 0x1FFF) {
            CoverageRegister.register(11);
            return true;
        }
        
        // Branch 12: ch >= 0x200C && ch <= 0x200D
        if (ch >= 0x200C && ch <= 0x200D) {
            CoverageRegister.register(12);
            return true;
        }
        
        // Branch 13: ch >= 0x2070 && ch <= 0x218F
        if (ch >= 0x2070 && ch <= 0x218F) {
            CoverageRegister.register(13);
            return true;
        }
        
        // Branch 14: ch >= 0x2C00 && ch <= 0x2EFF
        if (ch >= 0x2C00 && ch <= 0x2EFF) {
            CoverageRegister.register(14);
            return true;
        }
        
        // Branch 15: ch >= 0x3001 && ch <= 0xD7FF
        if (ch >= 0x3001 && ch <= 0xD7FF) {
            CoverageRegister.register(15);
            return true;
        }
        
        // Branch 16: ch >= 0xF900 && ch <= 0xFDCF
        if (ch >= 0xF900 && ch <= 0xFDCF) {
            CoverageRegister.register(16);
            return true;
        }
        
        // Branch 17: ch >= 0xFDF0 && ch <= 0xFFFD
        if (ch >= 0xFDF0 && ch <= 0xFFFD) {
            CoverageRegister.register(17);
            return true;
        }
        
        // Branch 18: ch >= 0x10000 && ch <= 0xEFFFF
        if (ch >= 0x10000 && ch <= 0xEFFFF) {
            CoverageRegister.register(18);
            return true;
        }
        
        // Branch 19: No match in any range
        CoverageRegister.register(19);
        return false;
    }
}