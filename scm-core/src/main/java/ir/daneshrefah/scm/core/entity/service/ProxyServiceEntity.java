package ir.daneshrefah.scm.core.entity.service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("7")
public class ProxyServiceEntity extends ServiceEntity {

}
