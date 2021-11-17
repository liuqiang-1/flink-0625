package com.atguigu.day08;

import com.atguigu.bean.WaterSensor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import static org.apache.flink.table.api.Expressions.$;

public class Flink07_SQL_Demo {
    public static void main(String[] args) throws Exception {
        //1.获取流的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //2.从元素中读取数据
        DataStreamSource<WaterSensor> waterSensorStream =
                env.fromElements(new WaterSensor("sensor_1", 1000L, 10),
                        new WaterSensor("sensor_1", 2000L, 20),
                        new WaterSensor("sensor_2", 3000L, 30),
                        new WaterSensor("sensor_1", 4000L, 40),
                        new WaterSensor("sensor_1", 5000L, 50),
                        new WaterSensor("sensor_2", 6000L, 60));


        //TODO 3.获取表的执行环境
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //4.将流转为动态表(未注册的表)
//        Table table = tableEnv.fromDataStream(waterSensorStream);

        //TODO 将流转为表（已注册的表）
//        Table table = tableEnv.fromDataStream(waterSensorStream);

//        tableEnv.registerTable("sensor", table);
//        tableEnv.createTemporaryView("sensor", table);
        tableEnv.createTemporaryView("sensor",waterSensorStream);


        //TODO 5.通过连续查询查询表中的数据(SQl)
//        Table resultTable = tableEnv.sqlQuery("select * from " + table + " where id = 'sensor_1'");
//        Table resultTable = tableEnv.sqlQuery("select * from sensor where id = 'sensor_1'");
//        TableResult tableResult = tableEnv.executeSql("select * from sensor where id = 'sensor_1'");
        TableResult tableResult = tableEnv.executeSql("select id,sum(vc) vcSum from sensor group by id");



//        resultTable.execute().print();
        tableResult.print();

        //TODO 6.将结果表转为流
//        DataStream<Row> result = tableEnv.toAppendStream(resultTable, Row.class);

//        result.print();



    }
}
