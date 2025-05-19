package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipTerminalAccessRepository;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.repository.activation.NibNativeRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NibUserActivationServiceImpl implements UserActivationService {

    private static final Logger log = LoggerFactory.getLogger(NibUserActivationServiceImpl.class);

    private final MembershipTerminalAccessRepository membershipTerminalAccessRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final TerminalRepository terminalRepository;
    private final NibNativeRepository nibNativeRepository;

    @Transactional
    @Override
    public void activate(GeneralPerson person, TerminalType fromTerminal) {
        validateInput(person, fromTerminal);
        GeneralPersonEntity personEntity = findPersonEntity(person.getUsername());
        duplicateUserChannelAuthentication(fromTerminal, personEntity);

        // TODO: Implement membership and membershipTerminalAccess logic


        Map<Integer, Integer> nibChannelMap = nibNativeRepository.findNibChannel();
       duplicateMembershipChannelAccess(personEntity.getId(), nibChannelMap,fromTerminal);


        log.info("User activation completed for username: {}", person.getUsername());
    }

    /**
     * Duplicates all MEMBERSHIP_CHANNEL_ACCESS records for a user, updates CHANNEL_ID based on a provided mapping,
     * and saves them with new MEMBERSHIP_CHANNEL_ACCESS_IDs.
     *
     * @param userId         The ID of the user to duplicate records for.
     * @param channelIdMap   A Map mapping old CHANNEL_IDs to new CHANNEL_IDs.
     * @throws IllegalStateException if duplication or insertion fails.
     */
    private void duplicateMembershipChannelAccess(Integer userId, Map<Integer, Integer> channelIdMap,TerminalType fromTerminal) {
        log.debug("Starting duplication of MEMBERSHIP_CHANNEL_ACCESS for userId: {}", userId);

        // Fetch existing records
        List<Map<String, Object>> membershipChannelAccess = nibNativeRepository.findMembershipChannelAccessByUserId(userId,fromTerminal);
        if (membershipChannelAccess.isEmpty()) {
            log.warn("No MEMBERSHIP_CHANNEL_ACCESS records found to duplicate for userId: {}", userId);
            return;
        }

        try {
                Integer membershipId = null;
            for (Map<String, Object> original : membershipChannelAccess) {
                // Create a new record by copying the original



                if (membershipId != null && membershipId.equals(original.get("MEMBERSHIP_ID"))) {
                    log.warn("Skipping duplicate record for userId: {} with MEMBERSHIP_ID: {}", userId, membershipId);
//                    Long membershipChannelAccessId = (Long) original.get("MEMBERSHIP_CHANNEL_ACCESS_ID");
                    continue;
                }
                membershipId = (Integer) original.get("MEMBERSHIP_ID");

                Map<String, Object> newRecord = new HashMap<>(original);

                // Get the old CHANNEL_ID
                Integer oldAuthenticationMethodId = (Integer) original.get(NibNativeRepository.AUTHENTICATION_METHOD_ID_NAME);
                if (oldAuthenticationMethodId == null) {
                    log.warn("Skipping record with null AUTHENTICATION_METHOD_ID for userId: {}", userId);
                    continue;
                }

                // Map to new CHANNEL_ID
                Integer newChannelId = channelIdMap.get(oldAuthenticationMethodId);
                if (newChannelId == null) {
                    log.warn("No mapping found for CHANNEL_ID {} for userId: {}, skipping", oldAuthenticationMethodId, userId);
                    continue;
                }

                // Update CHANNEL_ID
                newRecord.put("CHANNEL_ID", newChannelId);

                // Generate new MEMBERSHIP_CHANNEL_ACCESS_ID
//                Long maxId = nibNativeRepository.findMaxId("MEMBERSHIP_CHANNEL_ACCESS_ID", "ref.MEMBERSHIP_CHANNEL_ACCESS");
//                newRecord.put("MEMBERSHIP_CHANNEL_ACCESS_ID", maxId + 1);

                // Insert the new record
                Integer membershipChannelAccessId = nibNativeRepository.insertMembershipChannelAccess(newRecord);
                log.info("Successfully duplicated MEMBERSHIP_CHANNEL_ACCESS record for userId: {} with new CHANNEL_ID: {} and ID: {}",
                        userId, newChannelId, newRecord.get("MEMBERSHIP_CHANNEL_ACCESS_ID"));

                // Duplicate related MEMBERSHIP_CHANNEL_SERVICE_ACCESS records
                duplicateMembershipChannelServiceAccess(
                        (Integer) original.get("MEMBERSHIP_CHANNEL_ACCESS_ID"),
                        membershipChannelAccessId ,
                        (Integer) original.get("CHANNEL_ID"),
                        newChannelId
                );
            }
        } catch (Exception e) {
            log.error("Failed to duplicate MEMBERSHIP_CHANNEL_ACCESS for userId: {}: {}", userId, e.getMessage());
            throw new IllegalStateException("Failed to duplicate MEMBERSHIP_CHANNEL_ACCESS for userId: " + userId, e);
        }
    }

    /**
     * Duplicates MEMBERSHIP_CHANNEL_SERVICE_ACCESS records for a given MCS_ID, updating to a new MCS_ID
     * and mapping CHANNEL_EB_ACCESS_ID based on old and new CHANNEL_IDs.
     *
     * @param oldMcsId       The original MEMBERSHIP_CHANNEL_ACCESS_ID.
     * @param newMcsId       The new MEMBERSHIP_CHANNEL_ACCESS_ID.
     * @param oldChannelId   The original CHANNEL_ID.
     * @param newChannelId   The new CHANNEL_ID.
     */
    private void duplicateMembershipChannelServiceAccess(Integer oldMcsId, Integer newMcsId,
                                                         Integer oldChannelId, Integer newChannelId) {
        log.debug("Duplicating MEMBERSHIP_CHANNEL_SERVICE_ACCESS for oldMcsId: {} to newMcsId: {}", oldMcsId, newMcsId);

        List<Map<String, Object>> mcsasRecords = nibNativeRepository.getMcsasRecords(oldMcsId);
        if (mcsasRecords == null) return;

        if (mcsasRecords.isEmpty()) {
            log.warn("No MEMBERSHIP_CHANNEL_SERVICE_ACCESS records found for MCS_ID: {}", oldMcsId);
            return;
        }

        for (Map<String, Object> originalMcsas : mcsasRecords) {
            // Create a new record by copying the original
            Map<String, Object> newMcsasRecord = new HashMap<>(originalMcsas);

            // Update MCS_ID
            newMcsasRecord.put("MCS_ID", newMcsId);

            // Map CHANNEL_EB_ACCESS_ID
            BigDecimal oldChannelEbAccessId = (BigDecimal) originalMcsas.get("CHANNEL_EB_ACCESS_ID");
            BigDecimal newChannelEbAccessId = nibNativeRepository.findNewChannelEbAccessId(oldChannelEbAccessId, oldChannelId, newChannelId);
            if (newChannelEbAccessId == null) {
                log.warn("No CHANNEL_EB_ACCESS_ID found for new CHANNEL_ID {} for MCS_ID {}, skipping",
                        newChannelId, oldMcsId);
                continue;
            }
            newMcsasRecord.put("CHANNEL_EB_ACCESS_ID", newChannelEbAccessId);

            // Generate new MCSAS_ID
//            Long maxMcsasId = nibNativeRepository.findMaxId("MCSAS_ID", "ref.MEMBERSHIP_CHANNEL_SERVICE_ACCESS");
//            newMcsasRecord.put("MCSAS_ID", maxMcsasId + 1);

            // Insert the new MEMBERSHIP_CHANNEL_SERVICE_ACCESS record
            nibNativeRepository.insertMembershipChannelServiceAccess(newMcsasRecord);
            log.info("Successfully duplicated MEMBERSHIP_CHANNEL_SERVICE_ACCESS record for new MCS_ID: {} with CHANNEL_EB_ACCESS_ID: {}",
                    newMcsId, newChannelEbAccessId);
        }
    }



    /**
     * Duplicates a USER_CHANNEL_AUTHENTICATION record for a given user and terminal, updating it with the parent NIB channel ID.
     *
     * @param fromTerminal The terminal code to fetch the original authentication record from.
     * @param personEntity The person entity containing the user ID.
     */
    private void duplicateUserChannelAuthentication(TerminalType fromTerminal, GeneralPersonEntity personEntity) {
        log.debug("Starting duplication of USER_CHANNEL_AUTHENTICATION for userId: {} from terminal: {}",
                personEntity.getId(), fromTerminal);

        try {
            Map<String, Object> userChannelAuth = fetchUserChannelAuthentication(personEntity.getId(), fromTerminal);
            log.info("Fetched USER_CHANNEL_AUTHENTICATION for userId: {} with CODE: {}",
                    personEntity.getId(), userChannelAuth.get("CODE"));

            Integer parentNibChannelId = getParentNibChannelId();
            log.debug("Retrieved parent NIB channel ID: {}", parentNibChannelId);

            userChannelAuth.put("CHANNEL_ID", parentNibChannelId);
            log.debug("Updated USER_CHANNEL_AUTHENTICATION with new CHANNEL_ID: {}", parentNibChannelId);

            insertUpdatedUserChannelAuthentication(userChannelAuth);
            log.info("Successfully duplicated USER_CHANNEL_AUTHENTICATION for userId: {} with new ID: {}",
                    personEntity.getId(), userChannelAuth.get("USER_CHANNEL_AUTHENTICATION_ID"));

        } catch (IllegalStateException e) {
            log.error("Failed to duplicate USER_CHANNEL_AUTHENTICATION for userId: {} from terminal: {}. Error: {}",
                    personEntity.getId(), fromTerminal, e.getMessage());
            throw e; // Re-throw to let the caller handle it
        } catch (Exception e) {
            log.error("Unexpected error while duplicating USER_CHANNEL_AUTHENTICATION for userId: {} from terminal: {}",
                    personEntity.getId(), fromTerminal, e);
            throw new IllegalStateException("Unexpected error during user channel authentication duplication", e);
        }
    }

    private void validateInput(GeneralPerson person, TerminalType fromTerminal) {
        if (person == null || person.getUsername() == null || fromTerminal == null) {
            throw new IllegalArgumentException("Person and fromTerminal must not be null");
        }
    }

    private GeneralPersonEntity findPersonEntity(String username) {
        List<GeneralPersonEntity> entities = personRepository.findPersonByUsername(username);
        if (entities.isEmpty()) {
            log.error("No person found for username: {}", username);
            throw new IllegalStateException("Person not found for username: " + username);
        }
        return entities.get(0); // Assuming the first result is the intended one
    }

    private Map<String, Object> fetchUserChannelAuthentication(Integer userId, TerminalType fromTerminal) {
        List<Map<String, Object>> userChannelAuthList = nibNativeRepository.findUserChannelAuthenticationByUserId(userId, fromTerminal);

        if (userChannelAuthList.isEmpty()) {
            log.error("No USER_CHANNEL_AUTHENTICATION found for userId: {}", userId);
            throw new IllegalStateException("No user channel authentication found for userId: " + userId);
        }

        // If only one record exists, return it regardless of whether it matches fromTerminal
        if (userChannelAuthList.size() == 1) {
            Map<String, Object> singleRecord = userChannelAuthList.get(0);
            log.debug("Single record found for userId: {}, CODE: {}", userId, singleRecord.get("CODE"));
            return singleRecord;
        }

        // If multiple records exist, prefer the one matching fromTerminal
        return userChannelAuthList.stream()
                .filter(auth -> fromTerminal.name().equals(auth.get("CODE")))
                .findFirst()
                .orElseGet(() -> {
                    // If no match is found, return the first record as a fallback (per requirement 3)
                    Map<String, Object> fallbackRecord = userChannelAuthList.get(0);
                    log.warn("No record matching terminal {} found for userId: {}, returning first record with CODE: {}",
                            fromTerminal, userId, fallbackRecord.get("CODE"));
                    return fallbackRecord;
                });
    }

    private Integer getParentNibChannelId() {
        Integer parentNibChannelId = nibNativeRepository.findParentChannel(TerminalType.NIB);
        if (parentNibChannelId == null) {
            log.error("Parent NIB channel ID not found");
            throw new IllegalStateException("Parent NIB channel not found");
        }
        return parentNibChannelId;
    }

    private void updateUserChannelAuthentication(Map<String, Object> userChannelAuth, Integer parentNibChannelId) {
//        Long maxId = Optional.ofNullable(nibNativeRepository.findUserChannelAuthenticationMaxId())
//                .orElse(0L); // Default to 0 if null
        userChannelAuth.put("CHANNEL_ID", parentNibChannelId);
//        userChannelAuth.put("USER_CHANNEL_AUTHENTICATION_ID", maxId + 1); // Increment max ID
//        log.debug("Updated USER_CHANNEL_AUTHENTICATION with CHANNEL_ID: {} and ID: {}",
//                parentNibChannelId, maxId + 1);
    }

    private void insertUpdatedUserChannelAuthentication(Map<String, Object> userChannelAuth) {
        int rowsAffected = nibNativeRepository.insertUserChannelAuthentication(userChannelAuth);
        if (rowsAffected != 1) {
            log.error("Failed to insert USER_CHANNEL_AUTHENTICATION, rows affected: {}", rowsAffected);
            throw new IllegalStateException("Failed to insert user channel authentication");
        }
    }
}