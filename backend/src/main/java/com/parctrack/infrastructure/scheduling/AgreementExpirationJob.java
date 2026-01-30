package com.parctrack.infrastructure.scheduling;

import com.parctrack.domain.customer.Customer;
import com.parctrack.domain.customer.CustomerRepository;
import com.parctrack.domain.equipment.AgreementStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class AgreementExpirationJob {

    private static final Logger logger = LoggerFactory.getLogger(AgreementExpirationJob.class);

    private final CustomerRepository customerRepository;

    public AgreementExpirationJob(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Scheduled(cron = "0 0 1 * * *") // Daily at 1:00 AM
    @Transactional
    public void checkExpiredAgreements() {
        logger.info("Starting agreement expiration check");

        LocalDate today = LocalDate.now();

        // Find customers with COVERED status whose contract has expired
        List<Customer> expiredCustomers = customerRepository
                .findByAgreementStatusAndContractEndDateBefore(AgreementStatus.COVERED, today);

        int transitionedCount = 0;
        for (Customer customer : expiredCustomers) {
            customer.setAgreementStatus(AgreementStatus.PENDING);
            customerRepository.save(customer);
            transitionedCount++;
            logger.info("Auto-transitioned customer {} to PENDING due to expired contract (end date: {})",
                    customer.getId(), customer.getContractEndDate());
        }

        logger.info("Agreement expiration check complete. Transitioned {} customers to PENDING status",
                transitionedCount);
    }
}
