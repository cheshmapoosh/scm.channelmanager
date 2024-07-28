package ir.daneshrefah.scm.config.server.service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class YamlService {

    public Map<String, Object> touchAndLoadYaml(Path path) throws IOException {
        File file = path.toFile();
        FileUtils.touch(file);
        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml(new Constructor(Map.class, new LoaderOptions()));
            Map<String, Object> load = yaml.load(fis);
            return Objects.nonNull(load) ? load : new HashMap<>();
        }
    }

    public void saveYaml(Path path, Map<String, Object> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new Representer(new DumperOptions()), options);
        try (FileWriter writer = new FileWriter(path.toFile())) {
            yaml.dump(data, writer);
        }
    }

    public void updateProperty(Path path, String key, Object value) throws IOException {
        Map<String, Object> yamlData = touchAndLoadYaml(path);
        setProperty(yamlData, key, value);
        saveYaml(path, yamlData);
    }

    @SuppressWarnings("unchecked")
    private void setProperty(Map<String, Object> yamlData, String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = yamlData;

        for (int i = 0; i < keys.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.computeIfAbsent(keys[i], k -> new LinkedHashMap<>());
        }

        currentMap.put(keys[keys.length - 1], value);
    }
}
