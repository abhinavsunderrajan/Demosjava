package networkutils;

import java.io.IOException;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;

/**
 * I am planning to use {@linkplain Table} API for testing purposes. All tests
 * and trials will be done here.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class TestTableSaw {

    public static void main(String args[]) throws IOException {

	Builder builder = CsvReadOptions.builder(
		"/Users/abhinav.sunderrajan/Desktop/road-speed-profiling/abhinav/road_network_files/2019-8/unnormalized/roads_6_2019-8.txt")
		.separator('\t');
	CsvReadOptions options = builder.build();
	Table t1 = Table.read().csv(options);
	System.out.println(t1.columnNames());

	int i = 0;
	for (Row row : t1) {
	    System.out.println(row.getObject(0));
	    i++;
	    if (i == 10)
		break;
	}

//	x.column("node_id").forEach(nodeId -> {
//	    System.out.println(nodeId);
//	});
    }

}
