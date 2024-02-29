package org.posteducation.api.utils;

import io.restassured.authentication.PreemptiveOAuth2HeaderScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestUtils {
    private static final Logger log = LogManager.getLogger(TestUtils.class);
    public static Properties properties= new Properties();
    private static final RequestSpecification requestSpecWithAuth;
    private static final RequestSpecification requestSpecWithHeaderAuth;
    private static final RequestSpecification requestSpecWithHeader;
    private static final ResponseSpecification responseSpec;
    private static final ResponseSpecification responseSpecCreate;
    private static final ResponseSpecification responseSpecDelete;

    static {

        try {
            properties.load(new FileInputStream("src/test/resources/configs.properties"));
        } catch (IOException e){
            log.error(e);
        }
        //Request specification with base Uri, with Authentication
        PreemptiveOAuth2HeaderScheme oAuth2Scheme = new PreemptiveOAuth2HeaderScheme();
        oAuth2Scheme.setAccessToken(properties.getProperty("ACCESS_TOKEN"));
        requestSpecWithAuth = new RequestSpecBuilder().
                setBaseUri(properties.getProperty("ROOT_ENDPOINT")).
                setAuth(oAuth2Scheme).
                build();

        requestSpecWithHeaderAuth = new RequestSpecBuilder().
                addHeader("Authorization", "Bearer " + properties.getProperty("ACCESS_TOKEN")).
                setBaseUri(properties.getProperty("ROOT_ENDPOINT")).
                setAuth(oAuth2Scheme).
                build();

        //Request specification with base Uri, header with Authorization and CLIENT_ID
        requestSpecWithHeader = new RequestSpecBuilder().
                addHeader("Authorization", "Client-ID " + properties.getProperty("CLIENT_ID")).
                setBaseUri(properties.getProperty("ROOT_ENDPOINT")).
                build();

        //Response specification with expected status code 200 and content type JSON
        responseSpec = new ResponseSpecBuilder().
                expectStatusCode(HttpServletResponse.SC_OK).
                expectContentType(ContentType.JSON).
                build();

        //Response specification with expected status code 201 CREATED
        responseSpecCreate = new ResponseSpecBuilder().
                expectStatusCode(HttpServletResponse.SC_CREATED).
                build();

        //Response specification with expected status code 204 DELETE
        responseSpecDelete = new ResponseSpecBuilder().
                expectStatusCode(HttpServletResponse.SC_NO_CONTENT).
                build();
    }

    public static Properties getProperties() {
        return properties;
    }

    public static RequestSpecification getRequestSpecWithAuth() {
        return requestSpecWithAuth;
    }

    public static RequestSpecification getRequestSpecWithHeader() {
        return requestSpecWithHeader;
    }

    public static RequestSpecification getRequestSpecWithHeaderAuth(){
        return requestSpecWithHeaderAuth;
    }

    public static ResponseSpecification getResponseSpec() {
        return responseSpec;
    }
    public static ResponseSpecification getResponseSpecCreate() {
        return responseSpecCreate;
    }
    public static ResponseSpecification getResponseSpecDelete() {
        return responseSpecDelete;
    }

    public static boolean isStatusOk (Response response){
        if(response.getContentType().equals("application/json")){
            return (Integer) response.
                    then().
                    extract().
                    path("status") == HttpServletResponse.SC_OK;
        }
        return false;
    }
}
