package ru.t1.accountservice.core.entity.jwtversion;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.accountservice.core.entity.user.User;

@Entity
@Table(name = "version_jwt_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long version;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}