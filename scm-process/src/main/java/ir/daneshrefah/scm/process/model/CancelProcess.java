package ir.daneshrefah.scm.process.model;

import lombok.Data;

import java.util.List;

@Data
public class CancelProcess {
    private List<String> permission;
    private List<Action> actions;

}
