package ir.daneshrefah.scm.common.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.common.type.PeriodType;


public class Condition extends BaseModel<String> {

    private String id;
    private String title;
    private String desc;
    private ConditionType type;
    private Long value;
    private PeriodType periodType;
    private Integer periodValue;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Integer getPeriodValue() {
        return periodValue;
    }

    public void setPeriodValue(Integer periodValue) {
        this.periodValue = periodValue;
    }
}
