package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
public class PessoasTest {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory.withStatelessTransaction((session, tx) -> session.createQuery("delete from Pessoa").executeUpdate())
                .await().indefinitely();
    }

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
        String location = given()
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
                .statusCode(201)
                .extract().header("Location");

        Pessoa pessoa = given()
                .when()
                .header("Content-Type", "application/json")
                .get(location)
                .then().statusCode(200)
                .extract().body().as(Pessoa.class);

        assertThat(pessoa.id).isNotNull();
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
                .log().all()
                .statusCode(422);
    }

    @Test
    public void testProcurarPorTermo() {
        given()
                .when()
                .header("Content-Type", "application/json")
                .body("""
                        {
                            "apelido" : "joaquim",
                            "nome" : "Joaquim Silva",
                            "nascimento" : "1980-12-01",
                            "stack" : ["C#", "Node", "Oracle", "Java"]
                        }
                        """)
                .post("/pessoas")
                .then()
                .statusCode(201);

        given()
                .when()
                .header("Content-Type", "application/json")
                .get("/pessoas?t=silva")
                .then().statusCode(200);
    }

    @Test
    public void termoVazioDeveRetornar400() {
        given()
                .when()
                .header("Content-Type", "application/json")
                .get("/pessoas")
                .then().statusCode(400);
    }

    @Test
    public void apelidoDeveSerUnico() {
        given()
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
                .statusCode(201);

        given()
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
                .statusCode(422);
    }
}
