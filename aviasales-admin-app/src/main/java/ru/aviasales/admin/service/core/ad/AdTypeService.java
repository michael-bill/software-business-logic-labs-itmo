package ru.aviasales.admin.service.core.ad;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.repository.AdTypeRepository;
import ru.aviasales.common.dto.response.AdTypeResp;

@Service
@RequiredArgsConstructor
public class AdTypeService {

    private final AdTypeRepository adTypeRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<AdTypeResp> getAllAdTypes(Pageable pageable) {
        return adTypeRepository.findAll(pageable).map(x -> modelMapper.map(x, AdTypeResp.class));
    }
}
