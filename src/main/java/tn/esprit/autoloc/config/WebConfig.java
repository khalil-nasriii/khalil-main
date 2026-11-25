package tn.esprit.autoloc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * Atelier 8 : sérialisation stable des Page en JSON
 * ({ "content": [...], "page": { "size", "number", "totalElements", "totalPages" } }).
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebConfig {
}
