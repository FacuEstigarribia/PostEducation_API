package apitests;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.posteducation.api.models.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.posteducation.api.utils.TestUtils.*;

public class UsersTest {

    private static final Log LOGGER = LogFactory.getLog(UsersTest.class);
    static Properties properties= new Properties();

    private static RequestSpecification requestSpecWithHeaderAuth;

    private static ResponseSpecification responseSpec;  //GET

    private static ResponseSpecification responseSpecCreate;  //POST

    private static ResponseSpecification responseSpecDelete; //DELETE

    @BeforeClass
    public void beforeClassConfiguration(){
        properties = getProperties();



        requestSpecWithHeaderAuth = getRequestSpecWithHeaderAuth();

        //Response specification with expected status code 200 and content type JSON
        responseSpec = getResponseSpec();

        //Response specification with expected status code 201 and content type JSON
        responseSpecCreate = getResponseSpecCreate();

        //Response specification with expected status code 204 and content type JSON
        responseSpecDelete = getResponseSpecDelete();
    }

    @DataProvider(name = "testUserGet")
    public Object [][] dataForGetUser(){
        return new Object[][]{
                {6750448}
        };
    }
    @Test(dataProvider = "testUserGet")
    public void testGetUser(Integer userId){
        InputStream getUserJsonSchema = getClass().getClassLoader()
                .getResourceAsStream("getUserSchema.json");

        Response response = given().
                spec(requestSpecWithHeaderAuth).
                pathParam("userId", userId).
                when().
                get(properties.getProperty("USERS_ENDPOINT") + "{userId}");
        response.prettyPrint();
        response.then().
                spec(responseSpec).
                log().body();

        if(isStatusOk(response) && getUserJsonSchema != null){
            User user = response.as(User.class);
            response.
                    then().
                    assertThat().
                    body(JsonSchemaValidator.matchesJsonSchema(getUserJsonSchema));
            Assert.assertEquals(user.getData().getName(),"This is user 200");
        }
    }

    @Test(dataProvider = "testUserGet")
    public void getEmailOfUserById(Integer userId) {
        Response response = given()
                .spec(requestSpecWithHeaderAuth)
                .pathParam("userId", userId)
                .when()
                .get(properties.getProperty("USERS_ENDPOINT") + "{userId}");

        LOGGER.info("User data: " + response.getContentType());
        if (response.getStatusCode() == 200) {
            User user = response.as(User.class);
            if (user.getData() != null) {
                String userEmail = user.getData().getEmail();
                Assert.assertNotNull(userEmail, "Email should not be null");
                Assert.assertFalse(userEmail.isEmpty(), "Email should not be empty");
                LOGGER.info("Email of user with ID " + userId + ": " + userEmail);
            } else {
                Assert.fail("User not found for ID: " + userId);
            }
        } else {
            Assert.fail("Failed to get user. Status Code: " + response.getStatusCode());
        }
    }

    @Test(dataProvider = "testUserGet")
    public void getStatusOfUserById(String userId) {
        Response response = given()
                .spec(requestSpecWithHeaderAuth)
                .pathParam("userId", userId)
                .when()
                .get(properties.getProperty("USERS_ENDPOINT") + userId);

        if (response.getStatusCode() == 200) {
            User user = response.as(User.class);
            if (user.getData() != null) {
                String userStatus = user.getData().getStatus();
                Assert.assertNotNull(userStatus, "Status should not be null");
                Assert.assertFalse(userStatus.isEmpty(), "Status should not be empty");
                LOGGER.info("Status of user with ID " + userId + ": " + userStatus);
            } else {
                Assert.fail("User not found for ID: " + userId);
            }
        } else {
            Assert.fail("Failed to get user. Status Code: " + response.getStatusCode());
        }
    }


    @DataProvider(name = "dataForUpdateUser")
    public Object [][] dataUpdateUser(){
        return new Object[][]{
                {6732304, "lapicera"}
        };
    }

    @Test(dataProvider = "dataForUpdateUser")
    public void testUpdateUserNameById(int userId, String newUserName) {

        Response getResponse = given()
                .spec(requestSpecWithHeaderAuth)
                .pathParam("userId", userId)
                .when()
                .get(properties.getProperty("USERS_ENDPOINT") + "{userId}");

        if (getResponse.getStatusCode() == 200) {
            User user = getResponse.as(User.class);
            if (user.getData() != null) {

                String currentUserName = user.getData().getName();
                Response updateResponse = given()
                        .spec(requestSpecWithHeaderAuth)
                        .pathParam("userId", userId)
                        .formParam("name", newUserName)
                        .when()
                        .put(properties.getProperty("USERS_ENDPOINT") + "{userId}");

                if (updateResponse.getStatusCode() == 200) {
                    Assert.assertNotEquals(currentUserName, newUserName,
                            "User name should be updated.");

                    LOGGER.info("User name successfully updated for user ID " + userId);
                } else {

                    Assert.fail("Failed to update user name. Status Code: " + updateResponse.getStatusCode());
                }
            } else {
                Assert.fail("User not found for ID: " + userId);
            }
        } else {
            Assert.fail("Failed to get user. Status Code: " + getResponse.getStatusCode());
        }
    }




    @DataProvider(name = "testPostData")
    public Object[][] testPostUserData(){
        return new Object[][]{
                {"phone", "pwork@yahoo.com", "male", "active"}
        };
    }


    @Test(dataProvider = "testPostData")
    public void testPostUser(String name, String email, String gender, String status){

        given().
                spec(requestSpecWithHeaderAuth).
//                queryParam("id" , id).
                queryParam("name" , name).
                queryParam("email", email).
                queryParam("gender", gender).
                queryParam("status", status).
                when().
                post(properties.getProperty("USERS_ENDPOINT")).
                then().
                spec(responseSpecCreate);
    }

    @DataProvider(name = "deleteUser")
    public Object[][] deleteUserData(){
        return new Object[][]{
                {6750430}
        };
    }

    @Test(dataProvider = "deleteUser")
    public void testDeleteUser(Integer id){
        given().
                spec(requestSpecWithHeaderAuth).

                pathParam("userId", id).
                when().
                delete(properties.getProperty("USERS_ENDPOINT") + "{userId}").
                then().
                spec(responseSpecDelete);
    }

}
