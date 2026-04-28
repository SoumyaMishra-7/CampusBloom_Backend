package com.campusbloom.backend.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path uploadsRootDirectory;

    public WebConfig(@Value("${app.certificates.upload-dir:uploads/certificates}") String uploadDir) {
        Path uploadDirectory = Path.of(uploadDir).toAbsolutePath().normalize();
        this.uploadsRootDirectory = uploadDirectory.getParent() == null ? uploadDirectory : uploadDirectory.getParent();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsRootDirectory.toUri().toString());
    }
}
