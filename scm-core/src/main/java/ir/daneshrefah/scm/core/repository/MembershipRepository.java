package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.core.entity.asset.AccountMembershipEntity;
import ir.daneshrefah.scm.core.entity.asset.MembershipEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity,Long> {

    @Query("SELECT o FROM MembershipEntity o where o.assetType = :assetType and o.person.username = :username")
     List<MembershipEntity> findAllByAssetTypeAndPersonUsername(@Param("assetType")AssetType assetType,@Param("username") String username);

    @Query("SELECT o FROM MembershipEntity o where o.person.username = :username")
    List<MembershipEntity> findAllByPersonUsername(@Param("username") String username);

    @Query("SELECT o FROM MembershipEntity o where o.customerAccount.account.accountNo = :accountNo and o.person.username = :username")
    Optional<AccountMembershipEntity> findAccountMembershipByAccountNoAndUsername(@Param("accountNo") String accountNo,@Param("username") String username);

    @Query("SELECT O FROM AccountMembershipEntity O WHERE O.id = :membershipId")
    Optional<AccountMembershipEntity> findAccountMembershipById(@Param("membershipId") Long membershipId);

    interface MembershipSpecs {

        static Specification<MembershipEntity> toSpecification(String username, AssetType assetType) {
            return (root, query, builder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (Objects.nonNull(username)) {
                    predicates.add(builder.equal(root.get("person").get("username"), username));
                }
                if (Objects.nonNull(assetType)) {
                    predicates.add(builder.equal(root.get("assetType"), assetType));
                }
                return builder.and(predicates.toArray(new Predicate[0]));
            };
        }

    }

}
