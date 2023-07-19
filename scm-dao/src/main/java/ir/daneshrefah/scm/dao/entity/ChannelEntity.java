package ir.daneshrefah.scm.dao.entity;

import java.math.BigDecimal;

public class ChannelEntity extends AbstractEntity {

    private Boolean active;
    private Boolean published;
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
