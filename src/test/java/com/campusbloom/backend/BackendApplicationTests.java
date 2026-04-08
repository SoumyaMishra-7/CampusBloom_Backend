package com.campusbloom.backend;

import com.campusbloom.backend.model.ActionResponse;
import com.campusbloom.backend.model.AuthResponse;
import com.campusbloom.backend.model.CaptchaChallengeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class BackendApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private HttpEntity<String> jsonRequest(String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private CaptchaChallengeResponse fetchCaptcha() {
        ResponseEntity<CaptchaChallengeResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/auth/captcha",
                CaptchaChallengeResponse.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String solveCaptcha(String prompt) {
        String expression = prompt.replace("What is", "").replace("?", "").trim();
        String[] parts = expression.split(" ");
        int left = Integer.parseInt(parts[0]);
        String operator = parts[1];
        int right = Integer.parseInt(parts[2]);
        return switch (operator) {
            case "+" -> Integer.toString(left + right);
            case "-" -> Integer.toString(left - right);
            default -> throw new IllegalStateException("Unexpected captcha prompt: " + prompt);
        };
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testEndpointReturnsExpectedMessage() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/test", String.class);
        assertThat(response.getBody()).isEqualTo("Backend working");
    }

    @Test
    void dashboardEndpointReturnsStudentData() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/student/dashboard", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Soumya Mishra");
        assertThat(response.getBody()).contains("Smart Campus IoT Hackathon Winner");
    }

    @Test
    void studentCanRegisterAndLogin() {
        String registerPayload = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav@example.com",
                  "rollNumber": "CSE101",
                  "department": "Computer Science",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        ResponseEntity<ActionResponse> registerResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/student",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        assertThat(registerResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().message()).isEqualTo("Student account created successfully");

        CaptchaChallengeResponse captcha = fetchCaptcha();
        String loginPayload = """
                {
                  "role": "student",
                  "identifier": "CSE101",
                  "captchaId": "%s",
                  "captchaAnswer": "%s",
                  "password": "password123"
                }
                """.formatted(captcha.captchaId(), solveCaptcha(captcha.prompt()));

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login",
                jsonRequest(loginPayload),
                AuthResponse.class
        );

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().role()).isEqualTo("student");
        assertThat(loginResponse.getBody().redirectTo()).isEqualTo("/student-dashboard");
    }

    @Test
    void loginFailsWithWrongPassword() {
        String registerPayload = """
                {
                  "fullName": "Nisha Rao",
                  "email": "nisha@example.com",
                  "institutionName": "Campus Bloom University",
                  "adminId": "ADMIN001",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/admin",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        CaptchaChallengeResponse captcha = fetchCaptcha();
        String loginPayload = """
                {
                  "role": "admin",
                  "identifier": "ADMIN001",
                  "captchaId": "%s",
                  "captchaAnswer": "%s",
                  "password": "wrong-password"
                }
                """.formatted(captcha.captchaId(), solveCaptcha(captcha.prompt()));

        ResponseEntity<ActionResponse> loginResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/login",
                HttpMethod.POST,
                jsonRequest(loginPayload),
                ActionResponse.class
        );

        assertThat(loginResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().message()).isEqualTo("Invalid email/identifier or password");
    }

    @Test
    void loginFailsWithIncorrectCaptcha() {
        String registerPayload = """
                {
                  "fullName": "Ishita Sen",
                  "email": "ishita@example.com",
                  "rollNumber": "CSE102",
                  "department": "Computer Science",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/student",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        CaptchaChallengeResponse captcha = fetchCaptcha();
        String loginPayload = """
                {
                  "role": "student",
                  "identifier": "CSE102",
                  "captchaId": "%s",
                  "captchaAnswer": "9999",
                  "password": "password123"
                }
                """.formatted(captcha.captchaId());

        ResponseEntity<ActionResponse> loginResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/login",
                HttpMethod.POST,
                jsonRequest(loginPayload),
                ActionResponse.class
        );

        assertThat(loginResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().message()).isEqualTo("Incorrect captcha answer");
    }
}
