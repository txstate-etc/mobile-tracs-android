package edu.txstate.mobile.tracs.util;

public class Counter {
    private static int count = 0;

    public static void reset() {
        count = 0;
    }

    public static void increment() {
        count++;
    }

    public static void decrement() {
        count--;
    }

    public static int getCount() {
        return count;
    }
}
