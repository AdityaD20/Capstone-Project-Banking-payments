package com.aurionpro.app.service.impl;

import com.aurionpro.app.dto.TransactionReportDto;
import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.PaymentRequest;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.OrganizationRepository;
import com.aurionpro.app.repository.PaymentRequestRepository;
import com.aurionpro.app.service.ReportService;
import com.aurionpro.app.service.UserService;
import jakarta.persistence.criteria.Predicate; // <-- IMPORT THIS
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList; // <-- IMPORT THIS
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final UserService userService;
    private final OrganizationRepository organizationRepository;

    @Override
    public Page<TransactionReportDto> getTransactionReport(LocalDate from, LocalDate to, PaymentType type, Pageable pageable) {
        // The buildSpecification method now returns a ready-to-use Specification
        Specification<PaymentRequest> spec = buildSpecification(from, to, type);
        Page<PaymentRequest> page = paymentRequestRepository.findAll(spec, pageable);
        
        List<TransactionReportDto> dtos = page.getContent().stream()
                .map(this::mapToTransactionReportDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public List<TransactionReportDto> getTransactionReportForDownload(LocalDate from, LocalDate to, PaymentType type) {
        Specification<PaymentRequest> spec = buildSpecification(from, to, type);
        List<PaymentRequest> list = paymentRequestRepository.findAll(spec);
        return list.stream().map(this::mapToTransactionReportDto).collect(Collectors.toList());
    }
    
    // --- THIS IS THE NEW, SELF-CONTAINED SPECIFICATION BUILDER ---
    private Specification<PaymentRequest> buildSpecification(LocalDate from, LocalDate to, PaymentType type) {
        User orgUser = userService.getCurrentUser();
        Organization organization = organizationRepository.findByUserEmail(orgUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "userEmail", orgUser.getEmail()));

        // This is the root of our dynamic query.
        // It returns a Specification that takes the root, query, and criteriaBuilder as input.
        return (root, query, criteriaBuilder) -> {
            
            // We create a list to hold all our filtering conditions (predicates).
            List<Predicate> predicates = new ArrayList<>();

            // 1. ALWAYS filter by the current user's organization ID for security.
            predicates.add(criteriaBuilder.equal(root.get("organization").get("id"), organization.getId()));

            // 2. Add a date filter if 'from' is provided.
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }

            // 3. Add a date filter if 'to' is provided.
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to.atTime(LocalTime.MAX)));
            }

            // 4. Add a type filter if 'type' is provided.
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            
            // 5. Combine all the predicates into a single 'AND' condition.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TransactionReportDto mapToTransactionReportDto(PaymentRequest pr) {
        TransactionReportDto dto = new TransactionReportDto();
        dto.setTransactionId(pr.getId());
        dto.setType(pr.getType());
        dto.setStatus(pr.getStatus());
        dto.setAmount(pr.getAmount());
        dto.setDescription(pr.getDescription());
        dto.setTransactionDate(pr.getCreatedAt());

        if (pr.getType() == PaymentType.VENDOR && pr.getVendor() != null) {
            dto.setRecipientName(pr.getVendor().getName());
        } else if (pr.getType() == PaymentType.SALARY && pr.getItems() != null && !pr.getItems().isEmpty()) {
            dto.setRecipientName(pr.getItems().size() + " Employees");
        } else {
            dto.setRecipientName("N/A");
        }
        return dto;
    }
}