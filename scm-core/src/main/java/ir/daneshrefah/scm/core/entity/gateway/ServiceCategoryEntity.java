package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SERVICE_CATEGORY", schema = "REF")
public class ServiceCategoryEntity extends AbstractEntity<Short> {
    @Id
    @NotNull
    @Column(name = "SERVICE_CATEGORY_ID", nullable = false)
    private Short id;

    @Size(max = 100)
    @Convert(disableConversion = true)
    @Column(name = "NAME", length = 100)
    private String name;

    @Size(max = 200)
    @Convert(disableConversion = true)
    @Column(name = "DESCRIPTION", length = 200)
    private String description;

}