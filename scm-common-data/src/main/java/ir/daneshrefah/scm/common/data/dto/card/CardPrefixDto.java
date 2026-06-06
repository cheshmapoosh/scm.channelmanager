package ir.daneshrefah.scm.common.data.dto.card;

import java.io.Serializable;
import java.util.Objects;

public class CardPrefixDto implements Serializable {

    private Long id;


    private Long bankId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardPrefixDto cardPrefixDTO = (CardPrefixDto) o;
        if (cardPrefixDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), cardPrefixDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CardPrefixDTO{" +
                "id=" + getId() +
                ", bankId=" + getBankId() +
                "}";
    }
}
