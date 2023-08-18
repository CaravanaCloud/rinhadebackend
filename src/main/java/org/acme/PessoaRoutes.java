package org.acme;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Instrucoes em https://github.com/zanfranceschi/rinha-de-backend-2023-q3/blob/main/INSTRUCOES.md
 */
@ApplicationScoped
public class PessoaRoutes {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Route(methods = Route.HttpMethod.GET, path = "/contagem-pessoas", order = 2)
    Uni<Long> countPessoas() {
        return sessionFactory.
                withSession(session ->
                        session.createNamedQuery("Pessoa.count", Long.class)
                                .getSingleResult());
    }

    @Route(methods = Route.HttpMethod.POST, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void createPessoa(RoutingContext routingContext, RoutingExchange ex) {
        try {
            JsonObject jsonObject = routingContext.body().asJsonObject();
            Map<String, Object> attrs = jsonObject.getMap();
            if (isInvalid(attrs)) {
                unprocessable(ex, "invalid");
                return;
            }
            if (isSyntheticallyInvalid(attrs)) {
                badRequest(ex, "synthetically-invalid");
                return;
            }
            var pessoa = jsonObject.mapTo(Pessoa.class);
            sessionFactory.withStatelessSession(session -> session.insert(pessoa))
                    .subscribe()
                    .with(
                            v -> response(ex, 201)
                                    .putHeader("Location", "/pessoas/" + pessoa.id)
                                    .end(),
                            t -> {
                                if (t instanceof ConstraintViolationException){
                                    unprocessable(ex, "constraint violation:" + t.getMessage());
                                } else {
                                    unprocessable(ex, "insert failed:"+t.getMessage());
                                }
                            });
        } catch (ConstraintViolationException e) {
            unprocessable(ex, "constraint violation:" + e.getMessage());
        }
        catch (IllegalArgumentException e){
            unprocessable(ex, "illegal argument:" + e.getMessage());
        }
        catch (DateTimeParseException e) {
            unprocessable(ex, "unable to parse:" + e.getMessage());
        }
        catch (DateTimeException e) {
            unprocessable(ex, "date format is invalid: "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            unprocessable(ex, e.getMessage());
        }
    }

    private Void badRequest(RoutingExchange ex, String message) {
        ex.response()
                .putHeader("x-bad-request-msg", message)
                .setStatusCode(422)
                .end();
        return null;
    }
    private void unprocessable(RoutingExchange ex, String message) {
        response(ex,422)
                .putHeader("x-unacceptable-message", message)
                .end();
    }

    private HttpServerResponse response(RoutingExchange ex, int status) {
        return ex.response().setStatusCode(status);
    }

    @Route(methods = Route.HttpMethod.GET, path = "/pessoas/:id", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void findPessoaById(@Param("id") Optional<String> uuidParam, RoutingExchange ex) {
        // TODO: UUID pode ser inválido
        if (uuidParam.isEmpty()) {
            ex.response().setStatusCode(400).end();
            return;
        }
        try {
            UUID uuid = UUID.fromString(uuidParam.get());
            sessionFactory.withSession(session -> session.find(Pessoa.class, uuid)).subscribe().with(
                    pessoa -> {
                        if (pessoa == null) {
                            ex.response().setStatusCode(404).end();
                        } else {
                            ex.response().setStatusCode(200).end(JsonObject.mapFrom(pessoa).encode());
                        }
                    },
                    t -> ex.response().setStatusCode(500).end()
            );
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            ex.response().setStatusCode(400).end();
        }
    }


    @Route(methods = Route.HttpMethod.GET, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 3)
    Uni<List<Pessoa>> findPessoa(@Param("t") String termo, RoutingExchange ex) {
        if (termo == null || termo.isEmpty()) {
            ex.response().setStatusCode(400).end();
            return Uni.createFrom().nothing();
        }
        return sessionFactory.withSession(session ->
                session.createNamedQuery("Pessoa.findByTermo", Pessoa.class)
                        .setParameter("termo", "%" + termo.toLowerCase(Locale.ENGLISH) + "%")
                        .setMaxResults(50).getResultList());
    }

    private boolean isSyntheticallyInvalid(Map<String, Object> fields) {
        if (!(fields.get("apelido") instanceof String)) return true;
        if (!(fields.get("nome") instanceof String)) return true;
        if (!(fields.get("stack") instanceof List)) return true;
        return false;
    }

    private boolean isInvalid(Map<String, Object> fields) {
        if (fields.get("apelido") == null) return true;
        if (fields.get("nome") == null) return true;
        if (fields.get("nascimento") == null) return true;
        return false;
    }

}
