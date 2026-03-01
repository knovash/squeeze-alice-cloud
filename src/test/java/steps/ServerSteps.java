package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.knovash.alicebroker.Main;
import org.knovash.alicebroker.Server;
import static io.restassured.RestAssured.*;

public class ServerSteps {

    private static final int TEST_PORT = 8081;

    @Before
    public void setup() {
        Main.config.port = TEST_PORT;
        Server.start();
    }

    @After
    public void teardown() {
//        Server.stop();
    }

    @Given("the server is running")
    public void serverIsRunning() {
        // Сервер уже запущен в @Before
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String endpoint) {
        given().port(TEST_PORT).when().get(endpoint);
    }

    @Then("the response status code should be {int}")
    public void checkStatusCode(int statusCode) {
//        then().statusCode(statusCode);
    }


}