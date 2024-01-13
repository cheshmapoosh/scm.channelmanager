package ir.daneshrefah.scm.core.authority.decision.voter;


import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.VoteEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public abstract class DecisionVoter {
    List<DecisionVoter>  DECISION_VOTER_LIST = new ArrayList<>();
    /**
     * if any voter return a ACCESS_GRANTED value, the manager accept it and end the checking another voter
     */
    public static final int ACCESS_GRANTED = 1;
    /**if the voter return ACCESS_ABSTAIN it means the voter accepted but manager must check another voters */
    public static final int ACCESS_ABSTAIN = 0;
    /**
     * if the voter return ACCESS_DENIED or throws any AuthorityBaseException it means that rule does not passed and
     * the manager stopped the checking another voters.
     */
    public static final int ACCESS_DENIED = -1;

    public final int vote(Message message, TerminalServiceChannelAccess authObject) {
        Instant startTime = Instant.now();
        boolean isSupport = support(authObject);
        if (!isSupport) {
            return ACCESS_ABSTAIN;
        }
        int response = vote(message);
        logVotingEvent(message, this.getClass().getSimpleName(), response, startTime);
        return response;
    }

    protected abstract int vote(Message message);

    protected abstract boolean support(TerminalServiceChannelAccess service);

    private final void logVotingEvent(Message message, String input, int output, Instant startTime) {
        Instant endTime = Instant.now();
        Event event = VoteEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(startTime)
                .terminalCode(message.getHeader().getTerminalCode())
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
                .input(input)
                .output(output)
                .clientAgent(message.getHeader().getClientAgent())
                .endTimestamp(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

}
