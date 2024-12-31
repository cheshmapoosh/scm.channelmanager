package ir.daneshrefah.scm.cache.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapValuesCacheRequest {
    private String mapName;
    private String key;
}
