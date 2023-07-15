package ir.daneshrefah.scm.common.model;

public class ChannelEntity extends AbstractEntity{

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
