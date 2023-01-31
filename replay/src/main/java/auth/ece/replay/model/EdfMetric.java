package auth.ece.replay.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * https://archive.ics.uci.edu/ml/datasets/Individual+household+electric+power+consumption
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class EdfMetric extends DatasetMetric {

    @CsvBindByPosition(position = 0)
    String date;

    @CsvBindByPosition(position = 1)
    String time;

    @CsvBindByPosition(position = 2)
    Float activePower;

    @CsvBindByPosition(position = 3)
    Float reactivePower;

    @CsvBindByPosition(position = 4)
    Float voltage;

    @CsvBindByPosition(position = 5)
    Float intensity;
}
