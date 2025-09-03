//package ir.daneshrefah.scm.common.data.entity.asset;
//
//import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import jakarta.validation.constraints.NotNull;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "EB_SERVICE", schema = "REF")
//@Deprecated
//public class EbServiceEntity extends AbstractEntity<Integer> {
//
//    @Id
//    @Column(name = "EB_SERVICE_ID", nullable = false)
//    private Integer id;
//
//    @NotNull
//    @Column(name = "PUBLISH", nullable = false)
//    private Boolean publish;
//
//    @Column(name = "NAME", length = 100)
//    private String name;
//
//    @Column(name = "CODE", length = 50)
//    private String code;
//
//    @Column(name = "ABBREVIATION", length = 3)
//    private String abbreviation;
//        //TODO PRODUCTION DB DOES NOT HAVE PRIVILEGE
////    @Column(name = "SERVICE_CATEGORY_ID")
////    private Integer serviceCategoryId;
//
//}