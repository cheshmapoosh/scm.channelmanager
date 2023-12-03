package ir.daneshrefah.scm.config.controller;

import ir.daneshrefah.scm.config.model.ApiResponse;
import ir.daneshrefah.scm.config.model.property.Property;
import ir.daneshrefah.scm.config.model.property.PropertyDTO;
import ir.daneshrefah.scm.config.model.property.PropertyEditDTO;
import ir.daneshrefah.scm.config.model.ResponseStatus;
import ir.daneshrefah.scm.config.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config/property")
public class PropertyController {

    private final PropertyService propertyService;
    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<PropertyDTO>>> getAllProperties() {
        List<PropertyDTO> properties = propertyService.findAllProperties();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(ResponseStatus.SUCCESS,properties));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<PropertyDTO>> getPropertyById(@PathVariable String propertyId){
        PropertyDTO property = propertyService.findPropertyById(propertyId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(ResponseStatus.SUCCESS,property));
    }

    @DeleteMapping("{propertyId}")
    public ResponseEntity<ApiResponse<Void>> deletePropertyByID(@PathVariable String propertyId){
        propertyService.deletePropertyById(propertyId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> addNewProperty(@RequestBody Property requestDTO) {
        propertyService.addNewProperty(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> editPropertyById(@PathVariable String propertyId, @RequestBody PropertyEditDTO requestDto) {
        propertyService.editProperty(propertyId,requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/{profileId}/{applicationId}")
    public ResponseEntity<ApiResponse<List<PropertyDTO>>> getPropertyByProfileAndApplication(@PathVariable String profileId, @PathVariable String applicationId) {
       List<PropertyDTO> properties = propertyService.getPropertyByProfileIdAndApplicationId(profileId,applicationId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(ResponseStatus.SUCCESS,properties));
    }
}
