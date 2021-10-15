package com.eshop.common.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileProperties {

    private String path;
    private String avatar;
    private String maxSize;
    private String avatarMaxSize;

}
