package ir.daneshrefah.scm.common.data.dto.bank;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class BankDto implements Serializable {

    private Long id;

    @NotNull
    private String bic;

    @NotNull
    private Boolean isHomeBank;

    @NotNull
    private String name;

    private String imageURL;

    private Boolean bicDisabled;

    public Boolean isBicDisabled() {
        return bicDisabled;
    }

    public void setBicDisabled(Boolean bicDisabled) {
        this.bicDisabled = bicDisabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public Boolean isIs_home_bank() {
        return isHomeBank;
    }

    public void setIs_home_bank(Boolean isHomeBank) {
        this.isHomeBank = isHomeBank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankDto bankDTO = (BankDto) o;
        if (bankDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bankDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "BankDTO{" +
                "id=" + getId() +
                ", bic='" + getBic() + "'" +
                ", is_home_bank='" + isIs_home_bank() + "'" +
                ", name='" + getName() + "'" +
                ", imageURL='" + getImageURL() + "'" +
                "}";
    }
}

