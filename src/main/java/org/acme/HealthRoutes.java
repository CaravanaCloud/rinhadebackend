package org.acme;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.vertx.web.ReactiveRoutes;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HealthRoutes {
    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Route(methods = Route.HttpMethod.GET, path = "/_hc", produces = ReactiveRoutes.APPLICATION_JSON, order = 3)
    Map<String,String> getHealth(RoutingExchange ex) {
        var healthMap = new HashMap<String, String>();
        healthMap.put("datasource.valid",  isDatabaseHealthy().toString());
        return healthMap;
    }

    private Boolean isDatabaseHealthy() {
        try {
            var result = sessionFactory.
                withSession(session -> session.createNativeQuery("select 1+1")
                .getSingleResult());
            result.subscribe().with( r -> System.out.println("1+1="+r.toString()));
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
