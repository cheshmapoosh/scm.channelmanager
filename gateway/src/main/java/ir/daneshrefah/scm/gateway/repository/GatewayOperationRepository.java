package ir.daneshrefah.scm.gateway.repository;

import ir.daneshrefah.scm.common.model.GatewayOperation;
import ir.daneshrefah.scm.common.model.GatewayService;
import ir.daneshrefah.scm.common.model.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GatewayOperationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, GatewayOperation> findAllGatewayOperation() {
        String sql = "select O.*, S.SERVICE_CATEGORY_ID, S.NAME AS SERVICE_NAME, s.DESCRIPTION AS SERVICE_TITLE from ref.EB_SERVICE O  " +
                        "INNER JOIN REF.SERVICE_CATEGORY S " +
                        "ON O.SERVICE_CATEGORY_ID = S.SERVICE_CATEGORY_ID " +
                        "where PUBLISH = 1";
        List<GatewayOperation> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            GatewayOperation operation = new GatewayOperation();
//            operation.setId(rs.getLong("EB_SERVICE_ID"));
            operation.setServiceType(ServiceType.findByCode((int) rs.getLong("SERVICE_TYPE")));
            operation.setTitle(rs.getString("name"));
            operation.setCode(rs.getString("code"));
            GatewayService gatewayService = new GatewayService(rs.getLong("SERVICE_CATEGORY_ID"),
                    rs.getString("SERVICE_NAME"), rs.getString("SERVICE_TITLE"));
            operation.setService(gatewayService);
//            private String serviceProviderComponent;
            operation.setUrlBase(rs.getString("code"));
//            private String inputJSONSchema;
//            private String outputJSONSchema;
//            private String metadata;
            // Map other columns to entity fields as needed
            return operation;
        });
        Map<String, GatewayOperation> map = new HashMap<>();
        String citiesInputJSONSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "https://example.com/product.cities.json",
                  "title": "CitiesRequest",
                  "default": {},
                  "description": "A Core Banking Account Info",
                  "type": "object",
                   "properties": {
                     "id": {
                       "type": "string"
                     },
                     "type": {
                       "type": "integer"
                     }
                   },
                   "required": [
                     "id",
                     "type"
                   ]
                }
                """;
        String citiesOutputJSONSchema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "$id": "https://example.com/product.account.json",
                  "title": "Account",
                  "default": {},
                  "description": "A Core Banking Account Info",
                  "type": "object",
                  "properties": {
                    "accountNo": {
                      "description": "The unique identifier for a product",
                      "type": "string"
                    },
                    "title": {
                      "description": "The unique identifier for a product",
                      "type": "string"
                    },
                    "type": {
                      "description": "The unique identifier for a product",
                      "type": "integer"
                    }
                  },
                  "required": [ "accountNo" ]
                }
                """;
        String citiesMetadataJSONSchema = """
                {
                  "rq": {
                  "type": "object",
                  "properties": {
                        "P_IDX": {
                          "length": 10,
                          "type": "string",
                          "fromValue": "id",
                          "convertor": "fixString"
                        },
                        "P_TYPEX": {
                          "length": 10,
                          "type": "string",
                          "fromValue": "type",
                          "convertor": "fixString"
                        }
                    }
                    },
                    "rs": {
                       "type": "object",
                      "properties": {
                        "id": {
                          "length": 10,
                          "type": "string",
                          "fromValue": "P_IDX",
                          "convertor": "latinToPersianConvertor"
                        },
                        "type": {
                          "length": 200,
                          "type": "string",
                          "fromValue": "P_TYPEX"
                        }
                    }
                  }
                }
                """;
        if(map.isEmpty()) {
            map.put("cities", new GatewayOperation("cities", "nab", "/cities", citiesInputJSONSchema, citiesOutputJSONSchema, citiesMetadataJSONSchema));
            map.put("account_list",new GatewayOperation("account_list", "nab", "/accounts", citiesInputJSONSchema, citiesOutputJSONSchema, citiesMetadataJSONSchema));
            map.put("customer_list",new GatewayOperation("customer_list", "nab", "/customers", "", "", ""));
        }
        return map;
    }

}
