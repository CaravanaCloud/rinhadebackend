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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static final Logger log = LoggerFactory.getLogger(PessoaRoutes.class);
    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Route(methods = Route.HttpMethod.GET, path = "/contagem-pessoas", order = 2)
    Uni<Long> getContagemPessoas() {
        return sessionFactory.
                withSession(session ->
                        session.createNamedQuery("Pessoa.count", Long.class)
                                .getSingleResult());
    }

    @Route(methods = Route.HttpMethod.POST, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void postCreatePessoa(RoutingContext routingContext, RoutingExchange ex) {
        try {
            var jsonObject = routingContext.body().asJsonObject();
            var attrs = jsonObject.getMap();
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
                                if (t instanceof ConstraintViolationException) {
                                    unprocessable(ex, "insert constraint violation:" + t.getMessage());
                                } else {
                                    unprocessable(ex, "insert failed:" + t.getMessage());
                                }
                            });
        } catch (ConstraintViolationException e) {
            unprocessable(ex, "constraint violation:" + e.getMessage());
        } catch (IllegalArgumentException e) {
            unprocessable(ex, "illegal argument:" + e.getMessage());
        } catch (DateTimeParseException e) {
            unprocessable(ex, "unable to parse:" + e.getMessage());
        } catch (DateTimeException e) {
            unprocessable(ex, "date format is invalid: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            unprocessable(ex, e.getMessage());
        }
    }


    @Route(methods = Route.HttpMethod.GET, path = "/pessoas/:id", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void getPessoaById(@Param("id") Optional<String> uuidParam, RoutingExchange ex) {
        if (uuidParam.isEmpty()) {
            badRequest(ex, "empty uuid");
            return;
        }
        try {
            UUID uuid = UUID.fromString(uuidParam.get());
            sessionFactory.withSession(session -> session.find(Pessoa.class, uuid)).subscribe().with(
                    pessoa -> {
                        if (pessoa == null) {
                            notFound(ex, "pessoa not found");
                        } else {
                            var body = JsonObject.mapFrom(pessoa).encode();
                            ok(ex, body);
                        }
                    },
                    t -> serverError(ex, t.getMessage())
            );
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            badRequest(ex, "illegal argument" + e.getMessage());
        }
    }


    @Route(methods = Route.HttpMethod.GET, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 3)
    Uni<List<Pessoa>> findPessoa(@Param("t") String termo, RoutingExchange ex) {
        if (termo == null || termo.isEmpty()) {
            badRequest(ex, "empty term");
            return Uni.createFrom().nothing();
        }
        try {
            var result = sessionFactory.withSession(session ->
                    session.createNamedQuery("Pessoa.findByTermo", Pessoa.class)
                            .setParameter("termo", "%" + termo.toLowerCase(Locale.ENGLISH) + "%")
                            .setMaxResults(50)
                            .getResultList());
            return result;
        } catch (Exception e) {
           e.printStackTrace();
           log.error("term search error("+termo+"):" +e.getMessage());
           return Uni.createFrom().failure(e);
        }
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

    private void badRequest(RoutingExchange ex, String message) {
        log.warn("bad request: {}", message);
        response(ex, 400)
                .putHeader("x-badrequest-message", message)
                .end();
    }

    private void ok(RoutingExchange ex, String body) {
        response(ex, 200)
                .end(body);
    }

    private void notFound(RoutingExchange ex, String message) {
        log.warn("not found: {}", message);
        response(ex, 404)
                .putHeader("x-notfound-message", message)
                .end();
    }

    private void serverError(RoutingExchange ex, String message) {
        log.warn("server error: {}", message);
        response(ex, 500)
                .putHeader("x-servererror-message", message)
                .end();
    }

    private void unprocessable(RoutingExchange ex, String message) {
        log.warn("unprocessable: {}", message);
        response(ex, 422)
                .putHeader("x-unacceptable-message", message)
                .end();
    }

    private HttpServerResponse response(RoutingExchange ex, int status) {
        return ex.response().setStatusCode(status);
    }

}
