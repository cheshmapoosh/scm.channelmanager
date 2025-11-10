package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AtpsResponseHeader {
    private String actionCode;     // 5
    private String command;        // 2
    private String service;        // 2
    private String dateTime;       // 14
    private String referenceNo;    // 16



    @Override
    public String toString() {
        return "AtpsHeader{" +
                "actionCode='" + actionCode + '\'' +
                ", command='" + command + '\'' +
                ", service='" + service + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", referenceNo='" + referenceNo + '\'' +
                '}';
    }
}
