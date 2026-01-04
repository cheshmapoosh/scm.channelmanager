package ir.daneshrefah.scm.uaa.repository.activation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "LOGIN_MESSAGE",schema = "MBUAA")
public class LoginMessageEntity implements Serializable {

    @Id
    private Long id;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "client_id")
    private Long client;

    @Column(name = "application_code", nullable = false)
    private String application;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "ADVERTISEMENT_MESSAGE_URL")
    private String url;
    @Column(name = "ADVERTISEMENT_MESSAGE_TEXT")
    private String text;
    @Column(name = "ADVERTISEMENT_MESSAGE_ICON")
    private String icon;

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        LoginMessageEntity that = (LoginMessageEntity) object;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
