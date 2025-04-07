package ir.daneshrefah.scm.jmeter.samplers.cache;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

public class CacheClientSampler extends AbstractSampler {

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.sampleStart();

        try {
            String response = "Processed: ";

            result.setResponseData(response, null);
            result.setSuccessful(true);
        } catch (Exception e) {
            result.setSuccessful(false);
        } finally {
            result.sampleEnd();
        }
        return result;
    }
}
