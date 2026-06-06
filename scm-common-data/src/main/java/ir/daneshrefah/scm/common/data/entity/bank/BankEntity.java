package ir.daneshrefah.scm.common.data.entity.bank;

import ir.daneshrefah.scm.common.data.entity.cardPrefix.CardPrefixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bank")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BankEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "bic", nullable = false)
    private String bic;

    @NotNull
    @Column(name = "is_home_bank", nullable = false)
    private Boolean isHomeBank;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageURL;

    @Column(name = "is_bic_disabled")
    private Boolean bicDisabled;


    @OneToMany(mappedBy = "bank")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<CardPrefixEntity> cardPrefixes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBic() {
        return bic;
    }

    public BankEntity bic(String bic) {
        this.bic = bic;
        return this;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public Boolean isIs_home_bank() {
        return isHomeBank;
    }

    public BankEntity is_home_bank(Boolean is_home_bank) {
        this.isHomeBank = is_home_bank;
        return this;
    }

    public Boolean isBicDisabled() {
        return bicDisabled;
    }

    public void setBicDisabled(Boolean bicDisabled) {
        this.bicDisabled = bicDisabled;
    }

    public void setIs_home_bank(Boolean isHomeBank) {
        this.isHomeBank = isHomeBank;
    }

    public String getName() {
        return name;
    }

    public BankEntity name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public BankEntity imageURL(String imageURL) {
        this.imageURL = imageURL;
        return this;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Set<CardPrefixEntity> getCardPrefixes() {
        return cardPrefixes;
    }

    public BankEntity cardPrefixes(Set<CardPrefixEntity> cardPrefixes) {
        this.cardPrefixes = cardPrefixes;
        return this;
    }

    public BankEntity addCardPrefix(CardPrefixEntity cardPrefix) {
        this.cardPrefixes.add(cardPrefix);
        cardPrefix.setBank(this);
        return this;
    }

    public BankEntity removeCardPrefix(CardPrefixEntity cardPrefix) {
        this.cardPrefixes.remove(cardPrefix);
        cardPrefix.setBank(null);
        return this;
    }

    public void setCardPrefixes(Set<CardPrefixEntity> cardPrefixes) {
        this.cardPrefixes = cardPrefixes;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BankEntity)) {
            return false;
        }
        return id != null && id.equals(((BankEntity) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id=" + getId() +
                ", bic='" + getBic() + "'" +
                ", is_home_bank='" + isIs_home_bank() + "'" +
                ", name='" + getName() + "'" +
                ", imageURL='" + getImageURL() + "'" +
                "}";
    }
}
