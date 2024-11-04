package ir.daneshrefah.scm.common.service.converter;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConverterService {

    //TODO SAMPLE CODE , CONVERTS DOES NOT IMPLEMENTED YET

    public List<Converters> getAll() {
        List<Converters> converters = new ArrayList<>();
        converters.add(new Converters("JTG", "مبدل تاریخ شمسی به میلادی", 1));
        converters.add(new Converters("GTJ", "مبدل تاریخ میلادی به شمسی", 1));
        return converters;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Converters {
        private  String code;
        private  String title;
        private  int version;

    }

}
