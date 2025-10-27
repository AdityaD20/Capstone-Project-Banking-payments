package com.aurionpro.app.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import com.aurionpro.app.dto.EmployeeBatchDto;
import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaySlip;
import com.aurionpro.app.entity.PaymentRequestItem;
import com.aurionpro.app.entity.enums.EmployeeStatus;
import com.aurionpro.app.entity.enums.RoleType;
import com.aurionpro.app.entity.user.Role;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.listener.BatchEmailNotificationListener;
import com.aurionpro.app.repository.EmployeeRepository;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaySlipRepository;
import com.aurionpro.app.repository.PaymentRequestItemRepository;
import com.aurionpro.app.repository.RoleRepository;
import com.aurionpro.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BatchEmailNotificationListener emailNotificationListener;

    private final PaymentRequestItemRepository paymentRequestItemRepository;
    private final PaySlipRepository paySlipRepository;

    // =====================================================================================
    // == JOB 1: Bulk Employee Import from CSV
    // =====================================================================================

    @Bean
    @StepScope
    public FlatFileItemReader<EmployeeBatchDto> employeeCsvItemReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<EmployeeBatchDto>()
                .name("employeeCsvItemReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1)
                .delimited()
                .names("firstName", "lastName", "email", "dateOfBirth")
                .targetType(EmployeeBatchDto.class)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<EmployeeBatchDto, Employee> csvToEmployeeProcessor(@Value("#{jobParameters['organizationId']}") Long organizationId) {
        final Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("CRITICAL BATCH ERROR: Organization not found for ID: " + organizationId));
        final Role employeeRole = roleRepository.findByName(RoleType.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("CRITICAL BATCH ERROR: ROLE_EMPLOYEE not found."));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return (dto) -> {
            log.info("Processing record: {}", dto);

            // Validation 0: Check for empty/invalid lines. If email is blank, skip the record.
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                log.warn("SKIPPING record because it appears to be an empty or invalid line.");
                return null;
            }

            // Validation 1: Check for duplicate email
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                log.warn("SKIPPING record due to duplicate email: {}", dto.getEmail());
                return null;
            }

            // Validation 2: Robust Date Parsing
            LocalDate dateOfBirth;
            try {
                dateOfBirth = LocalDate.parse(dto.getDateOfBirth(), formatter);
            } catch (DateTimeParseException e) {
                log.error("SKIPPING record for email {} due to invalid date format: '{}'. Expected 'yyyy-MM-dd'.", dto.getEmail(), dto.getDateOfBirth());
                throw e; // Let the skip policy handle this exception
            }

            String temporaryPassword = RandomStringUtils.randomAlphanumeric(10);
            emailNotificationListener.storePassword(dto.getEmail(), temporaryPassword);

            User employeeUser = new User();
            employeeUser.setEmail(dto.getEmail());
            employeeUser.setPassword(passwordEncoder.encode(temporaryPassword));
            employeeUser.setEnabled(true);
            employeeUser.setPasswordChangeRequired(true);
            employeeUser.getRoles().add(employeeRole);

            Employee employee = new Employee();
            employee.setFirstName(dto.getFirstName());
            employee.setLastName(dto.getLastName());
            employee.setDateOfBirth(dateOfBirth);
            employee.setOrganization(organization);
            employee.setUser(employeeUser);
            employee.setStatus(EmployeeStatus.PENDING_DOCUMENTS);

            return employee;
        };
    }

    @Bean
    public ItemWriter<Employee> employeeItemWriter() {
        return employeeRepository::saveAll;
    }

    @Bean
    public Step importEmployeesStep() {
        return new StepBuilder("importEmployeesStep", jobRepository)
                .<EmployeeBatchDto, Employee>chunk(100, transactionManager)
                .reader(employeeCsvItemReader(null))
                .processor(csvToEmployeeProcessor(null))
                .writer(employeeItemWriter())
                .listener(emailNotificationListener)
                .faultTolerant()
                .skip(FlatFileParseException.class) // Skip records with the wrong number of columns (e.g., empty lines)
                .skip(DateTimeParseException.class) // Skip records with bad dates
                .skip(DataIntegrityViolationException.class) // Skip records that violate DB constraints
                .skipLimit(100) // Stop if more than 100 records fail
                .build();
    }

    @Bean
    public Job importEmployeesJob() {
        return new JobBuilder("importEmployeesJob", jobRepository)
                .start(importEmployeesStep())
                .build();
    }

    // =====================================================================================
    // == JOB 2: Salary Disbursement (Payslip Generation)
    // =====================================================================================
    
    @Bean
    @StepScope
    public RepositoryItemReader<PaymentRequestItem> paymentRequestItemReader(
            @Value("#{jobParameters['paymentRequestId']}") Long paymentRequestId) {
        return new RepositoryItemReaderBuilder<PaymentRequestItem>()
                .name("paymentRequestItemReader")
                .repository(paymentRequestItemRepository)
                .methodName("findByPaymentRequestId")
                .arguments(List.of(paymentRequestId))
                .pageSize(100)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<PaymentRequestItem, PaySlip> itemToPaySlipProcessor() {
        return item -> {
            Employee employee = item.getEmployee();
            PaySlip paySlip = new PaySlip();
            paySlip.setEmployee(employee);
            paySlip.setPayPeriod(java.time.YearMonth.now());
            paySlip.setNetSalary(item.getAmountPaid());
            if (employee.getSalaryStructure() != null) {
                paySlip.setBasicSalary(employee.getSalaryStructure().getBasicSalary());
                paySlip.setHra(employee.getSalaryStructure().getHra());
                paySlip.setDearnessAllowance(employee.getSalaryStructure().getDa());
                paySlip.setProvidentFund(employee.getSalaryStructure().getPf());
                paySlip.setOtherAllowances(employee.getSalaryStructure().getOtherAllowances());
            }
            return paySlip;
        };
    }

    @Bean
    public ItemWriter<PaySlip> paySlipWriter() {
        return paySlipRepository::saveAll;
    }

    @Bean
    public Step createPaySlipsStep() {
        return new StepBuilder("createPaySlipsStep", jobRepository)
                .<PaymentRequestItem, PaySlip>chunk(100, transactionManager)
                .reader(paymentRequestItemReader(null))
                .processor(itemToPaySlipProcessor())
                .writer(paySlipWriter())
                .build();
    }

    @Bean
    public Job salaryDisbursementJob() {
        return new JobBuilder("salaryDisbursementJob", jobRepository)
                .start(createPaySlipsStep())
                .build();
    }
}