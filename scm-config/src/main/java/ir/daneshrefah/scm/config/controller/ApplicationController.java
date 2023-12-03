package ir.daneshrefah.scm.config.controller;

import ir.daneshrefah.scm.config.model.ApiResponse;
import ir.daneshrefah.scm.config.model.application.ApplicationDTO;
import ir.daneshrefah.scm.config.model.ResponseStatus;
import ir.daneshrefah.scm.config.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/config/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getAllApplications() {
        List<ApplicationDTO> applications = applicationService.getAllApplication();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(ResponseStatus.SUCCESS,applications));
    }
}
