package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.payload.request.AgencyRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;

import java.util.List;

public interface Iagencyservices {



    MessageResponse createAgency(AgencyRequest agencyRequest, Long userId);

    List<Agency> listAllAgencies(Long userId);
    MessageResponse deleteAgency(Long id, Long userId);
    Agency getAgencyById(Long agencyId, Long userId);

    MessageResponse updateAgency(Long agencyId, AgencyRequest agencyRequest, Long userId);
}
