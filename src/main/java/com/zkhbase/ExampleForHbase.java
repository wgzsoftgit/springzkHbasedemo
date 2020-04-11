package com.zkhbase;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;

public class ExampleForHbase {
    public static Configuration configuration; // 管理Hbase的配置信息
    public static Connection connection; // 管理Hbase连接
    public static Admin admin; // 管理Hbase数据库的信息
    public static void main(String[] args) throws IOException {
        init(); //连接
        String colF[] ={"score"};
        createTable("student",colF); // 建表
       
        queryTableByCondition("student","zhangsan","score","English");
        insertData("student","zhangsan","score","English","69");
        insertData("student","zhangsan","score","Math","86");
        insertData("student","zhangsan","score","Computer","77");
        getData("student","zhangsan","score","Computer");
        queryTable("student");
        queryTableByRowKey("student","zhangsan","score","Computer"); //查询单列 表名，列名 列族
        //deleteColumnFamily("student");
        //deleteByRowKey("student","zhangsan");
        //truncateTable("student");
       // deleteTable("student");
    }
 
    // 操作数据库之前,建立连接
    public static void init(){
        configuration = HBaseConfiguration.create();
     //   configuration.set("hbase.rootdir","hdfs://192.168.220.129:9000/hbase");
        configuration.set("hbase.zookeeper.quorum","master:2181"); // 设置zookeeper节点
//        configuration.set("hbase.rpc.timeout", "3000");
//        configuration.set("hbase.client.operation.timeout", "3000");
//        configuration.set("hbase.client.scanner.timeout.period", "3000");
        
      //  configuration.set("hbase.zookeeper.property.clientPort","2181"); // 设置zookeeper节点
        try{
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
 
        }catch (IOException e){
            e.printStackTrace();
        }
    }
 
    // 1.创建表
    /*
    * @param myTableName 表名
    * @param colFamily 列族数组
    * @throws Exception
    * */
    public static void createTable(String myTableName,String[] colFamily) throws IOException{
        TableName tableName = TableName.valueOf(myTableName);
        if(admin.tableExists(tableName)){
            System.out.println("Table exists");
        }else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
            //②添加columnFamily 列族
            for(String str:colFamily){
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(str);
                hTableDescriptor.addFamily(hColumnDescriptor);
            }
            admin.createTable(hTableDescriptor);
        }
    }
  //2.创建表
    /**
     * 
     * @param tableName
     * @param info  多个参数
     * @throws IOException
     */
    public static void createTable2(String tableName, String... info) throws IOException {
        //①HTableDescriptor
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
        //②添加columnFamily 列族
        for (String cf : info) {
            hTableDescriptor.addFamily(new HColumnDescriptor(cf));
        }
        //③建表
        admin.createTable(hTableDescriptor);
        //④释放资源
        admin.close();
    }
    //查询表
    public static void queryTable(String name) throws IOException 
	{
		System.out.println("[hbaseoperation] start queryTable...");
 
		Table table = connection.getTable(TableName.valueOf(name));
		ResultScanner scanner = table.getScanner(new Scan());
 
		for (Result result : scanner) 
		{
			byte[] row = result.getRow();
			System.out.println("row key is:" + Bytes.toString(row));
 
			List<Cell> listCells = result.listCells();
			for (Cell cell : listCells) 
			{
				System.out.print("family:" + Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(),cell.getFamilyLength()));
				System.out.print("/qualifier:" + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
				System.out.print("/value:" + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
				System.out.println("/Timestamp:" + cell.getTimestamp());
			}
		}
		
		System.out.println("[hbaseoperation] end queryTable...");
	}
    //查询单行
    public static void queryTableByRowKey(String name,String rowkey,String colFamily,String authorName) throws IOException 
	{
		System.out.println("[hbaseoperation] start queryTableByRowKey...");
 
		Table table = connection.getTable(TableName.valueOf(name));
		Get get = new Get(rowkey.getBytes());
		
		get.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(authorName));//如果不追加列族，则查询所有列族
		Result result = table.get(get);
 
		List<Cell> listCells = result.listCells();
		for (Cell cell : listCells) 
		{
			String rowKey = Bytes.toString(CellUtil.cloneRow(cell));
			long timestamp = cell.getTimestamp();
			String family = Bytes.toString(CellUtil.cloneFamily(cell));
			String qualifier	= Bytes.toString(CellUtil.cloneQualifier(cell));
			String value = Bytes.toString(CellUtil.cloneValue(cell));  
 
			System.out.println(" ===> rowKey : " + rowKey + ",  timestamp : " +  timestamp + ", family : " + family + ", qualifier : " + qualifier + ", value : " + value);
		}
 
		System.out.println("[hbaseoperation] end queryTableByRowKey...");	
	}
    //根据具体的列名查询
    public static void queryTableByCondition(String name,String rowKeya,String colFamily,String authorName) throws IOException 
	{
		System.out.println("[hbaseoperation] start queryTableByCondition2...");
	
		Table table = connection.getTable(TableName.valueOf(name));
		Filter filter = new SingleColumnValueFilter(Bytes.toBytes(rowKeya), Bytes.toBytes(colFamily),CompareOp.EQUAL, Bytes.toBytes(authorName));

		Scan scan = new Scan();
 
		scan.setFilter(filter);
 
		ResultScanner scanner = table.getScanner(scan);
 
		for (Result result : scanner) 
		{
		    List<Cell> listCells = result.listCells();
		    for (Cell cell : listCells) 
 
			{
		        String rowKey = Bytes.toString(CellUtil.cloneRow(cell));
		        long timestamp = cell.getTimestamp();
		        String family = Bytes.toString(CellUtil.cloneFamily(cell));
		        String qualifier  = Bytes.toString(CellUtil.cloneQualifier(cell));
		        String value = Bytes.toString(CellUtil.cloneValue(cell));  
 
		        System.out.println(" ===> rowKey : " + rowKey + ",  timestamp : " + timestamp + ", family : " + family + ", qualifier : " + qualifier + ", value : " + value);
		    }
		}
 
		System.out.println("[hbaseoperation] end queryTableByCondition2...");
    }
    // 添加单元格数据
    /*
    * @param tableName 表名
    * @param rowKey 行键
    * @param colFamily 列族
    * @param col 列限定符
    * @param val 数据
    * @thorws Exception
    * */
    public static void insertData(String tableName,String rowKey,String colFamily,String col,String val) throws IOException{
        Table table = connection.getTable(TableName.valueOf(tableName));
        // put2 = new Put(Bytes.toBytes("row2"));//把String类型转成bytes类型
      //  put2.addColumn(Bytes.toBytes("columnfamily_1"), Bytes.toBytes("name"), Bytes.toBytes("<<C++ Prime>>"));
        
        Put put = new Put(rowKey.getBytes());
        put.addColumn(colFamily.getBytes(),col.getBytes(),val.getBytes());
        table.put(put);
        table.close();
    }
 
    //浏览数据
    /*
    * @param tableName 表名
    * @param rowKey 行
    * @param colFamily 列族
    * @param col 列限定符
    * @throw IOException
    * */
    public static void getData(String tableName,String rowKey,String colFamily,String col) throws IOException{
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get(rowKey.getBytes());
        get.addColumn(colFamily.getBytes(),col.getBytes());
        Result result =table.get(get);
        System.out.println(new String(result.getValue(colFamily.getBytes(),col==null?null:col.getBytes())));
        table.close();
    }
  //6.删除数据
    public static void deleteData(String tableName, String... rowkey) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        for (String rk : rowkey) {
            Delete del = new Delete(Bytes.toBytes(rk));//获得delete对象，其中持有要删除行的rowkey
            table.delete(del);
        }
        table.close();
    }
    public void deleteColumnFamily(String name,String cf) throws IOException
	{
        TableName tableName = TableName.valueOf(name);
        admin.deleteColumn(tableName, Bytes.toBytes(cf));
    }	
	
    public void deleteByRowKey(String name,String rowKey) throws IOException
	{
        Table table = connection.getTable(TableName.valueOf(name));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        table.delete(delete);
		queryTable(name);//&&
    }
    public void truncateTable(String name) throws IOException 
	{  
		TableName tableName = TableName.valueOf(name);
		//禁用并删除数据表内容的语句
		admin.disableTable(tableName);
		admin.truncateTable(tableName, true);
	}
 
	public void deleteTable(String name) throws IOException 
	{    //禁用并删除表
		admin.disableTable(TableName.valueOf(name));
		admin.deleteTable(TableName.valueOf(name));
	}
    // 操作数据库之后，关闭连接
    public static void close(){
        try{
            if(admin!=null){
                admin.close(); // 退出用户
            }
            if(null != connection){
                connection.close(); // 关闭连接
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
 
}
