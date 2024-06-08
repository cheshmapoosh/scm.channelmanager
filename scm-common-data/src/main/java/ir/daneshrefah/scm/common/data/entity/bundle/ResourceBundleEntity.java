package ir.daneshrefah.scm.common.data.entity.bundle;

import ir.daneshrefah.scm.common.data.converter.LocaleConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;


@Table(name = "TBL_SCM_RESOURCE_BUNDLE")
@Entity
@Getter
@Setter
public class ResourceBundleEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESOURCE_BUNDLE_ID")
    private Long id;
    @Column(name = "LOCALE_CODE")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;
    private String key;
    private String value;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
