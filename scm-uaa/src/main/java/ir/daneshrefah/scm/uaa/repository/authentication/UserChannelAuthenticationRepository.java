package ir.daneshrefah.scm.uaa.repository.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelAuthenticationRepository extends JpaRepository<UserChannelAuthentication, Long>, JpaSpecificationExecutor<UserChannelAuthentication> {

    @Query("""
            select u
            from UserChannelAuthentication u
            where u.user.id = :userId
            and u.channel.id in :channelIds
            """)
    List<UserChannelAuthentication> findByUser_IdAndChannel_IdIsIn(
            @Param("userId") Integer userId,
            @Param("channelIds") List<Integer> channelIds
    );
}
