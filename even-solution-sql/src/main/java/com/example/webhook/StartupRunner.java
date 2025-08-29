package com.example.webhook;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Component
public class StartupRunner implements ApplicationRunner {

    @Value("${app.name}")
    private String name;

    @Value("${app.regno}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    @Value("${app.generate-url}")
    private String generateUrl;

    @Value("${app.test-url}")
    private String testUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("=== Webhook flow starter ===");
        System.out.println("Using name=" + name + " regNo=" + regNo + " email=" + email);

        try {
            // Step 1: Call generateWebhook
            Map<String, Object> request = Map.of("name", name, "regNo", regNo, "email", email);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            System.out.println("Calling generateWebhook at: " + generateUrl);
            ResponseEntity<Map> resp = restTemplate.postForEntity(generateUrl, entity, Map.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                System.err.println("generateWebhook returned non-2xx: " + resp.getStatusCode());
                return;
            }

            Map<String, Object> respBody = resp.getBody();
            if (respBody == null) {
                System.err.println("Empty response body from generateWebhook");
                return;
            }

            Object webhookObj = respBody.get("webhook");
            Object tokenObj = respBody.get("accessToken");
            if (webhookObj == null || tokenObj == null) {
                System.err.println("Response missing 'webhook' or 'accessToken'. Response: " + respBody);
                return;
            }

            String webhook = webhookObj.toString();
            String accessToken = tokenObj.toString();
            System.out.println("Received webhook: " + webhook);
            System.out.println("Received accessToken: " + (accessToken.length() > 10 ? accessToken.substring(0,10) + "..." : accessToken));

            // Step 2: Decide which SQL to send based on regNo
            String digitsOnly = regNo.replaceAll("\\D+", "");
            int lastTwo = 0;
            if (digitsOnly.length() >= 2) {
                lastTwo = Integer.parseInt(digitsOnly.substring(digitsOnly.length()-2));
            } else if (digitsOnly.length() == 1) {
                lastTwo = Integer.parseInt(digitsOnly);
            }

            boolean isEven = (lastTwo % 2 == 0);
            System.out.println("Last two digits: " + lastTwo + " -> isEven=" + isEven);

            String finalQuery;
            if (isEven) {
                // Question 2
                finalQuery = """
                    SELECT e.emp_id,
                           e.first_name,
                           e.last_name,
                           d.department_name,
                           COALESCE((
                             SELECT COUNT(*)
                             FROM employee e2
                             WHERE e2.department = e.department
                               AND e2.dob > e.dob
                           ), 0) AS younger_employees_count
                    FROM employee e
                    JOIN department d ON e.department = d.department_id
                    ORDER BY e.emp_id DESC;
                    """;
            } else {
                // Question 1
                finalQuery = """
                    SELECT d.department_name,
                           e.first_name,
                           e.last_name,
                           SUM(p.amount) AS total_payment
                    FROM employee e
                    JOIN department d ON e.department = d.department_id
                    JOIN payments p ON e.emp_id = p.emp_id
                    GROUP BY d.department_name, e.emp_id, e.first_name, e.last_name
                    HAVING SUM(p.amount) = (
                        SELECT MAX(total_amt)
                        FROM (
                            SELECT SUM(p2.amount) AS total_amt
                            FROM employee e2
                            JOIN payments p2 ON e2.emp_id = p2.emp_id
                            WHERE e2.department = e.department
                            GROUP BY e2.emp_id
                        ) sub
                    )
                    ORDER BY d.department_name;
                    """;
            }

            // Step 3: Submit finalQuery to returned webhook
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.set("Authorization", accessToken); // FIX: send raw token, no Bearer
            Map<String, Object> body2 = Map.of("finalQuery", finalQuery);
            HttpEntity<Map<String, Object>> entity2 = new HttpEntity<>(body2, headers2);

            System.out.println("Submitting finalQuery to: " + webhook);
            System.out.println("Auth header being sent: " + headers2.getFirst("Authorization"));
            ResponseEntity<String> submitResp = restTemplate.postForEntity(webhook, entity2, String.class);
            System.out.println("Submission response: " + submitResp.getStatusCode() + " body=" + submitResp.getBody());

        } catch (Exception ex) {
            System.err.println("Error during startup flow: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
