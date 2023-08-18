package org.acme;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

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
                withSession(session -> session.createNamedQuery("Pessoa.count", Long.class).getSingleResult());
    }

    @Route(methods = Route.HttpMethod.POST, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void createPessoa(RoutingContext routingContext, RoutingExchange ex) {
        JsonObject jsonObject = routingContext.body().asJsonObject();
        Map<String, Object> attrs = jsonObject.getMap();
        if (isInvalid(attrs)) {
            ex.response().setStatusCode(422).end();
        } else if (isSyntheticallyInvalid(attrs)) {
            ex.response().setStatusCode(400).end();
        } else {
            Pessoa pessoa = jsonObject.mapTo(Pessoa.class);
            sessionFactory.withStatelessSession(session -> session.insert(pessoa)).subscribe().with(
                    v -> ex.response().setStatusCode(201).putHeader("Location", "/pessoas/" + pessoa.id).end(),
                    t -> ex.response().setStatusCode(422).end()
            );
        }
    }

    @Route(methods = Route.HttpMethod.GET, path = "/pessoas/:id", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    void findPessoaById(@Param("id") Optional<String> uuidParam, RoutingExchange ex) {
        // TODO: UUID pode ser inválido
        if (uuidParam.isEmpty()) {
            ex.response().setStatusCode(400).end();
        } else {
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
        return false;
    }

    private boolean isInvalid(Map<String, Object> fields) {
        if (fields.get("apelido") == null) return true;
        if (fields.get("nome") == null) return true;
        if (fields.get("nascimento") == null) return true;
        return false;
    }

}
