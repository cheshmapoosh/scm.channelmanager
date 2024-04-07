package ir.daneshrefah.scm.plugin.pichack.provider.dto.common;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public class Benefactor implements Serializable {

    private String shahabId;
    private String idCode;
    private PersonIdType idType;

    public String getShahabId() {
        return shahabId;
    }

    public void setShahabId(String shahabId) {
        this.shahabId = shahabId;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public PersonIdType getIdType() {
        return idType;
    }

    public void setIdType(PersonIdType idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        return "Benefactor{" +
                "shahabId='" + shahabId + '\'' +
                ", idCode='" + idCode + '\'' +
                ", idType=" + idType +
                '}';

//        return "Benefactor{" +
//                "shahabId='" + shahabId == null ? "" : shahabId + '\'' +
//                ", idCode='" + idCode == null ? "" : idCode + '\'' +
//                ", idType=" + idType == null ? "" : idType.getCode() +
//                '}';
    }
}
