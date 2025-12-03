package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ProductItemProcessor implements ItemProcessor<Product, Product> {

	private static final Logger log = LoggerFactory.getLogger(ProductItemProcessor.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

    @Override
	public Product process(final Product product) {
        Loyality loyality = null;

        try {
            loyality = jdbcTemplate.queryForObject(
                    "SELECT * FROM loyality_data WHERE product_sku = ?",
                    new DataClassRowMapper<>(Loyality.class),
                    product.product_sku()
            );
        } catch (Exception ignored) {

        }


        return new Product(
                product.product_id(),
                product.product_sku(),
                product.product_name(),
                product.product_amount(),
                loyality != null ? loyality.loyalityData() : "NO_LOYALITY"
        );
	}

}
