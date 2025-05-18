package photo_mgmt_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import photo_mgmt_backend.security.util.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class PhotoMgmtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoMgmtBackendApplication.class, args);
	}

}
