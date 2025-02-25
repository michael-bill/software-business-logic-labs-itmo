package ru.aviasales.admin.service.core.commissions;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aviasales.admin.dao.repository.UserSegmentRepository;
import ru.aviasales.admin.dto.response.UserSegmentResp;

@Service
@RequiredArgsConstructor
public class UserSegmentService {

    private final UserSegmentRepository userSegmentRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public Page<UserSegmentResp> getAllUserSegments(Pageable pageable) {
        return userSegmentRepository.findAll(pageable).map(x -> modelMapper.map(x, UserSegmentResp.class));
    }
}
