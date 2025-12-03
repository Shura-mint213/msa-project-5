package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

	private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

	private final JdbcTemplate jdbcTemplate;

	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("=== JOB FINISHED! Проверяем результат в таблице products ===");

			jdbcTemplate.query(
					"SELECT product_id, product_name, price, final_price FROM products ORDER BY product_id",
					(rs, row) -> """
							ID=%d | %s | Цена=%.2f → Финальная цена=%.2f (скидка применена)
							""".formatted(
							rs.getLong("product_id"),
							rs.getString("product_name"),
							rs.getBigDecimal("price"),
							rs.getBigDecimal("final_price")))
					.forEach(log::info);

			log.info("=== Всего обработано строк: {} ===",
					jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class));
		}
	}
}
