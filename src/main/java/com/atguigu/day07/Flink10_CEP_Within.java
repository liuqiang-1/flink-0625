package com.atguigu.day07;

import com.atguigu.bean.WaterSensor;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Flink10_CEP_Within {
    public static void main(String[] args) throws Exception {
        //1.获取流的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //2.从文件读取数据，并将数据转为JavaBean
        SingleOutputStreamOperator<WaterSensor> waterSensorDStream = env.readTextFile("input/sensor3.txt")
                .map(new MapFunction<String, WaterSensor>() {
                    @Override
                    public WaterSensor map(String value) throws Exception {
                        String[] split = value.split(",");
                        return new WaterSensor(split[0], Long.parseLong(split[1]), Integer.parseInt(split[2]));
                    }
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner(new SerializableTimestampAssigner<WaterSensor>() {
                                    @Override
                                    public long extractTimestamp(WaterSensor element, long recordTimestamp) {
                                        return element.getTs() * 1000;
                                    }
                                })
                );

        //TODO 1.定义模式
        Pattern<WaterSensor, WaterSensor> pattern =
                        Pattern
                                .<WaterSensor>begin("start")
                                .where(new IterativeCondition<WaterSensor>() {
                                    @Override
                                    public boolean filter(WaterSensor value, Context<WaterSensor> ctx) throws Exception {
                                        return "sensor_1".equals(value.getId());
                                    }
                                })
                                .next("end")
                                .where(new IterativeCondition<WaterSensor>() {
                                    @Override
                                    public boolean filter(WaterSensor value, Context<WaterSensor> ctx) throws Exception {
                                        return "sensor_2".equals(value.getId());
                                    }
                                })
                                //窗口时间为3，当两条匹配到的数据之间间隔超过3S（包含3S）则匹配不到
                                //如果用到循环模式，则是否超时比的是当前模式第一个进来的事件
                .within(Time.seconds(3))
                ;

        //TODO 2.将模式作用于流上
        PatternStream<WaterSensor> patternStream = CEP.pattern(waterSensorDStream, pattern);

        //TODO 3.获取匹配到的数据
        patternStream
                .select(new PatternSelectFunction<WaterSensor, String>() {
                    @Override
                    public String select(Map<String, List<WaterSensor>> pattern) throws Exception {
                        return pattern.toString();
                    }
                }).print();

        env.execute();
    }
}