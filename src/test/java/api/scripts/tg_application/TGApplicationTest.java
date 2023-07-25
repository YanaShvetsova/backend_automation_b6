package api.scripts.tg_application;

import api.pojo_classes.tg_application.CreateUser;
import api.pojo_classes.tg_application.UpdateUser;
import api.pojo_classes.tg_application.PatchUser;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static org.hamcrest.Matchers.equalTo;

public class TGApplicationTest {

    Response response;
    Faker faker = new Faker();
    RequestSpecification baseSpec;


    @BeforeMethod
    public void setAPI(){
        baseSpec = new RequestSpecBuilder().log(LogDetail.ALL)
                .setBaseUri(ConfigReader.getProperty("TG_Application"))
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test
    public void tg_applicationAPI(){
        //1. Retrieve a list of all users.
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get()
                .then().log().all()
                .extract().response();

        //2. Create a new user
        CreateUser createNewUser = CreateUser.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .dob("2000-01-01")
                .build();

        response = RestAssured.given()
                .spec(baseSpec)
                .body(createNewUser)
                .when().post()
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .body("firstName", equalTo(createNewUser.getFirstName()))
                .body("lastName", equalTo(createNewUser.getLastName()))
                .body("email", equalTo(createNewUser.getEmail()))
                .body("dob", equalTo(createNewUser.getDob()))
                .extract().response();

        int user_id = response.jsonPath().getInt("id");

        //3. Retrieve a specific user-created
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get(String.valueOf(user_id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .body("firstName", equalTo(createNewUser.getFirstName()))
                .body("lastName", equalTo(createNewUser.getLastName()))
                .body("email", equalTo(createNewUser.getEmail()))
                .body("dob", equalTo(createNewUser.getDob()))
                .extract().response();

        //4. Update an existing user
        UpdateUser updateUser = UpdateUser.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .dob("2001-02-02")
                .build();

        response = RestAssured.given()
                .spec(baseSpec)
                .body(updateUser)
                .when().put(String.valueOf(user_id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .body("firstName", equalTo(updateUser.getFirstName()))
                .body("lastName", equalTo(updateUser.getLastName()))
                .body("email", equalTo(updateUser.getEmail()))
                .body("dob", equalTo(updateUser.getDob()))
                .extract().response();

        //5. Partially update an existing User
        PatchUser patchUser = PatchUser.builder()
                .email(faker.internet().emailAddress())
                .dob("1980-01-02")
                .build();

        response = RestAssured.given()
                .spec(baseSpec)
                .body(patchUser)
                .when().patch(String.valueOf(user_id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .body("email", equalTo(patchUser.getEmail()))
                .body("dob", equalTo(patchUser.getDob()))
                .extract().response();

        //6. Retrieve a list of all users again
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get()
                .then().log().all()
                .extract().response();

        //7. Retrieve a specific user created to confirm the update.
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get(String.valueOf(user_id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .body("firstName", equalTo(updateUser.getFirstName()))
                .body("lastName", equalTo(updateUser.getLastName()))
                .body("email", equalTo(patchUser.getEmail()))
                .body("dob", equalTo(patchUser.getDob()))
                .extract().response();

        //8. Finally, delete the user that you created.
        response = RestAssured.given()
                .spec(baseSpec)
                .when().delete()
                .then().log().all()
                .extract().response();
    }
}
