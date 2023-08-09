import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.apache.hadoop.conf.Configuration
import org.apache.iceberg.*
import org.apache.iceberg.aws.s3.S3FileIOProperties
import org.apache.iceberg.catalog.SessionCatalog
import org.apache.iceberg.catalog.TableIdentifier
import org.apache.iceberg.data.GenericAppenderFactory
import org.apache.iceberg.data.GenericRecord
import org.apache.iceberg.expressions.Expressions
import org.apache.iceberg.io.OutputFileFactory
import org.apache.iceberg.io.UnpartitionedWriter
import org.apache.iceberg.rest.RESTSessionCatalog
import org.apache.iceberg.types.Types
import org.apache.iceberg.types.Types.StringType
import org.apache.iceberg.types.Types.TimestampType

fun main(args: Array<String>) {
    val catalog = RESTSessionCatalog()
    val conf = Configuration()
    System.setProperty("aws.region", "default")
    System.setProperty("aws.accessKeyId", "alluxio")
    System.setProperty("aws.secretAccessKey", "dummy")
    catalog.setConf(conf)

    val initialConfig: Map<String, String> =
            mapOf(
                    CatalogProperties.URI to "http://localhost:8181/",
                    CatalogProperties.CACHE_ENABLED to "true",
                    S3FileIOProperties.PATH_STYLE_ACCESS to "true",
                    S3FileIOProperties.ENDPOINT to "http://localhost:39999/api/v1/s3/",
            )
    catalog.initialize("rest-catalog", initialConfig)

    val ctx = SessionCatalog.SessionContext.createEmpty()

    val schema =
            Schema(
                    Types.NestedField.of(1, false, "ts1", TimestampType.withZone()),
                    Types.NestedField.of(2, false, "ts2", TimestampType.withZone()),
                    Types.NestedField.of(3, false, "text", StringType()),
            )
    val partitionSpec = PartitionSpec.builderFor(schema).year("ts1").build()
    val table =
            catalog.buildTable(ctx, TableIdentifier.of("default", "test1"), schema)
                    .withPartitionSpec(partitionSpec)
                    .withProperty(TableProperties.FORMAT_VERSION, "2")
                    .create()

    val appenderFactory = GenericAppenderFactory(table.schema())
    val fileFactory = OutputFileFactory.builderFor(table, 0, 0).format(FileFormat.PARQUET).build()
    val writer =
            UnpartitionedWriter(
                    table.spec(),
                    FileFormat.PARQUET,
                    appenderFactory,
                    fileFactory,
                    table.io(),
                    Long.MAX_VALUE
            )

    val r = GenericRecord.create(schema)
    r.setField("ts1", OffsetDateTime.of(2023, 8, 8, 0, 0, 0, 0, ZoneOffset.UTC))
    r.setField("ts2", OffsetDateTime.of(2023, 8, 8, 0, 0, 0, 0, ZoneOffset.UTC))
    r.setField("text", "test message")
    writer.write(r)
    val writeResult = writer.complete()

    val appender = table.newAppend()
    writeResult.dataFiles().forEach { appender.appendFile(it) }
    appender.commit()


    val filterTs1Files = table.newScan()
        .useSnapshot(table.currentSnapshot().snapshotId())
        .filter(Expressions.greaterThan("ts1", 1690848000000000L)) // ts1 > 2023-08-01 00:00:00.000 UTC
        .planFiles()
        .toList()

    val filterTs2Files = table.newScan()
        .useSnapshot(table.currentSnapshot().snapshotId())
        .filter(Expressions.greaterThan("ts2", 1690848000000000L)) // ts2 > 2023-08-01 00:00:00.000 UTC
        .planFiles()
        .toList()

    println("found files filtered by ts1: %d".format(filterTs1Files.size))
    filterTs1Files.forEach{ println(it) }
    println("found files filtered by ts2: %d".format(filterTs2Files.size))
    filterTs2Files.forEach{ println(it) }
}
