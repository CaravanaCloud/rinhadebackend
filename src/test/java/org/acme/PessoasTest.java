package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
public class PessoasTest {

    @Test
    public void testContagemPessoas() {
        given()
                .when().get("/contagem-pessoas")
                .then()
                .statusCode(200)
                .body(matchesRegex("[0-9]"));
    }


    @Test
    public void testCriarPessoaValida() {
        Pessoa novaPessoa = given()
                .when()
                .header("Content-Type", "application/json")
                .body("""
                        {
                            "apelido" : "josé",
                            "nome" : "José Roberto",
                            "nascimento" : "2000-10-01",
                            "stack" : ["C#", "Node", "Oracle"]
                        }
                        """)
                .post("/pessoas")
                .then()
                .statusCode(200)
                .extract().body().as(Pessoa.class);
        assertThat(novaPessoa.id).isNotNull();
        assertThat(novaPessoa.apelido).isEqualTo("josé");

        Pessoa getPessoa = given()
                .when()
                .header("Content-Type", "application/json")
                .get("/pessoas/" + novaPessoa.id)
                .then().statusCode(200)
                .extract().body().as(Pessoa.class);

        assertThat(getPessoa)
                .usingRecursiveComparison()
                .isEqualTo(novaPessoa);
    }

    @Test
    public void testCriarPessoaComRequisicaoSintaticamenteInvalida() {
        given()
                .when()
                .header("Content-Type", "application/json")
                .body("""
                        {
                            "apelido" : "josé",
                            "nome" : 1,
                            "nascimento" : "2000-10-01",
                            "stack" : ["C#", "Node", "Oracle"]
                        }
                        """)
                .post("/pessoas")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCriarPessoaComRequisicaoInvalida() {
        given()
                .when()
                .header("Content-Type", "application/json")
                .body("""
                        {
                            "apelido" : "josé",
                            "nome" : null,
                            "nascimento" : "2000-10-01",
                            "stack" : ["C#", "Node", "Oracle"]
                        }
                        """)
                .post("/pessoas")
                .then()
                .statusCode(422);
    }
}
