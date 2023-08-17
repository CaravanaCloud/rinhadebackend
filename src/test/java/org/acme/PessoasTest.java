package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PessoasTest {

    @Test
    public void testContagemPessoas() {
        given()
          .when().get("/contagem-pessoas")
          .then()
             .statusCode(200)
             .body(is("0"));
    }

}
