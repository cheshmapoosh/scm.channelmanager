package ir.daneshrefah.scm.uaa.captcha;

import com.google.code.kaptcha.servlet.KaptchaServlet;
import com.oopsguy.kaptcha.autoconfigure.KaptchaProperties;
import com.oopsguy.kaptcha.autoconfigure.util.ConfigUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Properties;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-11
 */
@RequiredArgsConstructor
public class ServletRegisterInitializer implements ServletContextInitializer {

    private final static String KAPTCHA_SERVLET_BEAN_NAME_SUBFFIX = "KapthcaServlet";

    @Resource
    private CaptchaProperties captchaProperties;

    @Resource(name = "captchaProps")
    private Properties captchaProps;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        Map<String, KaptchaProperties.SingleKaptchaProperties> items = captchaProperties.getItems();

        if (items == null || items.isEmpty()) {
            return;
        }

        KaptchaProperties.SingleKaptchaProperties props;

        for (Map.Entry<String, KaptchaProperties.SingleKaptchaProperties> entry : items.entrySet()) {
            props = entry.getValue();
            if (StringUtils.isEmpty(props.getPath())) {
                return;
            }

            ServletRegistration.Dynamic serviceServlet = servletContext.addServlet(
                    entry.getKey() + KAPTCHA_SERVLET_BEAN_NAME_SUBFFIX,
                    new CaptchaServlet());
            serviceServlet.addMapping(props.getPath());
            serviceServlet.setAsyncSupported(true);
            Properties subProps = ConfigUtils.kaptchaSubPropertiesToProperties(entry.getKey(), props);

            for (Map.Entry<Object, Object> en : captchaProps.entrySet()) {
                boolean isSkip = subProps.containsKey(en.getKey()) && !StringUtils.isEmpty(String.valueOf(subProps.get(en.getKey())))
                        || StringUtils.isEmpty(en.getValue());
                if (isSkip) {
                    continue;
                }
                subProps.setProperty(String.valueOf(en.getKey()), String.valueOf(en.getValue()));
            }

            for (Map.Entry<Object, Object> en : subProps.entrySet()) {
                serviceServlet.setInitParameter(String.valueOf(en.getKey()), String.valueOf(en.getValue()));
            }
        }
    }
}
