package org.acme;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@NamedQuery(name = "Pessoa.count", query = "SELECT count(apelido) FROM Pessoa p")
public class Pessoa {

    @Id
    @GeneratedValue
    public UUID id;

    @NotNull
    public String apelido;

    @NotNull
    public String nome;

    @PastOrPresent
    public LocalDate dataNascimento;

    /**
     * Persiste em JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @NotEmpty
    public List<String> stack;

}
