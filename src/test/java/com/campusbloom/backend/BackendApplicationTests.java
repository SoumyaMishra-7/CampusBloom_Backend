package com.campusbloom.backend;

import com.campusbloom.backend.model.ActionResponse;
import com.campusbloom.backend.model.AuthResponse;
import com.campusbloom.backend.model.CertificateRecord;
import com.campusbloom.backend.model.CaptchaChallengeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        assertThat(response.getBody()).isEqualTo("Backend working!");
    }

    @Test
    void apiTestEndpointReturnsExpectedMessage() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/test", String.class);
        assertThat(response.getBody()).isEqualTo("Backend working!");
    }

    @Test
    void rootEndpointReturnsAppRunningMessage() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);
        assertThat(response.getBody()).isEqualTo("App running");
    }

    @Test
    void apiDashboardEndpointReturnsStudentData() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/dashboard", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Soumya Mishra");
    }

    @Test
    void dashboardEndpointReturnsStudentData() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/student/dashboard", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Soumya Mishra");
        assertThat(response.getBody()).contains("Smart Campus IoT Hackathon Winner");
    }

    @Test
    void uploadedCertificateCanBeFetchedForPreview() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource("%PDF-1.4 preview test".getBytes()) {
            @Override
            public String getFilename() {
                return "preview-test.pdf";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("title", "Preview Test Certificate");
        body.add("category", "Technical");
        body.add("description", "Preview should be available");
        body.add("file", fileResource);

        ResponseEntity<CertificateRecord> uploadResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/student/certificates/upload",
                new HttpEntity<>(body, headers),
                CertificateRecord.class
        );

        assertThat(uploadResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(uploadResponse.getBody()).isNotNull();
        assertThat(uploadResponse.getBody().fileUrl()).isEqualTo("/api/student/certificates/" + uploadResponse.getBody().id() + "/file");

        ResponseEntity<byte[]> fileResponse = restTemplate.getForEntity(
                "http://localhost:" + port + uploadResponse.getBody().fileUrl(),
                byte[].class
        );

        assertThat(fileResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(fileResponse.getHeaders().getContentType()).isNotNull();
        assertThat(new String(fileResponse.getBody())).contains("%PDF-1.4 preview test");
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
                  "email": "aarav@example.com",
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
    void studentCanSignupThroughCompatibilityEndpoint() {
        String signupPayload = """
                {
                  "role": "student",
                  "fullName": "Compatibility Student",
                  "email": "compat.student@example.com",
                  "rollNumber": "COMP101",
                  "department": "Computer Science",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        ResponseEntity<ActionResponse> signupResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/signup",
                jsonRequest(signupPayload),
                ActionResponse.class
        );

        assertThat(signupResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(signupResponse.getBody()).isNotNull();
        assertThat(signupResponse.getBody().message()).isEqualTo("Student account created successfully");
    }

    @Test
    void studentCanLoginThroughCompatibilityEndpoint() {
        String registerPayload = """
                {
                  "fullName": "Compatibility Login Student",
                  "email": "compat.login@example.com",
                  "rollNumber": "COMP102",
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
                  "email": "compat.login@example.com",
                  "captchaId": "%s",
                  "captchaAnswer": "%s",
                  "password": "password123"
                }
                """.formatted(captcha.captchaId(), solveCaptcha(captcha.prompt()));

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/login",
                jsonRequest(loginPayload),
                AuthResponse.class
        );

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().role()).isEqualTo("student");
        assertThat(loginResponse.getBody().redirectTo()).isEqualTo("/student-dashboard");
    }

    @Test
    void adminCanRegister() {
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

        ResponseEntity<ActionResponse> registerResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/admin",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        assertThat(registerResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().message()).isEqualTo("Admin account created successfully");
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
                  "email": "nisha@example.com",
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
        assertThat(loginResponse.getBody().message()).isEqualTo("Invalid email or password");
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
                  "email": "ishita@example.com",
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

    @Test
    void adminCanLoginWithoutAdminId() {
        String registerPayload = """
                {
                  "fullName": "Riya Kapoor",
                  "email": "riya.admin@example.com",
                  "institutionName": "Campus Bloom University",
                  "adminId": "ADMIN777",
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
                  "email": "riya.admin@example.com",
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
        assertThat(loginResponse.getBody().role()).isEqualTo("admin");
        assertThat(loginResponse.getBody().redirectTo()).isEqualTo("/admin-dashboard");
    }

    @Test
    void studentRegistrationRejectsDuplicateRollNumber() {
        String registerPayload = """
                {
                  "fullName": "Aarav Sharma",
                  "email": "aarav.one@example.com",
                  "rollNumber": "CSE999",
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

        String duplicatePayload = """
                {
                  "fullName": "Aarav Sharma Two",
                  "email": "aarav.two@example.com",
                  "rollNumber": "CSE999",
                  "department": "Computer Science",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        ResponseEntity<ActionResponse> duplicateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/register/student",
                HttpMethod.POST,
                jsonRequest(duplicatePayload),
                ActionResponse.class
        );

        assertThat(duplicateResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(duplicateResponse.getBody()).isNotNull();
        assertThat(duplicateResponse.getBody().message()).isEqualTo("An account with this roll number already exists");
    }

    @Test
    void adminRegistrationRejectsDuplicateAdminId() {
        String registerPayload = """
                {
                  "fullName": "Admin One",
                  "email": "admin.one@example.com",
                  "institutionName": "Campus Bloom University",
                  "adminId": "ADMIN900",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/admin",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        String duplicatePayload = """
                {
                  "fullName": "Admin Two",
                  "email": "admin.two@example.com",
                  "institutionName": "Campus Bloom University",
                  "adminId": "ADMIN900",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        ResponseEntity<ActionResponse> duplicateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/auth/register/admin",
                HttpMethod.POST,
                jsonRequest(duplicatePayload),
                ActionResponse.class
        );

        assertThat(duplicateResponse.getStatusCode().is4xxClientError()).isTrue();
        assertThat(duplicateResponse.getBody()).isNotNull();
        assertThat(duplicateResponse.getBody().message()).isEqualTo("An account with this admin ID already exists");
    }

    @Test
    void studentAccountCanBeDeletedFromDatabase() {
        String registerPayload = """
                {
                  "fullName": "Delete Student",
                  "email": "delete.student@example.com",
                  "rollNumber": "DEL100",
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

        String deletePayload = """
                {
                  "role": "student",
                  "email": "delete.student@example.com"
                }
                """;

        ResponseEntity<ActionResponse> deleteResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/delete-account",
                jsonRequest(deletePayload),
                ActionResponse.class
        );

        assertThat(deleteResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(deleteResponse.getBody()).isNotNull();
        assertThat(deleteResponse.getBody().message()).isEqualTo("Student account deleted successfully");
    }

    @Test
    void adminAccountCanBeDeletedFromDatabase() {
        String registerPayload = """
                {
                  "fullName": "Delete Admin",
                  "email": "delete.admin@example.com",
                  "institutionName": "Campus Bloom University",
                  "adminId": "DEL-ADMIN-1",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register/admin",
                jsonRequest(registerPayload),
                ActionResponse.class
        );

        String deletePayload = """
                {
                  "role": "admin",
                  "email": "delete.admin@example.com"
                }
                """;

        ResponseEntity<ActionResponse> deleteResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/delete-account",
                jsonRequest(deletePayload),
                ActionResponse.class
        );

        assertThat(deleteResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(deleteResponse.getBody()).isNotNull();
        assertThat(deleteResponse.getBody().message()).isEqualTo("Admin account deleted successfully");
    }
}
