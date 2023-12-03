package ir.daneshrefah.scm.config.controller;

import ir.daneshrefah.scm.config.model.ApiResponse;
import ir.daneshrefah.scm.config.model.ResponseStatus;
import ir.daneshrefah.scm.config.model.profile.ProfileDTO;
import ir.daneshrefah.scm.config.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<ProfileDTO>>> getAllProfiles() {
        List<ProfileDTO> profiles = profileService.getAllProfiles();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(ResponseStatus.SUCCESS,profiles));
    }
}
