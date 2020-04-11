package com.zkhbase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 //root  Huawei12#$
//192.168.134.151   2222
public class HbaseTest2 {
    private static HBaseAdmin admin = null;
    public static Connection conn = null;
    // 定义配置对象HBaseConfiguration
    private static Configuration configuration;
    public HbaseTest2() throws Exception {
    	Configuration configuration =  new Configuration();
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum","master");  //hbase 服务地址192.168.220.129
        configuration.set("hbase.zookeeper.property.clientPort","2181"); //端口号
        conn = ConnectionFactory.createConnection(configuration);
        admin = (HBaseAdmin) conn.getAdmin();
    }
    // Hbase获取所有的表信息
    public List<String> getAllTables() {
        List<String> tables = null;
        if (admin != null) {
            try {
                HTableDescriptor[] allTable = admin.listTables();
                System.out.println("11111111111");
                System.out.println(allTable);
                System.out.println("2222222222222");
                if (allTable.length > 0) tables = new ArrayList<String>();
                for (HTableDescriptor hTableDescriptor : allTable) {
                    tables.add(hTableDescriptor.getNameAsString());
                    System.out.println(hTableDescriptor.getNameAsString());
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tables;
    }
    public static void main(String[] args) throws Exception {
        HbaseTest2 hbaseTest = new HbaseTest2();
        System.out.println(admin);
        List<String> tables = hbaseTest.getAllTables();
        for(String s:tables) {
        	System.out.println(s);
        }
    }
}
//https://blog.csdn.net/yz930618/java/article/details/74173332