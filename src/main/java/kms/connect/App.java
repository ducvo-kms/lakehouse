package kms.connect;


import io.delta.sql.DeltaSparkSessionExtension;
import java.util.Objects;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.delta.catalog.DeltaCatalog;

public class App {

  public static void main(String[] args) {
    SparkConf conf =
        new SparkConf()
            .setAppName("MRF")
            .setMaster("local[8]")
            .set("hive.metastore.uris", "thrift://localhost:9083")
            .set("spark.hadoop.fs.s3a.aws.credentials.provider",
                SimpleAWSCredentialsProvider.class.getName())
            .set("spark.hadoop.fs.s3a.access.key", "minio")
            .set("spark.hadoop.fs.s3a.secret.key", "minio123")
            .set("spark.hadoop.fs.s3a.path.style.access", "true")
            .set("spark.hadoop.fs.s3a.endpoint", "http://localhost:9000")
            .set("spark.hadoop.fs.s3a.connection.ssl.enabled", "false")
            .set("spark.hadoop.fs.s3a.connection.timeout", "200000")
            .set("spark.sql.warehouse.dir", "s3a://datalake/lakehouse")
            .set("spark.hadoop.fs.s3a.connection.establish.timeout", "50000")
            .set("spark.sql.extensions", DeltaSparkSessionExtension.class.getName())
            .set("spark.sql.catalog.spark_catalog", DeltaCatalog.class.getName())
            .set("spark.hadoop.fs.s3a.impl", S3AFileSystem.class.getName())
            .set("spark.executor.cores", "8")
            .set("spark.executor.memory", "16g")
            .set("spark.task.cpus", "8")
            .set("spark.databricks.delta.schema.autoMerge.enabled", "true");

    SparkContext context = new SparkContext(conf);

    SparkSession spark =
        SparkSession.builder()
            .sparkContext(context)
            .enableHiveSupport()
            .getOrCreate();

    String file = Objects.requireNonNull(
        App.class.getClassLoader().getResource("in_network.jsonl.json")).getPath();
    Dataset<Row> df = spark.read()
        .json(file);

    df.printSchema();
    df.show();

    df.write()
        .format("delta")
        .saveAsTable("in_network");

  }
}
