package ir.daneshrefah.scm.cache.rest.api;

import com.hazelcast.flakeidgen.FlakeIdGenerator;
import ir.daneshrefah.scm.cache.service.HazelCastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hazelcast")
@RequiredArgsConstructor
public class HazelcastRestAPI {

    //http://localhost:8080/swagger-ui/index.html#/

    private final HazelCastService hazelCastService;

    @GetMapping("/{mapName}/{key}")
    public ResponseEntity get(@PathVariable("mapName") String mapName,
                      @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.getFromCache(mapName, key));
    }

    @PutMapping("/{mapName}/{key}")
    public ResponseEntity put(@PathVariable("mapName") String mapName,
                    @PathVariable("key") String key,
                    @RequestBody String value) {
        hazelCastService.putInCache(mapName, key, value);
        return ResponseEntity.ok("DONE");
    }

    @PutMapping("/{mapName}/{key}/{lifetime}")
    public ResponseEntity put(@PathVariable("mapName") String mapName,
                    @PathVariable("key") String key,
                    @PathVariable("lifetime") String lifetime,
                    @RequestBody String value) {
        hazelCastService.putInCache(mapName, key, value, Integer.parseInt(lifetime));
        return ResponseEntity.ok("DONE");
    }

    @DeleteMapping("/{mapName}/{key}")
    public ResponseEntity remove(@PathVariable("mapName") String mapName,
                         @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.removeFromCache(mapName, key));
    }

    @GetMapping("/create/{mapName}")
    public ResponseEntity create(@PathVariable("mapName") String mapName) {
        hazelCastService.createCacheIfNull(mapName);
        return ResponseEntity.ok(mapName);
    }

    @GetMapping("/generate-flake-id/{name}")
    public ResponseEntity generateId(@PathVariable("name") String name) {
        FlakeIdGenerator idGeneratorIfNull = hazelCastService.createIdGeneratorIfNull(name);
        return ResponseEntity.ok(idGeneratorIfNull.newId());
    }

    @GetMapping("/maps")
    public ResponseEntity mapList() {
        return ResponseEntity.ok(hazelCastService.getMapList());
    }

    @GetMapping("/maps/get-all/{map-name}")
    public ResponseEntity mapList(@PathVariable("map-name") String mapName) {
        return ResponseEntity.ok(hazelCastService.getMapData(mapName));
    }
}
