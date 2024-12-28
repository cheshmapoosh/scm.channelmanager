//package ir.daneshrefah.scm.core.converter;
//
//import ir.daneshrefah.scm.common.model.customer.AssetType;
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//
///**
// * Description of the class or purpose of the file.
// *
// * @author reza jamshidi
// * @version 1.0
// * @since 2024-03-25
// */
//@Converter
//public class AssetTypeConverter implements AttributeConverter<AssetType, Integer> {
//
//    @Override
//    public Integer convertToDatabaseColumn(AssetType enumValue) {
//        return enumValue.getCode();
//    }
//
//    @Override
//    public AssetType convertToEntityAttribute(Integer code) {
//        return AssetType.findByCode(code);
//    }
//}