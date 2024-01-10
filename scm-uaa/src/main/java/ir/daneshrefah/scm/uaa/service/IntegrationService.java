package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.repository.authentication.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private Map<String, Integer> terminalCodeMap;
    private Map<Integer, String> channelIdMap;

    public String findTerminalCodeByChannelId(Integer channelId) {
        if (null == channelIdMap)
            initChannelMaps();
        return channelIdMap.get(channelId);
    }

    public Integer findChannelIdByTerminalCode(String terminalCode) {
        if (null == terminalCodeMap)
            initChannelMaps();
        return terminalCodeMap.get(terminalCode);
    }

    private void initChannelMaps() {
        if (null == terminalCodeMap) {
            synchronized (this) {
                if (null == terminalCodeMap) {
                    List<String[]> terminalList = integrationRepository.findAllTerminals();
                    terminalCodeMap = terminalList.stream()
                            .collect(Collectors.toMap(arr -> arr[0].trim().toUpperCase(), arr -> Integer.valueOf(arr[1])));
                    channelIdMap = terminalList.stream()
                            .collect(Collectors.toMap(arr -> Integer.valueOf(arr[1]), arr -> arr[0].trim().toUpperCase()));
                }
            }
        }
    }
}
