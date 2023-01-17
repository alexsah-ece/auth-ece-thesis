package auth.ece.app.model;

import com.opencsv.bean.CsvBindByName;
import lombok.ToString;

/**
 * https://archive.ics.uci.edu/ml/datasets/Individual+household+electric+power+consumption
 */
@ToString
public class EdfDataset {

    @CsvBindByName(column = "Date")
    String date;

    @CsvBindByName(column = "Time")
    String time;

    @CsvBindByName(column = "Global_active_power")
    Float activePower;

    @CsvBindByName(column = "Global_reactive_power")
    Float reactivePower;

    @CsvBindByName(column = "Voltage")
    Float voltage;

    @CsvBindByName(column = "Global_intensity")
    Float intensity;
}