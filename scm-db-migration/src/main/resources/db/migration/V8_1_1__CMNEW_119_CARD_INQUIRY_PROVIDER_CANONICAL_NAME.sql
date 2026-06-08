UPDATE REF.TBL_SCM_OPERATION_PROVIDER
SET NAME = 'hps-shetab7',
    URI = 'scm-shetab:hps-shetab7',
    LAST_EDIT_DATE = CURRENT_TIMESTAMP
WHERE OPERATION_PROVIDER_ID IN (
    SELECT OPERATION_PROVIDER_ID
    FROM REF.TBL_SCM_OPERATION
    WHERE NAME = 'SVC_CARD_INQUIRY_TCP'
)
  AND (
      NAME = 'hps'
      OR NAME = 'hps-shetab7'
      OR URI = 'shetab:' || 'hps'
      OR URI = 'shetab:hps-shetab7'
      OR URI = 'scm-shetab:hps-shetab7'
  );

UPDATE REF.TBL_SCM_OPERATION_PROVIDER
SET URI = 'scm-shetab:hps-shetab7',
    LAST_EDIT_DATE = CURRENT_TIMESTAMP
WHERE URI = 'shetab:hps-shetab7';

UPDATE REF.TBL_SCM_OPERATION_PROVIDER
SET NAME = 'hps-rest',
    URI = 'scm-rest:hps-rest',
    LAST_EDIT_DATE = CURRENT_TIMESTAMP
WHERE OPERATION_PROVIDER_ID IN (
    SELECT OPERATION_PROVIDER_ID
    FROM REF.TBL_SCM_OPERATION
    WHERE NAME IN ('SVC_CARD_INQUIRY_REST', 'SVC_CARD_PASSWORD_INQUIRY_REST', 'SVC_CARD_XFER_ADD_REST')
)
  AND (
      NAME = 'hps'
      OR NAME = 'hps-rest'
      OR URI = 'rest:hps-rest'
      OR URI = 'rest-provider:hps-rest'
      OR URI = 'scm-rest:hps-rest'
  );

UPDATE REF.TBL_SCM_OPERATION_PROVIDER
SET NAME = 'hps-rest',
    URI = 'scm-rest:hps-rest',
    LAST_EDIT_DATE = CURRENT_TIMESTAMP
WHERE URI IN ('rest:hps-rest', 'rest-provider:hps-rest');

UPDATE REF.TBL_SCM_OPERATION_PROVIDER
SET URI = 'scm-nab:nab-atps',
    LAST_EDIT_DATE = CURRENT_TIMESTAMP
WHERE URI = 'nab:nab-atps';
