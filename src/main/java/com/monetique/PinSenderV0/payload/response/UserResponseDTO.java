package com.monetique.PinSenderV0.payload.response;


import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.models.Users.UserSession;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponseDTO {

        private Long id;
        private String username;
        private String email;
        private String phoneNumber;
        private List<String> roles;   // Liste des rôles sous forme de chaîne de caractères
        private Long adminId;         // Identifiant de l'admin
        private Long bankId;          // Identifiant de la banque
        private Long agencyId;        // Identifiant de l'agence
        private String sessionId;     // Identifiant de session
        private boolean active;       // Statut actif de l'utilisateur
        private TabBank bank;         // Information sur la banque (DTO)
        private Agency agency;
        private UserSession session;
}

