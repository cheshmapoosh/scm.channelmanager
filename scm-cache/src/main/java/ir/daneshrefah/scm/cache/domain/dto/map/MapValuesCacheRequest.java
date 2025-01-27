package ir.daneshrefah.scm.cache.domain.dto.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapValuesCacheRequest {
    private String name;
    private String key;
}
