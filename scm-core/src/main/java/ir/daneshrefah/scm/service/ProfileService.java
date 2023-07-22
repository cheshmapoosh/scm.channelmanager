package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.Profile;
import ir.daneshrefah.scm.entity.ProfileEntity;
import ir.daneshrefah.scm.repository.ProfileRepository;
import ir.daneshrefah.scm.mapper.ProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    ProfileRepository profileRepository;

    public Profile findProfileByCode(String name) {
        ProfileEntity profileEntity = profileRepository.findByCode(name);
        return ProfileMapper.INSTANCE.toModel(profileEntity);
    }

}
