package com.monetique.PinSenderV0.payload.response;



import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.Role;
import lombok.Getter;
import lombok.Setter;


import java.util.Set;

@Getter
@Setter
public class UserbyidResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private boolean active;
    private Set<Role> roles; // Garde les rôles sous forme d'objets complets
    private Long adminId;
    private TabBank bank;  // Retourne l'objet complet
    private Agency agency; // Retourne l'objet complet
}
