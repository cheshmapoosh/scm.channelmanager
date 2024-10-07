package ir.daneshrefah.scm.common.service.provider;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceProviderFindResponse  {
    private String id;
    private String creator;
    private String lastEditor;
    private LocalDateTime createDate;
    private LocalDateTime lastEditDate;
    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderProtocol protocol;
    private AssetProvider assetProvider;
}
