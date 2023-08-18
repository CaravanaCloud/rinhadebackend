package org.acme;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@NamedQueries({
        @NamedQuery(name = "Pessoa.count", query = "SELECT count(p.id) FROM Pessoa p")
})
public class Pessoa {

    @Id
    @GeneratedValue
    public UUID id;

    //    @NotNull
    @Column(unique = true)
    public String apelido;

    //    @NotNull
    public String nome;

    //    @PastOrPresent
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate nascimento;

    /**
     * Persiste em JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
//    @NotEmpty
    public List<String> stack;


    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", apelido='" + apelido + '\'' +
                ", nome='" + nome + '\'' +
                ", nascimento=" + nascimento +
                ", stack=" + stack +
                '}';
    }
}
