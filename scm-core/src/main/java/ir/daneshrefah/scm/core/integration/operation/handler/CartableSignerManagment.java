package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.data.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import ir.daneshrefah.scm.provider.task.service.TaskManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartableSignerManagment  {

    private final TaskManagementServiceImpl taskManagementService;
    private final ObjectMapper mapper;
    private final PersonRepository personRepository;
    private final MembershipRepository membershipRepository;

    @JavaService(operationCode = OperationCode.SVC_ADD_SIGNERS)
    @Transactional
    public void handle(Exchange exchange) throws Exception {

//        JsonNode body = (JsonNode) exchange.getProperty(Message.ORIGINAL_BODY);
        Object body = exchange.getMessage().getBody();
        ObjectNode request = (ObjectNode) body;

        ObjectNode transactionData = (ObjectNode) request.get("transactionData");
        if (transactionData == null) {
            throw new IllegalArgumentException("transactionData is null");
        }

        List<TaskEntity> tasks = getTasksByProcessId(request);
        addCustomerCout(transactionData, tasks);
        addCustomerList(exchange, transactionData, tasks);

        exchange.getMessage().setBody(request);
    }

    private void addCustomerList(Exchange exchange, ObjectNode transactionData, List<TaskEntity> tasks) {
        ArrayNode customerIds = mapper.createArrayNode();
        for (TaskEntity task : tasks) {
            MembershipEntity membership = getMembership(task);
            String customerNo = membership.getCustomerAccount().getCustomer().getCustomerNo();

            if (membership.getPerson().getPersonType() == PersonType.CORPORATE) {
                customerIds.insert(0, "31808154");
            } else {
                customerIds.add(customerNo);
            }
        }
        exchange.setProperty("users", customerIds);
        transactionData.set("customers", customerIds);
    }

    private void addCustomerCout(ObjectNode transactionData, List<TaskEntity> tasks) {
        transactionData.put("customerCount", tasks.size());
    }

    private MembershipEntity getMembership(TaskEntity task) {
        Integer userId = task.getUserId();
        MembershipEntity membership = membershipRepository.findMembershipListByUserId(userId).getFirst();

        if (membership == null) {
            throw new RuntimeException("Membership not found");
        }
        return membership;
    }

    private List<TaskEntity> getTasksByProcessId(ObjectNode request) {
        JsonNode processId = request.get("processId");
        if (Objects.isNull(processId)) {
            throw new IllegalArgumentException("processId is null");
        }
        List<TaskEntity> tasks = taskManagementService.findByProcessInstanceId(processId.asLong());
        if (Objects.isNull(tasks)) {
            throw new IllegalArgumentException("tasks is null");
        }
        return tasks;
    }
}
