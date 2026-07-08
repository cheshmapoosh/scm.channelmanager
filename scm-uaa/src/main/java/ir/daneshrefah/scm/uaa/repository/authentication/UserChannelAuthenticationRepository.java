package ir.daneshrefah.scm.uaa.repository.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelAuthenticationRepository extends JpaRepository<UserChannelAuthentication, Long>, JpaSpecificationExecutor<UserChannelAuthentication> {
    List<UserChannelAuthentication> findByUser_IdAndChannel_IdIsIn(Integer userId,  List<Short> channelIds);
}
