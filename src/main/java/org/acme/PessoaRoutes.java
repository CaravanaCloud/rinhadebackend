package org.acme;

import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
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
    Uni<Pessoa> createPessoa(@Body Pessoa pessoa, RoutingExchange ex) {
        return sessionFactory.withStatelessSession(session -> session.insert(pessoa).replaceWith(pessoa));
    }

    @Route(methods = Route.HttpMethod.GET, path = "/pessoas/:id", produces = ReactiveRoutes.APPLICATION_JSON, order = 2)
    Uni<Pessoa> findPessoaById(@Param("id") Optional<String> uuidParam, RoutingExchange ex) {
        // TODO: UUID pode ser inválido
        UUID uuid = UUID.fromString(uuidParam.get());
        return sessionFactory.withSession(session -> session.find(Pessoa.class, uuid));
    }


    @Route(methods = Route.HttpMethod.GET, path = "/pessoas", produces = ReactiveRoutes.APPLICATION_JSON, order = 3)
    Uni<List<Pessoa>> findPessoa(@Param("t") Optional<String> termo, RoutingExchange ex) {
        if (termo.isEmpty()) {
            ex.notFound().end();
        }
        return sessionFactory.withSession(session ->
                session.createQuery("select p from Pessoa p", Pessoa.class)
                        .setMaxResults(50).getResultList());
    }

    @Route(type = Route.HandlerType.FAILURE, order = 1)
    void validationException(ConstraintViolationException e, HttpServerResponse response) {
        response.setStatusCode(422).end(e.getMessage());
    }
}
