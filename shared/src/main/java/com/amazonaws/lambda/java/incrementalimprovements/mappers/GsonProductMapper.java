package com.amazonaws.lambda.java.incrementalimprovements.mappers;

import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import com.google.gson.Gson;

public class GsonProductMapper implements ProductMapper {
    private final Gson gson;

    public GsonProductMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Product mapFromJson(String body) {
        return gson.fromJson(body, Product.class);
    }

    @Override
    public String mapToJson(Product product) {
        return gson.toJson(product);
    }
}
