package ir.daneshrefah.scm.common.data.entity.cardPrefix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ir.daneshrefah.scm.common.data.entity.bank.BankEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;

@Entity
@Table(name = "card_prefix")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CardPrefixEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties("cardPrefixes")
    private BankEntity bank;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankEntity getBank() {
        return bank;
    }

    public CardPrefixEntity bank(BankEntity bank) {
        this.bank = bank;
        return this;
    }

    public void setBank(BankEntity bank) {
        this.bank = bank;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CardPrefixEntity)) {
            return false;
        }
        return id != null && id.equals(((CardPrefixEntity) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CardPrefixEntity{" +
                "id=" + getId() +
                "}";
    }
}

