package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.payload.request.AgencyRequest;
import com.monetique.PinSenderV0.payload.response.AgencyDTO;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.UserAgenceDTO;

import java.util.List;

public interface Iagencyservices {



    MessageResponse createAgency(AgencyRequest agencyRequest, Long userId);
    List<AgencyDTO> listAllAgencies(Long userId);
    List<UserAgenceDTO> listAllAgenciesAssociatedUser(Long userId);
    MessageResponse deleteAgency(Long id, Long userId);
    Agency getAgencyById(Long agencyId, Long userId);

    Agency getAgencyByIdforall(Long agencyId);

    MessageResponse updateAgency(Long agencyId, AgencyRequest agencyRequest, Long userId);
}
