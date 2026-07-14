package ir.daneshrefah.scm.common.data.entity.bank;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Entity
@Table(name = "BANK")
@Accessors(chain = true)
public class BankEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BANK_ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "IIN", nullable = false)
    private String iin;

    @Column(name = "IMAGE_URL", nullable = false)
    private String imageUrl;
}
