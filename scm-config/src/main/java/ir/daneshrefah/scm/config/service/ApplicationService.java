package ir.daneshrefah.scm.config.service;

import ir.daneshrefah.scm.config.model.application.ApplicationDTO;
import ir.daneshrefah.scm.config.model.mapper.ApplicationMapper;
import ir.daneshrefah.scm.config.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public List<ApplicationDTO> getAllApplication() {
         List<ApplicationDTO> applications = ApplicationMapper.INSTANCE.toApplicationsModel(applicationRepository.findAll());
         return applications;
    }

}
