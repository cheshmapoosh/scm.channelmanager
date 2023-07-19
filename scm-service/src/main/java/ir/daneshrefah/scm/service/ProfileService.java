package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.Profile;
import ir.daneshrefah.scm.dao.entity.ProfileEntity;
import ir.daneshrefah.scm.dao.repository.ProfileRepository;
import ir.daneshrefah.scm.mapper.ProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    ProfileRepository profileRepository;

    public Profile findProfileByName(String name) {
        ProfileEntity profileEntity = profileRepository.findByName(name);
        return ProfileMapper.INSTANCE.toModel(profileEntity);
    }

}
