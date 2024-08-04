package ir.daneshrefah.scm.common.model.customer;

public interface AssetTypeCode {
    String ACCOUNT  = "1";
    String LOAN     = "2";
    String CARD     = "3";

    static Integer intValue(String assetTypeCode){
        return Integer.parseInt(assetTypeCode);
    }
}
