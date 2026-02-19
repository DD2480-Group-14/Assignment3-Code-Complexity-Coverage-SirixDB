package io.sirix.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

public class XMLTokenTest {
    
    @AfterAll
    public static void printCoverageReport() {
        System.out.println("\n=== XMLToken::isNCStartChar Coverage Report ===");
        CoverageRegister.printReport();
    }
    
    @Test
    public void testIsNCStartChar_UppercaseLetters() {
        assertTrue(XMLToken.isNCStartChar('A'));
        assertTrue(XMLToken.isNCStartChar('Z'));
        assertFalse(XMLToken.isNCStartChar('@'));
    }
    
    @Test
    public void testIsNCStartChar_LowercaseLetters() {
        assertTrue(XMLToken.isNCStartChar('a'));
        assertTrue(XMLToken.isNCStartChar('z'));
        assertFalse(XMLToken.isNCStartChar('`'));
    }
    
    @Test
    public void testIsNCStartChar_Underscore() {
        assertTrue(XMLToken.isNCStartChar('_'));
        assertFalse(XMLToken.isNCStartChar('-'));
    }
    
    @Test
    public void testIsNCStartChar_UnicodeRange() {
        assertTrue(XMLToken.isNCStartChar(0xC0));
        assertTrue(XMLToken.isNCStartChar(0x370));
        assertFalse(XMLToken.isNCStartChar(0xD7));
    }
    
    @Test
    public void testIsNCStartChar_InvalidCharacters() {
        assertFalse(XMLToken.isNCStartChar('0'));
        assertFalse(XMLToken.isNCStartChar(' '));
        assertFalse(XMLToken.isNCStartChar('\n'));
    }
}