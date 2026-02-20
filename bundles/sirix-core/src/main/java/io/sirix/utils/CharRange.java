package io.sirix.utils;

public class CharRange {
    private final int start;
    private final int end;

    public CharRange (int start, int end){
        this.start = start;
        this.end = end;
    }

    public boolean contains (int ch){
        return ch >= start && ch <= end;
    }

    @Override
    public String toString(){
        return String.format("0x%X-0x%X", start, end);
    }
}