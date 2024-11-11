package ir.daneshrefah.scm.common.data.service.bundle;

import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.constant.BundleParameterPattern;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
/**
 * @see BundleDefaults ExceptionBundleGrammer
 */
public interface ResourceBundleService {
    Optional<String> get(Locale locale, String key);
    Optional<String> get(Locale locale, String key, BundleParameterPattern parameterPattern,Object...parameters);
    Optional<String> get(String key,BundleParameterPattern parameterPattern,Object...parameters);
    Optional<String> get(String key);
    boolean contains(Locale locale,String key);
    boolean contains(String key);
    void update(ResourceBundle resourceBundle);
    ResourceBundle save(ResourceBundle resourceBundle);
    List<ResourceBundle> getAll();
}
