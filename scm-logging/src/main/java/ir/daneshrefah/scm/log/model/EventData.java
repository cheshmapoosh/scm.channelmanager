package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties
public class EventData {
   private String name;

   private long timestamp;

   private int totalAttributeCount;

   private Map<String, String> attributes;
}
