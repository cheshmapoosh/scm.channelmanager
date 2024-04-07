package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.constant.ConstantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Repository
public interface ConstantRepository extends JpaRepository<ConstantEntity, Long> {

}
