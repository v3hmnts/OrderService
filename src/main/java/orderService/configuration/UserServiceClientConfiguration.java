package orderService.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfiguration {

    @Value("${user-service.uri}")
    private String userServiceUri;

    public RestClient userServiceRestClient(){
        return RestClient.create(userServiceUri);
    }

}
