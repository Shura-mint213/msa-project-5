package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job importProductJob;

    @NewSpan("start-batch-job")
    @PostMapping("/run")
    public String runJob(@SpanTag("request-source") 
                         @RequestParam(defaultValue = "manual") String source) throws Exception {

        log.info("Запрос на запуск ETL задачи. source={}", source);

        JobParameters params = new JobParametersBuilder()
                .addLong("startTime", System.currentTimeMillis())
                .addString("source", source)
                .toJobParameters();

        jobLauncher.run(importProductJob, params);

        log.info("Задача отправлена в выполнение");

        return "Job started";
    }
}
