package com.amazonaws.lambda.java.incrementalimprovements.mappers;

import com.amazonaws.lambda.java.incrementalimprovements.model.Product;

public interface ProductMapper {

    Product mapFromJson(String body);

    String mapToJson(Product product);
}
