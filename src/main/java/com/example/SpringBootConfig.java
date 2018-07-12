package com.example;

import java.util.HashMap;
import java.util.Properties;

import org.springframework.stereotype.Service;

@Service
public interface SpringBootConfig {
	Properties getProperties();
	HashMap<String,String> getHashMap();
	void loadProps();
}
