package com.aurionpro.app.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchEmailNotificationListener implements ItemWriteListener<Employee> {

	private final EmailService emailService;

	// This ThreadLocal map safely passes the generated passwords from the processor
	// to this listener.
	private final ThreadLocal<Map<String, String>> employeePasswords = ThreadLocal.withInitial(HashMap::new);

	/**
	 * This method is called from the ItemProcessor to temporarily store the
	 * generated password. It's safe because each batch job thread gets its own copy
	 * of the map.
	 * 
	 * @param email    The employee's email (used as a key).
	 * @param password The generated temporary password.
	 */
	public void storePassword(String email, String password) {
		employeePasswords.get().put(email, password);
	}

	/**
	 * This method is executed AFTER a chunk of employees is successfully saved to
	 * the database. It is the correct place to trigger side-effects like sending
	 * emails.
	 */
	@Override
	public void afterWrite(Chunk<? extends Employee> items) {
		log.info("Successfully wrote {} employees. Preparing to send welcome emails.", items.size());
		Map<String, String> passwords = employeePasswords.get();

		for (Employee employee : items) {
			String email = employee.getUser().getEmail();
			String password = passwords.get(email);
			
			log.info("LISTENER: Retrieving password for email [{}]: {}", email, password);

			if (password != null) {
				try {
					// Send the email with the retrieved password
					emailService.sendEmployeeCredentialsEmail(email, password);
					log.info("Successfully sent welcome email to {}", email);
				} catch (Exception e) {
					log.error("Failed to send welcome email to {}. Error: {}", email, e.getMessage());
					// In a real production system, you might add this failed email to a separate
					// retry queue.
				}
			} else {
				log.warn("Could not find temporary password for successfully processed employee email: {}", email);
			}
		}
		// IMPORTANT: Clear the map for the next chunk to avoid memory leaks and
		// incorrect data
		employeePasswords.get().clear();
	}

	/**
	 * This method is required by the interface but not used in this scenario.
	 */
	@Override
	public void beforeWrite(Chunk<? extends Employee> items) {
		// No action needed before the write.
	}

	
	@Override
	public void onWriteError(Exception exception, Chunk<? extends Employee> items) {
		log.error("Error occurred while writing a chunk of {} employees. No emails will be sent for this failed chunk.",
				items.size());
		
		employeePasswords.get().clear();
	}
}