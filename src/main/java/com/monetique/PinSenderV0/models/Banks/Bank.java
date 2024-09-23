
/*
package com.monetique.PinSenderV0.models.Banks;



import com.monetique.PinSenderV0.models.Users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "banks")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;
    @NotBlank
    private String contactEmail;

    @NotBlank
    private String contactPhoneNumber;

    private String address;
    @Column(name = "bank_code", unique = true, nullable = false)
    private String bankCode;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = true)
    private User admin;  // The admin of the bank (an Admin user)

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Agency> agencies = new HashSet<>();

    public Bank(String name) {
        this.name = name;
    }
}
*/