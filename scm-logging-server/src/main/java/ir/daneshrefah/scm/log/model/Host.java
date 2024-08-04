package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Host{
    private Os os;
    private String name;
    private String id;
    private List<String> ip=new ArrayList<>();
    private List<String> mac=new ArrayList<>();
    private String architecture;
    private boolean containerized;
    private String hostname;
}