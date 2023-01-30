package auth.ece.app.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/FIE0S4
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class AMPds2Metric extends DatasetMetric {
    @CsvBindByPosition(position = 0)
    Long unixTimestamp;

    @CsvBindByPosition(position = 1)
    Float counter;

    @CsvBindByPosition(position = 2)
    Float avgRate;

    @CsvBindByPosition(position = 3)
    Float instRate;
}
