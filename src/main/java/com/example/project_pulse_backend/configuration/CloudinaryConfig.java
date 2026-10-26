package com.example.project_pulse_backend.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class CloudinaryConfig {

    @Value("${app.cloud.name}")
    private String cloudName;

    @Value("${app.cloud.key}")
    private String cloudKey;

    @Value("${app.cloud.secret}")
    private String cloudSecret;

    @Bean
    Cloudinary cloudinary() {
        return new Cloudinary(//
                ObjectUtils.asMap(//
                        "cloud_name", cloudName, //
                        "api_key", cloudKey, //
                        "api_secret", cloudSecret, //
                        "secure", true));
    }

}
