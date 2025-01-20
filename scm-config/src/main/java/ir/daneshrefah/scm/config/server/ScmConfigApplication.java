package ir.daneshrefah.scm.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
@EnableConfigurationProperties
public class ScmConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScmConfigApplication.class, args);
	}

}
