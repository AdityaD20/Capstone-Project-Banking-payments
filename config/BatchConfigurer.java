//package com.aurionpro.app.config;
//
//import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class BatchConfigurer extends DefaultBatchConfiguration {
//
//    private final DataSource dataSource;
//    private final PlatformTransactionManager transactionManager;
//
//    // We inject the primary DataSource and PlatformTransactionManager that Spring Boot has already created for your application.
//    public BatchConfigurer(@Qualifier("dataSource") DataSource dataSource,
//                           @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
//        this.dataSource = dataSource;
//        this.transactionManager = transactionManager;
//    }
//
//    // This override explicitly tells Spring Batch to use your application's main DataSource for its tables.
//    @Override
//    protected DataSource getDataSource() {
//        return this.dataSource;
//    }
//
//    // This override explicitly tells Spring Batch to use your application's main Transaction Manager.
//    @Override
//    protected PlatformTransactionManager getTransactionManager() {
//        return this.transactionManager;
//    }
//}