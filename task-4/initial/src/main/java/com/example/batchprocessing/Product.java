package com.example.batchprocessing;

public record Product(
        Long product_id,
        Long product_sku,
        String product_name,
        Long product_amount,
        String product_data
) {

}
