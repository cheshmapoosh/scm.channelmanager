package ir.daneshrefah.scm.cache.rest.api;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import ir.daneshrefah.scm.cache.rest.service.HazelCastRestEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hazelcast")
@RequiredArgsConstructor
public class HazelcastRestAPI {

    //http://localhost:8080/swagger-ui/index.html#/

    private final HazelCastRestEndpoint hazelCastRestEndpoint;

    @GetMapping("/{mapName}/{key}")
    public Object get(@PathVariable("mapName") String mapName,
                      @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastRestEndpoint.getFromCache(mapName, key));
    }

    @PutMapping("/{mapName}/{key}")
    public Object put(@PathVariable("mapName") String mapName,
                    @PathVariable("key") String key,
                    @RequestBody String value) {
        hazelCastRestEndpoint.putInCache(mapName, key, value);
        return ResponseEntity.ok("DONE");
    }

    @PutMapping("/{mapName}/{key}/{lifetime}")
    public Object put(@PathVariable("mapName") String mapName,
                    @PathVariable("key") String key,
                    @PathVariable("lifetime") String lifetime,
                    @RequestBody String value) {
        hazelCastRestEndpoint.putInCache(mapName, key, value, Integer.parseInt(lifetime));
        return ResponseEntity.ok("DONE");
    }

    @DeleteMapping("/{mapName}/{key}")
    public Object remove(@PathVariable("mapName") String mapName,
                         @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastRestEndpoint.removeFromCache(mapName, key));
    }

    @GetMapping("/create/{mapName}")
    public Object create(@PathVariable("mapName") String mapName) {
        hazelCastRestEndpoint.createCacheIfNull(mapName);
        return ResponseEntity.ok(mapName);
    }

    @GetMapping("/generate-flake-id/{name}")
    public Object generateId(@PathVariable("name") String name) {
        FlakeIdGenerator idGeneratorIfNull = hazelCastRestEndpoint.createIdGeneratorIfNull(name);
        return ResponseEntity.ok(idGeneratorIfNull.newId());
    }

    @GetMapping("/maps")
    public Object mapList() {
        return ResponseEntity.ok(hazelCastRestEndpoint.getMapList());
    }

    @GetMapping("/maps/get-all/{map-name}")
    public Object mapList(@PathVariable("map-name") String mapName) {
        return ResponseEntity.ok(hazelCastRestEndpoint.getMapData(mapName));
    }
}
