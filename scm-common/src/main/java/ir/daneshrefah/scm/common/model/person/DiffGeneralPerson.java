package ir.daneshrefah.scm.common.model.person;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class DiffGeneralPerson {
    private boolean syncAll;
    private List<Diff> diffs;

    @Getter
    @Setter
    public static class Diff {
        private String title;
        private Object current;
        private Object update;
        private boolean sync;
    }

}
