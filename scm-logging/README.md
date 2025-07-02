# Log Server Application

This application listens to IBM MQ and inserts logs into `TBL_TRACE_LOG`.

## Implementing Filebeat

1. **Download Filebeat**
    - [Filebeat Download Link](https://www.elastic.co/downloads/beats/filebeat)

2. **Extract Filebeat**
    - Extract the downloaded Filebeat package to your preferred directory.

3. **Configure Filebeat**
    - Open the `filebeat.yml` file and add the following configuration:

    ```yaml
    #============================== Filebeat inputs ===============================
    filebeat.inputs:
      - type: filestream
        enabled: true
        paths:
          # this must be the path to your app.log file
          - /home/rayani.a@drp.local/Desktop/danesh-refah/channelmanager/appLogFolder/*.log 
    # ================================== Outputs ===================================
    # ------------------------------ Logstash Output -------------------------------
    output.logstash:
      # The Logstash hosts
      hosts: ["localhost:5044"]
    ```

   **Note:** If Elasticsearch output configuration is enabled, comment it out:

    ```yaml
    # ================================== Outputs ===================================
    # ---------------------------- Elasticsearch Output ----------------------------
    #output.elasticsearch:
      # Array of hosts to connect to.
      #hosts: ["localhost:9200"]
    ```

4. **Run Filebeat**
    - Navigate to the directory where you extracted Filebeat and run the following command:

    ```sh
    ./filebeat -e
    ```

This will start Filebeat and forward the logs specified in the `filebeat.yml` configuration to Logstash running
on `localhost:5044`.

## Implementing Logstash

1. **Download Logstash**
    - [Logstash Download Link](https://www.elastic.co/downloads/logstash)

2. **Extract Logstash**
    - Extract the downloaded Logstash package to your preferred directory.

3. **Configure Logstash**
    - Go to the Logstash directory and create `pipeline.conf` with the following content:

    ```yaml
    input {
      stdin { # This is for demo
         codec => json
      }
      
      beats {
         port => 5044
         host => "0.0.0.0"
      }
    }

    filter {
      grok {
        match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} %{GREEDYDATA:json_message}" }
      }
      json {
        source => "json_message"
        target => "payload"
        remove_field => ["json_message","[payload][events]"]
      }
      
      mutate {
        remove_field => ["message","events"]
      }
    }

    output { # This is for demo
      stdout {
         codec => rubydebug
      }
      
      jms {
        destination => "SCM2LOG"
        yaml_file => "/home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/mq.yml" # change this path
        yaml_section => "wmq"
      }
    }
    ```

   Make sure to update the `yaml_file` path to the location where you placed the `mq.yml` file.

4. **Create `mq.yml` for JMS Plugin**

   Create a `mq.yml` file in the Logstash directory with the following content:

    ```yaml
    wmq:
      :factory: com.ibm.mq.jms.MQQueueConnectionFactory
      :queue_manager: CSQ1
      :host_name: 10.10.4.238
      :channel: DEV.ADMIN.SVRCONN
      :port: 11414
      :transport_type: 1
      :username: admin
      :password: passw0rd
      :require_jars:
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.dhbcore.jar 
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.axis2.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.commonservices.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.headers.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.jmqi.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.jms.Nojndi.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.pcf.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.soap.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mq.tools.ras.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/com.ibm.mqjms.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/javax.jms.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/javax.resource.jar
        - /home/rayani.a@drp.local/Downloads/test/logstash-8.14.3/jars/javax.transaction.jar
    ```

5. **Install JMS Plugin**
    - Download the necessary JAR files from
      the [IBM MQ Github Repository](https://github.com/saudurehman/logstash_jms_ibm_mq).

    - Place the JAR files in the Logstash directory and update the paths in `mq.yml`.

    - Install the JMS plugin with the following command:

    ```sh
    bin/logstash-plugin install logstash-input-jms
    ```

6. **Run Logstash**

    - Navigate to the Logstash directory and run the following command to start Logstash with your configuration:

    ```sh
    ./bin/logstash -f config/pipelines/pipeline.conf
    ```

   Place the `pipeline.conf` file in the `config/pipelines` directory.

This will start Logstash and process the logs forwarded by Filebeat, outputting them to the specified JMS destination.
