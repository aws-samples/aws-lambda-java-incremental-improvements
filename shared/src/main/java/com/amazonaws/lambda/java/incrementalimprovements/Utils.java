package com.amazonaws.lambda.java.incrementalimprovements;

public class Utils {

    public static void ignoreException(Runnable action) {
        try {
            action.run();
        } catch (Exception exception) {
            //swallow
        }
    }
}