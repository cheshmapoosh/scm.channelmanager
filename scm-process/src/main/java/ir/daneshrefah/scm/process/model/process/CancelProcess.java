package ir.daneshrefah.scm.process.model.process;

import ir.daneshrefah.scm.process.model.Action;
import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

import java.util.List;

@Data
public class CancelProcess implements BaseProcessModel {
    private List<String> permission;
    private List<Action> actions;
}
