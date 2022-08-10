package com.amazonaws.lambda.java.incrementalimprovements.store;

import com.amazonaws.lambda.java.incrementalimprovements.model.Product;

import java.util.List;
import java.util.concurrent.*;

public class MultithreadedProductStore extends BasicProductStore {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final BasicProductStore productStore;

    public MultithreadedProductStore(BasicProductStore productStore) {
        super(null, null, null);
        this.productStore = productStore;
    }

    @Override
    public void saveProduct(Product product) {
        List<Callable<Void>> tasks = List.of(() -> {
            productStore.saveProductInDynamo(product);
            return null;
        }, () -> {
            productStore.saveProductInS3(product);
            return null;
        });

        try {
            for (Future<Void> task : executor.invokeAll(tasks)) {
                task.get(); //exceptions are ignored unless we "get()" here
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
