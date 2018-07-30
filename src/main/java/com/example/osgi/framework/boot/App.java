package com.example.osgi.framework.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({
    @PropertySource("classpath:application-${spring.profiles.active}.properties")
})
public class App{
	
	public static void main(String[] args) {		
		SpringApplication.run(App.class, args);
	}
	
	// Note: Need to specify "Bundle-ManifestVersion: 2" in any bundle used in this framework

}