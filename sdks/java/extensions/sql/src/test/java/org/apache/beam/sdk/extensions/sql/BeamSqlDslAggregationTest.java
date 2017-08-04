/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.extensions.sql;

import java.sql.Types;
import java.util.Arrays;
import org.apache.beam.sdk.extensions.sql.schema.BeamSqlRecordType;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.BeamRecord;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.junit.Test;

/**
 * Tests for GROUP-BY/aggregation, with global_window/fix_time_window/sliding_window/session_window
 * with BOUNDED PCollection.
 */
public class BeamSqlDslAggregationTest extends BeamSqlDslBase {
  /**
   * GROUP-BY with single aggregation function with bounded PCollection.
   */
  @Test
  public void testAggregationWithoutWindowWithBounded() throws Exception {
    runAggregationWithoutWindow(boundedInput1);
  }

  /**
   * GROUP-BY with single aggregation function with unbounded PCollection.
   */
  @Test
  public void testAggregationWithoutWindowWithUnbounded() throws Exception {
    runAggregationWithoutWindow(unboundedInput1);
  }

  private void runAggregationWithoutWindow(PCollection<BeamRecord> input) throws Exception {
    String sql = "SELECT f_int2, COUNT(*) AS `size` FROM PCOLLECTION GROUP BY f_int2";

    PCollection<BeamRecord> result =
        input.apply("testAggregationWithoutWindow", BeamSql.simpleQuery(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(Arrays.asList("f_int2", "size"),
        Arrays.asList(Types.INTEGER, Types.BIGINT));

    BeamRecord record = new BeamRecord(resultType);
    record.addField("f_int2", 0);
    record.addField("size", 4L);

    PAssert.that(result).containsInAnyOrder(record);

    pipeline.run().waitUntilFinish();
  }

  /**
   * GROUP-BY with multiple aggregation functions with bounded PCollection.
   */
  @Test
  public void testAggregationFunctionsWithBounded() throws Exception{
    runAggregationFunctions(boundedInput1);
  }

  /**
   * GROUP-BY with multiple aggregation functions with unbounded PCollection.
   */
  @Test
  public void testAggregationFunctionsWithUnbounded() throws Exception{
    runAggregationFunctions(unboundedInput1);
  }

  private void runAggregationFunctions(PCollection<BeamRecord> input) throws Exception{
    String sql = "select f_int2, count(*) as size, "
        + "sum(f_long) as sum1, avg(f_long) as avg1, max(f_long) as max1, min(f_long) as min1,"
        + "sum(f_short) as sum2, avg(f_short) as avg2, max(f_short) as max2, min(f_short) as min2,"
        + "sum(f_byte) as sum3, avg(f_byte) as avg3, max(f_byte) as max3, min(f_byte) as min3,"
        + "sum(f_float) as sum4, avg(f_float) as avg4, max(f_float) as max4, min(f_float) as min4,"
        + "sum(f_double) as sum5, avg(f_double) as avg5, "
        + "max(f_double) as max5, min(f_double) as min5,"
        + "max(f_timestamp) as max6, min(f_timestamp) as min6 "
        + "FROM TABLE_A group by f_int2";

    PCollection<BeamRecord> result =
        PCollectionTuple.of(new TupleTag<BeamRecord>("TABLE_A"), input)
        .apply("testAggregationFunctions", BeamSql.query(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(
        Arrays.asList("f_int2", "size", "sum1", "avg1", "max1", "min1", "sum2", "avg2", "max2",
            "min2", "sum3", "avg3", "max3", "min3", "sum4", "avg4", "max4", "min4", "sum5", "avg5",
            "max5", "min5", "max6", "min6"),
        Arrays.asList(Types.INTEGER, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT,
            Types.BIGINT, Types.SMALLINT, Types.SMALLINT, Types.SMALLINT, Types.SMALLINT,
            Types.TINYINT, Types.TINYINT, Types.TINYINT, Types.TINYINT, Types.FLOAT, Types.FLOAT,
            Types.FLOAT, Types.FLOAT, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE,
            Types.TIMESTAMP, Types.TIMESTAMP));

    BeamRecord record = new BeamRecord(resultType);
    record.addField("f_int2", 0);
    record.addField("size", 4L);

    record.addField("sum1", 10000L);
    record.addField("avg1", 2500L);
    record.addField("max1", 4000L);
    record.addField("min1", 1000L);

    record.addField("sum2", (short) 10);
    record.addField("avg2", (short) 2);
    record.addField("max2", (short) 4);
    record.addField("min2", (short) 1);

    record.addField("sum3", (byte) 10);
    record.addField("avg3", (byte) 2);
    record.addField("max3", (byte) 4);
    record.addField("min3", (byte) 1);

    record.addField("sum4", 10.0F);
    record.addField("avg4", 2.5F);
    record.addField("max4", 4.0F);
    record.addField("min4", 1.0F);

    record.addField("sum5", 10.0);
    record.addField("avg5", 2.5);
    record.addField("max5", 4.0);
    record.addField("min5", 1.0);

    record.addField("max6", FORMAT.parse("2017-01-01 02:04:03"));
    record.addField("min6", FORMAT.parse("2017-01-01 01:01:03"));

    PAssert.that(result).containsInAnyOrder(record);

    pipeline.run().waitUntilFinish();
  }

  /**
   * Implicit GROUP-BY with DISTINCT with bounded PCollection.
   */
  @Test
  public void testDistinctWithBounded() throws Exception {
    runDistinct(boundedInput1);
  }

  /**
   * Implicit GROUP-BY with DISTINCT with unbounded PCollection.
   */
  @Test
  public void testDistinctWithUnbounded() throws Exception {
    runDistinct(unboundedInput1);
  }

  private void runDistinct(PCollection<BeamRecord> input) throws Exception {
    String sql = "SELECT distinct f_int, f_long FROM PCOLLECTION ";

    PCollection<BeamRecord> result =
        input.apply("testDistinct", BeamSql.simpleQuery(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(Arrays.asList("f_int", "f_long"),
        Arrays.asList(Types.INTEGER, Types.BIGINT));

    BeamRecord record1 = new BeamRecord(resultType);
    record1.addField("f_int", 1);
    record1.addField("f_long", 1000L);

    BeamRecord record2 = new BeamRecord(resultType);
    record2.addField("f_int", 2);
    record2.addField("f_long", 2000L);

    BeamRecord record3 = new BeamRecord(resultType);
    record3.addField("f_int", 3);
    record3.addField("f_long", 3000L);

    BeamRecord record4 = new BeamRecord(resultType);
    record4.addField("f_int", 4);
    record4.addField("f_long", 4000L);

    PAssert.that(result).containsInAnyOrder(record1, record2, record3, record4);

    pipeline.run().waitUntilFinish();
  }

  /**
   * GROUP-BY with TUMBLE window(aka fix_time_window) with bounded PCollection.
   */
  @Test
  public void testTumbleWindowWithBounded() throws Exception {
    runTumbleWindow(boundedInput1);
  }

  /**
   * GROUP-BY with TUMBLE window(aka fix_time_window) with unbounded PCollection.
   */
  @Test
  public void testTumbleWindowWithUnbounded() throws Exception {
    runTumbleWindow(unboundedInput1);
  }

  private void runTumbleWindow(PCollection<BeamRecord> input) throws Exception {
    String sql = "SELECT f_int2, COUNT(*) AS `size`,"
        + " TUMBLE_START(f_timestamp, INTERVAL '1' HOUR) AS `window_start`"
        + " FROM TABLE_A"
        + " GROUP BY f_int2, TUMBLE(f_timestamp, INTERVAL '1' HOUR)";
    PCollection<BeamRecord> result =
        PCollectionTuple.of(new TupleTag<BeamRecord>("TABLE_A"), input)
        .apply("testTumbleWindow", BeamSql.query(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(
        Arrays.asList("f_int2", "size", "window_start"),
        Arrays.asList(Types.INTEGER, Types.BIGINT, Types.TIMESTAMP));

    BeamRecord record1 = new BeamRecord(resultType);
    record1.addField("f_int2", 0);
    record1.addField("size", 3L);
    record1.addField("window_start", FORMAT.parse("2017-01-01 01:00:00"));

    BeamRecord record2 = new BeamRecord(resultType);
    record2.addField("f_int2", 0);
    record2.addField("size", 1L);
    record2.addField("window_start", FORMAT.parse("2017-01-01 02:00:00"));

    PAssert.that(result).containsInAnyOrder(record1, record2);

    pipeline.run().waitUntilFinish();
  }

  /**
   * GROUP-BY with HOP window(aka sliding_window) with bounded PCollection.
   */
  @Test
  public void testHopWindowWithBounded() throws Exception {
    runHopWindow(boundedInput1);
  }

  /**
   * GROUP-BY with HOP window(aka sliding_window) with unbounded PCollection.
   */
  @Test
  public void testHopWindowWithUnbounded() throws Exception {
    runHopWindow(unboundedInput1);
  }

  private void runHopWindow(PCollection<BeamRecord> input) throws Exception {
    String sql = "SELECT f_int2, COUNT(*) AS `size`,"
        + " HOP_START(f_timestamp, INTERVAL '1' HOUR, INTERVAL '30' MINUTE) AS `window_start`"
        + " FROM PCOLLECTION"
        + " GROUP BY f_int2, HOP(f_timestamp, INTERVAL '1' HOUR, INTERVAL '30' MINUTE)";
    PCollection<BeamRecord> result =
        input.apply("testHopWindow", BeamSql.simpleQuery(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(
        Arrays.asList("f_int2", "size", "window_start"),
        Arrays.asList(Types.INTEGER, Types.BIGINT, Types.TIMESTAMP));

    BeamRecord record1 = new BeamRecord(resultType);
    record1.addField("f_int2", 0);
    record1.addField("size", 3L);
    record1.addField("window_start", FORMAT.parse("2017-01-01 00:30:00"));

    BeamRecord record2 = new BeamRecord(resultType);
    record2.addField("f_int2", 0);
    record2.addField("size", 3L);
    record2.addField("window_start", FORMAT.parse("2017-01-01 01:00:00"));

    BeamRecord record3 = new BeamRecord(resultType);
    record3.addField("f_int2", 0);
    record3.addField("size", 1L);
    record3.addField("window_start", FORMAT.parse("2017-01-01 01:30:00"));

    BeamRecord record4 = new BeamRecord(resultType);
    record4.addField("f_int2", 0);
    record4.addField("size", 1L);
    record4.addField("window_start", FORMAT.parse("2017-01-01 02:00:00"));

    PAssert.that(result).containsInAnyOrder(record1, record2, record3, record4);

    pipeline.run().waitUntilFinish();
  }

  /**
   * GROUP-BY with SESSION window with bounded PCollection.
   */
  @Test
  public void testSessionWindowWithBounded() throws Exception {
    runSessionWindow(boundedInput1);
  }

  /**
   * GROUP-BY with SESSION window with unbounded PCollection.
   */
  @Test
  public void testSessionWindowWithUnbounded() throws Exception {
    runSessionWindow(unboundedInput1);
  }

  private void runSessionWindow(PCollection<BeamRecord> input) throws Exception {
    String sql = "SELECT f_int2, COUNT(*) AS `size`,"
        + " SESSION_START(f_timestamp, INTERVAL '5' MINUTE) AS `window_start`"
        + " FROM TABLE_A"
        + " GROUP BY f_int2, SESSION(f_timestamp, INTERVAL '5' MINUTE)";
    PCollection<BeamRecord> result =
        PCollectionTuple.of(new TupleTag<BeamRecord>("TABLE_A"), input)
        .apply("testSessionWindow", BeamSql.query(sql));

    BeamSqlRecordType resultType = BeamSqlRecordType.create(
        Arrays.asList("f_int2", "size", "window_start"),
        Arrays.asList(Types.INTEGER, Types.BIGINT, Types.TIMESTAMP));

    BeamRecord record1 = new BeamRecord(resultType);
    record1.addField("f_int2", 0);
    record1.addField("size", 3L);
    record1.addField("window_start", FORMAT.parse("2017-01-01 01:01:03"));

    BeamRecord record2 = new BeamRecord(resultType);
    record2.addField("f_int2", 0);
    record2.addField("size", 1L);
    record2.addField("window_start", FORMAT.parse("2017-01-01 02:04:03"));

    PAssert.that(result).containsInAnyOrder(record1, record2);

    pipeline.run().waitUntilFinish();
  }

  @Test
  public void testWindowOnNonTimestampField() throws Exception {
    exceptions.expect(IllegalStateException.class);
    exceptions.expectMessage(
        "Cannot apply 'TUMBLE' to arguments of type 'TUMBLE(<BIGINT>, <INTERVAL HOUR>)'");
    pipeline.enableAbandonedNodeEnforcement(false);

    String sql = "SELECT f_int2, COUNT(*) AS `size` FROM TABLE_A "
        + "GROUP BY f_int2, TUMBLE(f_long, INTERVAL '1' HOUR)";
    PCollection<BeamRecord> result =
        PCollectionTuple.of(new TupleTag<BeamRecord>("TABLE_A"), boundedInput1)
        .apply("testWindowOnNonTimestampField", BeamSql.query(sql));

    pipeline.run().waitUntilFinish();
  }

  @Test
  public void testUnsupportedDistinct() throws Exception {
    exceptions.expect(IllegalStateException.class);
    exceptions.expectMessage("Encountered \"*\"");
    pipeline.enableAbandonedNodeEnforcement(false);

    String sql = "SELECT f_int2, COUNT(DISTINCT *) AS `size` FROM PCOLLECTION GROUP BY f_int2";

    PCollection<BeamRecord> result =
        boundedInput1.apply("testUnsupportedDistinct", BeamSql.simpleQuery(sql));

    pipeline.run().waitUntilFinish();
  }
}
