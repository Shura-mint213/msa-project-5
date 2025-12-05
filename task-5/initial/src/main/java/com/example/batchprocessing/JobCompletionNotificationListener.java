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
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting job: {}", jobExecution.getJobInstance().getJobName());
    }

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("=== JOB FINISHED! Проверяем результат в таблице products ===");

			jdbcTemplate.query(
					"SELECT product_id, product_name, product_data FROM products ORDER BY product_id",
					(rs, row) -> String.format("ID=%d | %s | Data=%s",
						rs.getLong("product_id"),
						rs.getString("product_name"),
						rs.getString("product_data")
					)
				)
				.forEach(log::info);
				

            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", 
                Integer.class
            );
            log.info("=== Всего обработано строк: {} ===", count);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job failed: {}", jobExecution.getFailureExceptions());
        }
	}
}
