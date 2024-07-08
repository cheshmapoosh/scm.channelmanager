package ir.daneshrefah.scm.process.model.task;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class HistoryTaskInfo implements BaseProcessModel {
    private Set<String> search;
}
