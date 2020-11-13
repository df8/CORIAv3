package com.coria.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Sebastian Gross, 2017
 * Extended by David Fradin, 2020: Adapted for GraphQL tests.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
/*
According to documentation, @GraphQLTest shall be used instead of @SpringBootTest.
The tests failed to initialize the AppContext class, failing every test with a NullPointerException.
I found a temporary fix in the comments of a GitHub issue: https://github.com/graphql-java-kickstart/graphql-spring-boot/issues/113
combined with the following pull request: https://github.com/thirdwavelist/barista/commit/6852534a6caae9b09a1b50a1dc59121045ba7de3
*/

public abstract class BaseTest {
    protected TestRestTemplate restTemplate;
    protected ObjectMapper objectMapper;
    protected GraphQLTestTemplate graphQLTestTemplate;

    @Autowired
    public void setRestTemplate(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setGraphQLTestTemplate(GraphQLTestTemplate graphQLTestTemplate) {
        this.graphQLTestTemplate = graphQLTestTemplate;
    }

    @Value("${graphql.servlet.mapping:/graphql}")
    protected String graphqlMapping;


    protected String readResourceToString(String fileName) throws IOException {
        byte[] bytes = readResourceToBytes(fileName);
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    protected byte[] readResourceToBytes(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        java.net.URL res = classLoader.getResource(fileName);
        if (res != null) {
            return Files.readAllBytes((new File(res.getFile())).toPath());
        }
        return null;
    }
}
