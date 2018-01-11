import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


/*******************************************************************************************
 * This is practice using the Rest Assured testing framework to write automated tests.
 * The tests in this file test the Ergast Motor Racing Database API. It's was the API for
 * the tutorial I read which can be found here:
 * https://techbeacon.com/how-perform-api-testing-rest-assured
 *
 * Some the tests I followed along and wrote, and as I got more comfortable with Rest Assured
 * I started making up my own tests. To verify, I tested the endpoints in the browser as well.
 * This is definitely not a how-to or tutorial.
 *
 * Per the Git Repo:
 *
 * Testing and validation of REST services in Java is harder than in dynamic languages
 * such as ... Groovy. REST Assured brings the simplicity of using these languages
 * into the Java domain.
 *
 * But I think the Java Doc does a better job in summing up what Rest Assured is:
 *
 * REST Assured is a Java DSL for simplifying testing of REST based services
 * built on top of HTTP Builder.
 * *******************************************************************************************/


public class RestTutorial {


    public RestTutorial() { }

    /*******************************************************************************************
     * This is an example test of an API endpoint. In this test I'm verifying that the body of
     * the response has 20 circuitId elements.
     * *******************************************************************************************/

    @Test
    public void testNumCircuits_2017() {
        given() // given that:
            .when() // when the following happens:
                .get("http://ergast.com/api/f1/2017/circuits.json") // make the GET request to the API
            .then() // then with the response:
                .assertThat() // assert that the following are true:
                .body("MRData.CircuitTable.Circuits.circuitId", hasSize(20)); // the body contains 20 circuitId elements
                // I also used Groovy path notation for the JSON response data
    }

    /*******************************************************************************************
     * This is an example test of an API endpoint with the parameters being passed as
     * query parameters in the path
     *
     * e.g http://thing.com/?text="Hey I'm a query param value!!!"
     *
     * In this test, I'm testing to validate that the checksum requested is the one I expected
     * *******************************************************************************************/

    @Test
    public void testResponseHeader_Correct() {
        given(). // given that:
                when(). // when the following happens:
                    get("http://ergast.com/api/f1/2017/circuits.json"). // make the HTTP GET request to the endpoint
                then(). // then do the following with the response:
                    assertThat(). // assert that the following is true:  *** And if it's not, we'll get an assertion error
                        statusCode(200). // that the response status code is 200....which is OK
                        and(). // and
                        contentType(ContentType.JSON). // that the Content-Type in the header is JSON
                        and(). // and
                        header("Content-Length", equalTo("4551")); // that the Content-Length in the header 4551
    }

    /*******************************************************************************************
     * This is an example test of an API endpoint with the parameters being passed as
     * query parameters in the path
     *
     * e.g http://thing.com/?text="Hey I'm a query param value!!!"
     *
     * In this test, I'm testing to validate that the checksum requested is the one I expected
     *
     * NOTE: When passing query params, you must use the '.param("paramName", value)' method.
     * *******************************************************************************************/

    @Test
    public void testChecksum() {
        String text = "oohrah";
        String checksum = "4d69131dd7eaed4aedbafd4333c1ccf1"; // this is my expected checksum... I tested this md5 endpoint in the browser first

        given().
                param("text", text). // when using query
                when().get("http://md5.jsontest.com").
                then().
                assertThat().
                body("md5", equalTo(checksum));
    }

    /******************************************************************************************************
     * This is an example test of an API endpoint with the parameters being passed in the path
     * e.g "http://thing.com/api/2017/circuits.json   where year=2017
     *
     * just like passing query params in
     * e.g.http://thing.com/api/?year=2017
     *
     * In this test, I'm testing to make sure that the is a response is a 404 when
     * I pass a bad parameter to the endpoint.
     *
     * NOTE: When passing params in the path, you must use the '.pathParam("paramName", value)' method
     * *****************************************************************************************************/

    @Test
    public void testNumCircuits_params() {
        String season = "2017";

        given()
                .pathParam("season", season)
                .when()
                .get("http://ergast.com/api/f1/{season}/circuits.json")
                .then()
                .assertThat()
                .body("MRData.CircuitTable.Circuits.circuitId", hasSize(20));
    }

   /*******************************************************************************************
    * This is an example test of an API endpoint with the parameters being passed in the path
    * e.g "http://thing.com/api/2017/circuits.json   where year=2017
    *
    * just like passing query params in
    * e.g.http://thing.com/api/?year=2017
    *
    * In this test, I'm testing to make sure that the is a 404 when I pass a bad parameter to
    * the endpoint.
    * *******************************************************************************************/

   @Test
    public void testNumCircuits_mismatchParam_404() {
        String goodSeason = "2017";
        String badParam = "f2";

        given()
                .pathParam("season", goodSeason) // make the param reference
                .pathParam("badParam", badParam) // bad param f2 racing doesn't exist
                .when()
                .get("http://ergast.com/api/{badParam}/{season}/circuits.json") // make the request with the bad param
                .then()
                .assertThat()
                .statusCode(404); // then make sure the status code is 404
    }

    /*******************************************************************************************
     * This is an example of passing parameters between tests. Sometimes I need to capture a
     * value of the response of one API call and re-use it in another API call.
     *
     * In this test, I'm getting the circuit Id in a String object from the response, and passing
     * it to the next request in order to use that variable to verify the the response returns the
     * correct Location data.
     *
     * Should verify the location data for Austin, Tx
     * lat	"30.1328"
     * long	"-97.6411"
     * locality	"Austin"
     * country	"USA"
     *
     * NOTE: when capturing the response in a variable or object, you must use the '.extract()' method
     * *******************************************************************************************/

    @Test
    public void test_retrieves_circuitName_then_verifies_Location() {



        // get the circuitId element of the second element using the '.extract()' method from the response
        String circuitId = given()
                .when()
                .get("http://ergast.com/api/f1/2017/circuits.json")
                .then()
                .extract()
                .path("MRData.CircuitTable.Circuits.circuitId[1]");

        // once we have the id in a string, I can pass it to the next request
        given().
                pathParam("circuitId", circuitId). // pass the circuitId as a path param
                when(). // when the following happens:
                get("http://ergast.com/api/f1/circuits/{circuitId}.json"). // make the HTTP GET request
                then(). // then do the following with the response:
                assertThat(). // assert that the following is true:
                body("MRData.CircuitTable.Circuits.Location[0].country", equalTo("USA")). // the latitude in the response is 30.1328
                and().
                body("MRData.CircuitTable.Circuits.Location[0].lat", equalTo("30.1328")). // the longitude in the response is -97.6411
                and().
                body("MRData.CircuitTable.Circuits.Location[0].long", equalTo("-97.6411")); // the longitude in the response is -97.6411
    }



}
