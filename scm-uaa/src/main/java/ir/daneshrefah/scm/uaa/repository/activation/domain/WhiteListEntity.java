package ir.daneshrefah.scm.uaa.repository.activation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "white_list",schema = "MBUAA")
@Getter
@Setter
public class WhiteListEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",sequenceName = "SEQUENCE_GENERATOR")
    private Long id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

}
