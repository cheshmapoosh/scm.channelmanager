package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.service.user.UserFindRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
public class UserSpecs {

    public static Specification<UserEntity> toSpecification(UserFindRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != request.getActive()) {
                predicates.add(builder.equal(root.get("active"), request.getActive()));
            }
            if (StringUtils.isNotEmpty(request.getCreatorBranch())) {
                predicates.add(builder.equal(root.get("creatorBranch"), request.getCreatorBranch()));
            }
            if (StringUtils.isNotEmpty(request.getTerminalCode())) {
                predicates.add(builder.equal(root.get("terminalId"), request.getTerminalId()));
            }
            if (StringUtils.isNotEmpty(request.getNickname())) {
                predicates.add(builder.like(root.get("nickname"), getLikeQuery(request.getNickname())));
            }
            if (StringUtils.isNotEmpty(request.getAccessParameters())) {
                predicates.add(builder.like(root.get("accessParameters"), getLikeQuery(request.getAccessParameters())));
            }
            if (StringUtils.isNotEmpty(request.getCreator())) {
                predicates.add(builder.equal(root.get("creator"), request.getCreator()));
            }
            if (StringUtils.isNotEmpty(request.getEditor())) {
                predicates.add(builder.equal(root.get("lastEditor"), request.getEditor()));
            }
            if (Objects.nonNull(request.getLoginAuthenticationMethod())){
                predicates.add(builder.equal(root.get("loginAuthenticationMethod"), request.getLoginAuthenticationMethod().getDbRef()));
            }
            if (Objects.nonNull(request.getTransactionAuthenticationMethod())){
                predicates.add(builder.equal(root.get("transactionAuthenticationMethod"), request.getTransactionAuthenticationMethod().getDbRef()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
    private static String getLikeQuery(String input){
        return "%"+input+"%";
    }

}
