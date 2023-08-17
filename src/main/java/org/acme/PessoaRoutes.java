package org.acme;

import io.quarkus.vertx.web.Route;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class PessoaRoutes {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Route(methods = Route.HttpMethod.GET, path = "/contagem-pessoas")
    Uni<Long> contagemPessoas() {
        return sessionFactory.
                withSession(session -> session.createNamedQuery("Pessoa.count", Long.class).getSingleResult());
    }
}
