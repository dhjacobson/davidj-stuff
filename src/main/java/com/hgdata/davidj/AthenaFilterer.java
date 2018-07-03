package com.hgdata.davidj;

import com.hgdata.davidj.db.AthenaDb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AthenaFilterer {

    private AthenaDb athena = new AthenaDb();

    public AthenaFilterer() {

    }

    public void filterAndWriteToFile(String athenaDatabase, String athenaTable, String filterSqlClause, String outputField, String outputFileName) throws IOException, SQLException {
        FileWriter fstream = new FileWriter(new File("output/athena-filter/" + outputFileName));
        BufferedWriter out = new BufferedWriter(fstream);

        String sql = String.format(
                "SELECT %s FROM %s.%s WHERE %s", outputField, athenaDatabase, athenaTable, filterSqlClause
        );



        ResultSet rs = athena.fetch(sql);
        rs.setFetchSize(100);

        int counter = 0;
        while (rs.next()) {
            out.write(rs.getString(outputField));
            out.newLine();
            counter++;
            if (counter % 100000 == 0) {
                System.out.println(counter);
            }
        }

        System.out.println("Completed writing into text file");
        out.close();

    }

    public static void main(String[] args) throws IOException, SQLException {
        AthenaFilterer filterer = new AthenaFilterer();


        String[] hexChars = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        for (int i=0; i<hexChars.length; i++) {
            String filterSqlClause = String.format("trim(lower(regexp_extract(everything, '\"domain\" *: *\"([\\w-.]*?)\"', 1))) IN (\n" +
                    "  SELECT alternate_url from mrd20180327delivery.farnsworth_url_aliases ua\n" +
                    "  JOIN mrd20180327delivery.farnsworth_urls u ON ua.url = u.url\n" +
                    "  WHERE u.core_product_hits > 0 OR u.completeness_score = 1\n" +
                    ")\n" +
                    "AND substr(to_hex(md5(to_utf8(everything))),1,1) = '%s'", hexChars[i]);
            filterer.filterAndWriteToFile(
                    "digsig",
                    "ds17_raw_updates_03_12_2018_single_field",
                    filterSqlClause,
                    "everything",
                    "ds17_raw_03_12_2018_ab_only.txt"
            );
        }
    }

}
