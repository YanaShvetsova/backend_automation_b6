package api.scripts.tg_application;

import api.pojo_classes.tg_application.CreateUser;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import api.pojo_classes.tg_application.CreateUser;
import api.pojo_classes.tg_application.UpdateUser;
import utils.ConfigReader;
import utils.DBUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIProject03 {
    RequestSpecification baseSpec;
    Response response;
    Faker faker = new Faker();

    @BeforeMethod
    public void setAPI(){

        baseSpec = new RequestSpecBuilder().log(LogDetail.ALL)
                .setBaseUri(ConfigReader.getProperty("TGSchoolBaseURI"))
                .setContentType(ContentType.JSON)
                .build();
        DBUtil.createDBConnection();

    }

    @Test
    public void tg_applicationAPI(){

        //1. Create a new user
        CreateUser createNewUser = CreateUser.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .dob("2000-01-01")
                .build();

        response = RestAssured.given()
                .spec(baseSpec)
                .body(createNewUser)
                .when().post("/students")
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .extract().response();

        int id = response.jsonPath().getInt("id");

        String query = "SELECT * FROM STUDENT WHERE id = " + id;

        List<List<Object>> queryResult = DBUtil.getQueryResultList(query);
        List<Object> dbResult = queryResult.get(0);

        BigDecimal dbId = (BigDecimal) dbResult.get(0);
        int dbIdInt = dbId.intValue();

        List<Object> dBResult = new ArrayList<>(dbResult);
        dBResult.set(0, dbIdInt);

        for (Object o : dBResult) {
            System.out.println(o);
        }

        Assert.assertEquals(dBResult, Arrays.asList(id, createNewUser.getDob(),
                createNewUser.getEmail(), createNewUser.getFirstName(),
                createNewUser.getLastName()));

        //2. Retrieve a specific user-created
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get("/students/" + String.valueOf(id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .extract().response();

        String query2 = "SELECT * FROM STUDENT WHERE id = " + id;

        List<List<Object>> queryResult2 = DBUtil.getQueryResultList(query2);
        List<Object> dbResult1 = queryResult2.get(0);

        BigDecimal dbId1 = (BigDecimal) dbResult1.get(0);
        int dbIdInt1 = dbId1.intValue();

        List<Object> dBResult2 = new ArrayList<>(dbResult1);

        dBResult2.set(0, dbIdInt1);

        for (Object o : dBResult2) {
            System.out.println(o);
        }

        Assert.assertEquals(dBResult2, Arrays.asList(id, createNewUser.getDob(),
                createNewUser.getEmail(), createNewUser.getFirstName(),
                createNewUser.getLastName()));

        //3. Update an existing user
        UpdateUser updatedNewUser = UpdateUser.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .dob("2006-02-02")
                .build();

        response = RestAssured.given()
                .spec(baseSpec)
                .body(updatedNewUser)
                .when().put("/students/" + String.valueOf(id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .extract().response();

        String query3 = "SELECT * FROM STUDENT WHERE id = " + id;
        List<List<Object>> queryResult3 = DBUtil.getQueryResultList(query3);
        List<Object> dbResult3 = queryResult3.get(0);

        BigDecimal dbId2 = (BigDecimal) dbResult3.get(0);
        int dbIdInt2 = dbId2.intValue();

        List<Object> updatedDBResult3 = new ArrayList<>(dbResult3);

        updatedDBResult3.set(0, dbIdInt2);

        for (Object o : updatedDBResult3) {
            System.out.println(o);
        }

        Assert.assertEquals(updatedDBResult3, Arrays.asList(id, updatedNewUser.getDob(),
                updatedNewUser.getEmail(), updatedNewUser.getFirstName(),
                updatedNewUser.getLastName()));

        //4. Retrieve a specific user created to confirm the update.
        response = RestAssured.given()
                .spec(baseSpec)
                .when().get("/students/" + String.valueOf(id))
                .then().log().all()
                .assertThat().statusCode(200)
                .time(Matchers.lessThan(4000L))
                .extract().response();

        String query4 = "SELECT * FROM STUDENT WHERE id = " + id;
        List<List<Object>> queryResult4 = DBUtil.getQueryResultList(query4);
        List<Object> dbResult4 = queryResult4.get(0);

        BigDecimal dbId3 = (BigDecimal) dbResult4.get(0);
        int dbIdInt3 = dbId3.intValue();

        List<Object> updatedDBResult4 = new ArrayList<>(dbResult4);

        updatedDBResult4.set(0, dbIdInt3);

        for (Object o : updatedDBResult4) {
            System.out.println(o);
        }

        Assert.assertEquals(updatedDBResult4, Arrays.asList(id, updatedNewUser.getDob(),
                updatedNewUser.getEmail(), updatedNewUser.getFirstName(),
                updatedNewUser.getLastName()));

        //5. Finally, delete the user that you created.
        response = RestAssured.given()
                .spec(baseSpec)
                .when().delete("/students/")
                .then().log().all()
                .assertThat().statusCode(200)
                .extract().response();

        queryResult4 = DBUtil.getQueryResultList(query4);
        Assert.assertTrue(queryResult4.isEmpty());

    }
}