package com.example.project_pulse_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:projectpulse;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.bootstrap.demo-users.enabled=false",
        "app.bootstrap.demo-users.password=test-only-password",
        "app.cloud.name=test-cloud",
        "app.cloud.key=test-key",
        "app.cloud.secret=test-secret",
        "app.email.url=http://localhost",
        "app.email.api-key=test-email-key",
        "app.email.sender-email=test@example.com",
        "app.email.sender-name=Project Pulse Tests",
        "app.domain.frontend=http://localhost",
        "jwt.secretKey=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.expirationTime=36000",
        "outbound.identity.client-id=test-client",
        "outbound.identity.client-secret=test-secret",
        "outbound.identity.redirect-uri=http://localhost/callback",
        "outbound.identity.grant-types=authorization_code",
        "spring.ai.openai.api-key=test-api-key"
})
class ProjectPulseBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
