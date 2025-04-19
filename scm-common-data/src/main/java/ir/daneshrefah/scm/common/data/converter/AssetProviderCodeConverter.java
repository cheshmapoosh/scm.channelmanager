package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.constant.AssetProviderCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AssetProviderCodeConverter implements AttributeConverter<AssetProviderCode,String> {
    @Override
    public String convertToDatabaseColumn(AssetProviderCode assetProviderCode) {
        return assetProviderCode.getValue();
    }

    @Override
    public AssetProviderCode convertToEntityAttribute(String s) {
        return AssetProviderCode.find(s);
    }
}
