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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.beam.sdk.extensions.sql.utils.DateTimeUtils.parseTimestampWithUTCTimeZone;
import static org.apache.beam.sdk.extensions.sql.utils.DateTimeUtils.parseTimestampWithoutTimeZone;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.FieldType;
import org.apache.beam.sdk.schemas.logicaltypes.SqlTypes;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.vendor.guava.v32_1_2_jre.com.google.common.collect.Lists;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * prepare input records to test.
 *
 * <p>Note that, any change in these records would impact tests in this package.
 */
public class BeamSqlDslBase {
  @Rule public final TestPipeline pipeline = TestPipeline.create();
  @Rule public ExpectedException exceptions = ExpectedException.none();

  static Schema schemaInTableA;
  static Schema schemaFloatDouble;
  static Schema schemaBytes;
  static Schema schemaBytesPaddingTest;

  static List<Row> rowsInTableA;
  static List<Row> monthlyRowsInTableA;
  static List<Row> rowsOfFloatDouble;
  static List<Row> rowsOfBytes;
  static List<Row> rowsOfBytesPaddingTest;

  // bounded PCollections
  protected PCollection<Row> boundedInput1;
  protected PCollection<Row> boundedInput2;
  protected PCollection<Row> boundedInputFloatDouble;
  protected PCollection<Row> boundedInputBytes;
  protected PCollection<Row> boundedInputBytesPaddingTest;
  protected PCollection<Row> boundedInputMonthly;

  // unbounded PCollections
  protected PCollection<Row> unboundedInput1;
  protected PCollection<Row> unboundedInput2;

  @BeforeClass
  public static void prepareClass() throws ParseException {
    schemaInTableA =
        Schema.builder()
            .addInt32Field("f_int")
            .addInt64Field("f_long")
            .addInt16Field("f_short")
            .addByteField("f_byte")
            .addFloatField("f_float")
            .addDoubleField("f_double")
            .addStringField("f_string")
            .addField("f_date", FieldType.logicalType(SqlTypes.DATE))
            .addField("f_time", FieldType.logicalType(SqlTypes.TIME))
            .addField("f_datetime", FieldType.logicalType(SqlTypes.DATETIME))
            .addDateTimeField("f_timestamp")
            .addInt32Field("f_int2")
            .addDecimalField("f_decimal")
            .addIterableField("f_iterable", FieldType.STRING)
            .build();

    rowsInTableA =
        TestUtils.RowsBuilder.of(schemaInTableA)
            .addRows(
                1,
                1000L,
                (short) 1,
                (byte) 1,
                1.0f,
                1.0d,
                "string_row1",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 1, 3),
                LocalDateTime.of(2017, 1, 1, 1, 1, 3),
                parseTimestampWithoutTimeZone("2017-01-01 01:01:03"),
                0,
                new BigDecimal(1),
                Lists.newArrayList("s1", "s2"))
            .addRows(
                2,
                2000L,
                (short) 2,
                (byte) 2,
                2.0f,
                2.0d,
                "string_row2",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 2, 3),
                LocalDateTime.of(2017, 1, 1, 1, 2, 3),
                parseTimestampWithoutTimeZone("2017-01-01 01:02:03"),
                0,
                new BigDecimal(2),
                Lists.newArrayList("s1", "s2"))
            .addRows(
                3,
                3000L,
                (short) 3,
                (byte) 3,
                3.0f,
                3.0d,
                "string_row3",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 6, 3),
                LocalDateTime.of(2017, 1, 1, 1, 6, 3),
                parseTimestampWithoutTimeZone("2017-01-01 01:06:03"),
                0,
                new BigDecimal(3),
                Lists.newArrayList("s1", "s2"))
            .addRows(
                4,
                4000L,
                (short) 4,
                (byte) 4,
                4.0f,
                4.0d,
                "第四行",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(2, 4, 3),
                LocalDateTime.of(2017, 1, 1, 2, 4, 3),
                parseTimestampWithoutTimeZone("2017-01-01 02:04:03"),
                0,
                new BigDecimal(4),
                Lists.newArrayList("s1", "s2"))
            .getRows();

    monthlyRowsInTableA =
        TestUtils.RowsBuilder.of(schemaInTableA)
            .addRows(
                1,
                1000L,
                (short) 1,
                (byte) 1,
                1.0f,
                1.0d,
                "string_row1",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 1, 3),
                LocalDateTime.of(2017, 1, 1, 1, 1, 3),
                parseTimestampWithUTCTimeZone("2017-01-01 01:01:03"),
                0,
                new BigDecimal(1),
                Lists.newArrayList("s1", "s2"))
            .addRows(
                2,
                2000L,
                (short) 2,
                (byte) 2,
                2.0f,
                2.0d,
                "string_row2",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 2, 3),
                LocalDateTime.of(2017, 1, 1, 1, 2, 3),
                parseTimestampWithUTCTimeZone("2017-02-01 01:02:03"),
                0,
                new BigDecimal(2),
                Lists.newArrayList("s1", "s2"))
            .addRows(
                3,
                3000L,
                (short) 3,
                (byte) 3,
                3.0f,
                3.0d,
                "string_row3",
                LocalDate.of(2017, 1, 1),
                LocalTime.of(1, 6, 3),
                LocalDateTime.of(2017, 1, 1, 1, 6, 3),
                parseTimestampWithUTCTimeZone("2017-03-01 01:06:03"),
                0,
                new BigDecimal(3),
                Lists.newArrayList("s1", "s2"))
            .getRows();

    schemaFloatDouble =
        Schema.builder()
            .addFloatField("f_float_1")
            .addDoubleField("f_double_1")
            .addFloatField("f_float_2")
            .addDoubleField("f_double_2")
            .addFloatField("f_float_3")
            .addDoubleField("f_double_3")
            .build();

    rowsOfFloatDouble =
        TestUtils.RowsBuilder.of(schemaFloatDouble)
            .addRows(
                Float.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Float.NaN,
                Double.NaN)
            .getRows();

    schemaBytes = Schema.builder().addStringField("f_func").addByteArrayField("f_bytes").build();

    rowsOfBytes =
        TestUtils.RowsBuilder.of(schemaBytes)
            .addRows(
                "LENGTH",
                "".getBytes(UTF_8),
                "LENGTH",
                "абвгд".getBytes(UTF_8),
                "LENGTH",
                "\0\1".getBytes(UTF_8),
                "TO_HEX",
                "foobar".getBytes(UTF_8),
                "TO_HEX",
                " ".getBytes(UTF_8),
                "TO_HEX",
                "abcABC".getBytes(UTF_8),
                "TO_HEX",
                "abcABCжщфЖЩФ".getBytes(UTF_8),
                "HashingFn",
                "foobar".getBytes(UTF_8),
                "HashingFn",
                " ".getBytes(UTF_8),
                "HashingFn",
                "abcABCжщфЖЩФ".getBytes(UTF_8))
            .getRows();

    schemaBytesPaddingTest =
        Schema.builder()
            .addNullableField("f_bytes_one", FieldType.BYTES)
            .addNullableField("length", FieldType.INT64)
            .addNullableField("f_bytes_two", FieldType.BYTES)
            .build();
    rowsOfBytesPaddingTest =
        TestUtils.RowsBuilder.of(schemaBytesPaddingTest)
            .addRows(
                "abcdef".getBytes(UTF_8),
                0L,
                "defgh".getBytes(UTF_8),
                "abcdef".getBytes(UTF_8),
                6L,
                "defgh".getBytes(UTF_8),
                "abcdef".getBytes(UTF_8),
                4L,
                "defgh".getBytes(UTF_8),
                "abcdef".getBytes(UTF_8),
                10L,
                "defgh".getBytes(UTF_8),
                "abc".getBytes(UTF_8),
                10L,
                "defgh".getBytes(UTF_8),
                "abc".getBytes(UTF_8),
                7L,
                "-".getBytes(UTF_8),
                "".getBytes(UTF_8),
                7L,
                "def".getBytes(UTF_8),
                null,
                null,
                null)
            .getRows();
  }

  @Before
  public void preparePCollections() {
    boundedInput1 =
        pipeline.apply("boundedInput1", Create.of(rowsInTableA).withRowSchema(schemaInTableA));

    boundedInput2 =
        pipeline.apply(
            "boundedInput2", Create.of(rowsInTableA.get(0)).withRowSchema(schemaInTableA));

    boundedInputFloatDouble =
        pipeline.apply(
            "boundedInputFloatDouble",
            Create.of(rowsOfFloatDouble).withRowSchema(schemaFloatDouble));

    boundedInputBytes =
        pipeline.apply("boundedInputBytes", Create.of(rowsOfBytes).withRowSchema(schemaBytes));

    boundedInputBytesPaddingTest =
        pipeline.apply(
            "boundedInputBytesPaddingTest",
            Create.of(rowsOfBytesPaddingTest).withRowSchema(schemaBytesPaddingTest));
    boundedInputMonthly =
        pipeline.apply(
            "boundedInputMonthly", Create.of(monthlyRowsInTableA).withRowSchema(schemaInTableA));

    unboundedInput1 = prepareUnboundedPCollection1();
    unboundedInput2 = prepareUnboundedPCollection2();
  }

  private PCollection<Row> prepareUnboundedPCollection1() {
    TestStream.Builder<Row> values = TestStream.create(schemaInTableA);

    for (Row row : rowsInTableA) {
      values = values.advanceWatermarkTo(new Instant(row.getDateTime("f_timestamp")));
      values = values.addElements(row);
    }

    return PBegin.in(pipeline)
        .apply("unboundedInput1", values.advanceWatermarkToInfinity())
        .apply(
            "unboundedInput1.fixedWindow1year",
            Window.into(FixedWindows.of(Duration.standardDays(365))));
  }

  private PCollection<Row> prepareUnboundedPCollection2() {
    TestStream.Builder<Row> values = TestStream.create(schemaInTableA);

    Row row = rowsInTableA.get(0);
    values = values.advanceWatermarkTo(new Instant(row.getDateTime("f_timestamp")));
    values = values.addElements(row);

    return PBegin.in(pipeline)
        .apply("unboundedInput2", values.advanceWatermarkToInfinity())
        .apply(
            "unboundedInput2.fixedWindow1year",
            Window.into(FixedWindows.of(Duration.standardDays(365))));
  }
}
