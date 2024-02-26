package ir.daneshrefah.scm.cache.rest.api;

import com.hazelcast.flakeidgen.FlakeIdGenerator;
import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.cache.service.HazelCastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hazelcast")
@RequiredArgsConstructor
public class HazelcastRestAPI {

    //http://localhost:8080/swagger-ui/index.html#/

    private final HazelCastService hazelCastService;

    @GetMapping("/{mapName}/{key}")
    public ResponseEntity<Object> get(@PathVariable("mapName") String mapName,
                                      @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.getFromCache(mapName, key));
    }

    @PutMapping("/session")
    public ResponseEntity<Object> putSession(@RequestBody UserAuthenticationTO userAuthentication) {
        return ResponseEntity.ok(hazelCastService.putSession(userAuthentication));
    }

    @GetMapping("/session/{user-nickname}/{terminal-code}")
    public ResponseEntity<Object> getSession(@PathVariable("user-nickname") String nickname,
                                             @PathVariable("terminal-code") String terminalCode) {
        return ResponseEntity.ok(hazelCastService.getSession(nickname,terminalCode));
    }

    @GetMapping("/{mapName}/{key}/entry-view")
    public ResponseEntity<Object> getEntryView(@PathVariable("mapName") String mapName,
                                               @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.getEntryView(mapName, key));
    }

    @GetMapping("/{mapName}/{key}/exists")
    public ResponseEntity<Object> exists(@PathVariable("mapName") String mapName,
                                         @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.exists(mapName, key));
    }

    @PutMapping(value = "/{mapName}/{key}")
    public ResponseEntity<Object> put(@PathVariable("mapName") String mapName,
                                      @PathVariable("key") String key,
                                      @RequestBody String value) {
        return ResponseEntity.ok(hazelCastService.putInCache(mapName, key, value));
    }

    @PutMapping("/{mapName}/{key}/{ttl}")
    public ResponseEntity<Object> put(@PathVariable("mapName") String mapName,
                                      @PathVariable("key") String key,
                                      @PathVariable("ttl") String lifetime,
                                      @RequestBody String value) {
        return ResponseEntity.ok(hazelCastService.putInCache(mapName, key, value, Integer.parseInt(lifetime)));
    }

    @PutMapping("/{mapName}/{key}/{ttl}/{max-idle}")
    public ResponseEntity<Object> put(@PathVariable("mapName") String mapName,
                                      @PathVariable("key") String key,
                                      @PathVariable("ttl") String lifetime,
                                      @PathVariable("max-idle") String maxIdle,
                                      @RequestBody String value) {
        return ResponseEntity.ok(hazelCastService.putInCache(mapName, key, value, Integer.parseInt(lifetime), Integer.parseInt(maxIdle)));
    }

    @DeleteMapping("/{mapName}/{key}")
    public ResponseEntity<Object> remove(@PathVariable("mapName") String mapName,
                                         @PathVariable("key") String key) {
        return ResponseEntity.ok(hazelCastService.removeFromCache(mapName, key));
    }

    @DeleteMapping("/session/{user-nickname}/{terminal-code}")
    public ResponseEntity<Object> removeSession(@PathVariable("user-nickname") String userNickname,
                                                @PathVariable("terminal-code") String terminalCode) {
        return ResponseEntity.ok(hazelCastService.removeSession(userNickname, terminalCode));
    }

    @GetMapping("/create/{mapName}")
    public ResponseEntity<String> create(@PathVariable("mapName") String mapName) {
        hazelCastService.createCacheIfNull(mapName);
        return ResponseEntity.ok(mapName);
    }

    @GetMapping("/generate-flake-id/{name}")
    public ResponseEntity<Long> generateId(@PathVariable("name") String name) {
        FlakeIdGenerator idGeneratorIfNull = hazelCastService.createIdGeneratorIfNull(name);
        return ResponseEntity.ok(idGeneratorIfNull.newId());
    }

    @GetMapping("/maps")
    public ResponseEntity<List<String>> mapList() {
        return ResponseEntity.ok(hazelCastService.getMapList());
    }

    @GetMapping("/maps/get-all/{map-name}")
    public ResponseEntity<Object> mapList(@PathVariable("map-name") String mapName) {
        return ResponseEntity.ok(hazelCastService.getMapData(mapName));
    }

}
