package ir.daneshrefah.scm.config.service;

import ir.daneshrefah.scm.config.model.profile.ProfileDTO;
import ir.daneshrefah.scm.config.model.mapper.ProfileMapper;
import ir.daneshrefah.scm.config.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<ProfileDTO> getAllProfiles() {
        return ProfileMapper.INSTANCE.toProfilesDTO(profileRepository.findAll());
    }
}
