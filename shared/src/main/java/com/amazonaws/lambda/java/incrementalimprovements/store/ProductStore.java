package com.amazonaws.lambda.java.incrementalimprovements.store;

import com.amazonaws.lambda.java.incrementalimprovements.model.Product;

public interface ProductStore {
    void saveProduct(Product product);
}
