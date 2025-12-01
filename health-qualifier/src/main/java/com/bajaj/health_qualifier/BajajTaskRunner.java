package com.bajaj.health_qualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Component
public class BajajTaskRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> STARTING BAJAJ FINSERV HEALTH TASK <<<");
        String myName = "Gargi Singh";
        String myRegNo = "22BCE10274";
        String myEmail = "gargisingh2022@vitbhopal.ac.in";

        String mySolvedSqlQuery = "SELECT d.DEPARTMENT_NAME, " +
                "AVG(TIMESTAMPDIFF(YEAR, e.DOB, NOW())) AS AVERAGE_AGE, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST " +
                "FROM (SELECT DISTINCT EMP_ID FROM PAYMENTS WHERE AMOUNT > 70000) p_filtered " +
                "JOIN EMPLOYEE e ON p_filtered.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                "ORDER BY d.DEPARTMENT_ID DESC";


        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", myName);
        requestBody.put("regNo", myRegNo);
        requestBody.put("email", myEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("1. Sending POST to generate webhook...");

            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                System.out.println("   Response received: " + responseBody);

                String webhookUrl = (String) responseBody.get("webhook");
                String accessToken = (String) responseBody.get("accessToken");

                if (webhookUrl != null && accessToken != null) {
                    System.out.println("2. Webhook URL obtained: " + webhookUrl);
                    System.out.println("   Submitting SQL solution...");

                    HttpHeaders submitHeaders = new HttpHeaders();
                    submitHeaders.setContentType(MediaType.APPLICATION_JSON);

                    submitHeaders.set("Authorization", accessToken);

                    Map<String, String> submitBody = new HashMap<>();
                    submitBody.put("finalQuery", mySolvedSqlQuery);

                    HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(submitBody, submitHeaders);

                    ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitEntity, String.class);

                    System.out.println(">>> SUBMISSION RESULT <<<");
                    System.out.println("Status Code: " + submitResponse.getStatusCode());
                    System.out.println("Response Body: " + submitResponse.getBody());

                } else {
                    System.err.println("Error: Failed to retrieve webhook or accessToken from response.");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred:");
            e.printStackTrace();
        }
    }
}
