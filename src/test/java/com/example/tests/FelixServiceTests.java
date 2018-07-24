package com.example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.osgi.FelixService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class FelixServiceTests {

	@Autowired 
	private FelixService fs;
	
    @Test
    public void felixServiceLoads() throws Exception {
    	assertThat(fs).isNotNull();
    }

}
