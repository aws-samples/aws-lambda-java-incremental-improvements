package com.amazonaws.lambda.java.incrementalimprovements.handlers;

import com.amazonaws.lambda.java.incrementalimprovements.store.BasicProductStore;
import com.amazonaws.lambda.java.incrementalimprovements.store.MultithreadedProductStore;

public class MultithreadingHandler extends SdkInitCallHandler {

    public MultithreadingHandler() {
        super();
        productStore = new MultithreadedProductStore((BasicProductStore) this.productStore);
    }
}
